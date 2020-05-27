package it.units.malelab.jgea.core.evolver;

import com.google.common.base.Stopwatch;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.evolver.stopcondition.StopCondition;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.CachedBoundedNonDeterministicFunction;
import it.units.malelab.jgea.core.function.CachedNonDeterministicFunction;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.event.*;
import it.units.malelab.jgea.core.ranker.Ranker;
import it.units.malelab.jgea.core.util.Misc;
import org.apache.commons.math3.linear.*;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author luca
 */
public class CMAEvolutionStrategy <F> implements Evolver<double[], double[], F> {

    /**
     * Population size, sample size, number of offspring, λ.
     */
    protected final int lambda;
    /**
     * Parent number, number of (positively) selected search points in the population, number of strictly
     * positive recombination weights, µ.
     */
    protected final int mu;
    /**
     * Problem dimensionality.
     */
    protected final int size;
    /**
     * Endpoint of the initial search points.
     */
    protected final double initMin;
    /**
     * Endpoint of the initial search points.
     */
    protected final double initMax;
    /**
     * Step-size.
     */
    double stepSize;
    /**
     * Recombination weights.
     */
    double[] weights;
    /**
     * Learning rate for the cumulation for the step-size control.
     */
    protected final double cs;
    /**
     * Learning rate for cumulation for the rank-one update of the covariance matrix.
     */
    protected final double cc;
    /**
     * The variance effective selection mass for the mean.
     */
    protected final double mueff;
    /**
     * Expectation of ||N(0,I)|| == norm(randn(N,1))
     */
    protected final double chiN;
    /**
     * Learning rate for the rank-mu update of the covariance matrix update.
     */
    protected final double cmu;
    /**
     * Learning rate for the rank-one update of the covariance matrix update.
     */
    protected final double c1;
    /**
     * Damping parameter for step-size update.
     */
    protected final double damps;
    protected final Ranker<Individual<double[], double[], F>> ranker;
    protected final List<StopCondition> stopConditions;
    protected final long cacheSize;

    private static final Logger L = Logger.getLogger(CMAEvolutionStrategy.class.getName());

    public CMAEvolutionStrategy(int lambda, int mu, int size, double initMin, double initMax, double stepSize, double[] weights, double cs, double cc, double mueff, double chiN, double cmu, double c1, double damps, Ranker<Individual<double[], double[], F>> ranker, List<StopCondition> stopConditions, long cacheSize) {
        this.lambda = lambda;
        this.mu = mu;
        this.size = size;
        this.initMin = initMin;
        this.initMax = initMax;
        this.stepSize = stepSize;
        this.weights = weights;
        this.cs = cs;
        this.cc = cc;
        this.mueff = mueff;
        this.chiN = chiN;
        this.cmu = cmu;
        this.c1 = c1;
        this.damps = damps;
        this.ranker = ranker;
        this.stopConditions = stopConditions;
        this.cacheSize = cacheSize;
    }

    public CMAEvolutionStrategy(int lambda, int mu, int size, double initMin, double initMax, double stepSize, Ranker<Individual<double[], double[], F>> ranker, List<StopCondition> stopConditions, long cacheSize) {
        this.lambda = lambda;
        this.mu = mu;
        this.size = size;
        this.initMin = initMin;
        this.initMax = initMax;
        this.stepSize = stepSize;
        this.ranker = ranker;
        this.stopConditions = stopConditions;
        this.cacheSize = cacheSize;

        weights = new double[mu];
        double sum = 0d;
        double sumSq = 0d;
        for (int i = 0; i < mu; i++) {
            weights[i] = Math.log((lambda + 1) / 2d) - Math.log(i + 1);
            sum += weights[i];
            sumSq += Math.pow(weights[i], 2);
        }
        // normalize recombination weights array
        for (int i = 0; i < mu; i++) {
            weights[i] /= sum;
        }
        mueff = Math.pow(sum, 2) / sumSq;

        // initialize step-size control parameters
        cs = (mueff + 2) / (size + mueff + 5);
        damps = 1 + 2 * Math.max(0, Math.sqrt((mueff - 1) / (size + 1d)) - 1) + cs;

        // initialize covariance matrix adaptation
        cc = (4d + mueff / size) / (size + 4d + 2 * mueff / size);
        c1 = 2 / (Math.pow((size + 1.3), 2) + mueff);
        cmu = Math.min(1 - c1, 2 * (mueff - 2 + 1 / mueff) / (Math.pow((size + 2), 2) + 2 * mueff / 2d));

        // initialize other variables
        chiN = Math.sqrt(size) * (1d - 1d / (4d * size) + 1d / (21d * Math.pow(size, 2)));
    }

    /**
     * Constructs a new CMA-ES instance using default parameters.
     */
    public CMAEvolutionStrategy(int size, double initMin, double initMax, Ranker<Individual<double[], double[], F>> ranker, List<StopCondition> stopConditions, long cacheSize) {
        this.size = size;
        this.initMin = initMin;
        this.initMax = initMax;
        this.ranker = ranker;
        this.stopConditions = stopConditions;
        this.cacheSize = cacheSize;

        stepSize = 0.5;
        // initialize selection and recombination parameters
        lambda = 4 + (int) Math.floor(3 * Math.log(size));
        mu = (int) Math.floor(lambda / 2d);
        weights = new double[mu];
        double sum = 0d;
        double sumSq = 0d;
        for (int i = 0; i < mu; i++) {
            weights[i] = Math.log((lambda + 1) / 2d) - Math.log(i + 1);
            sum += weights[i];
            sumSq += Math.pow(weights[i], 2);
        }
        // normalize recombination weights array
        for (int i = 0; i < mu; i++) {
            weights[i] /= sum;
        }
        mueff = Math.pow(sum, 2) / sumSq;

        // initialize step-size control parameters
        cs = (mueff + 2) / (size + mueff + 5);
        damps = 1 + 2 * Math.max(0, Math.sqrt((mueff - 1) / (size + 1d)) - 1) + cs;

        // initialize covariance matrix adaptation
        cc = (4d + mueff / size) / (size + 4d + 2 * mueff / size);
        c1 = 2 / (Math.pow((size + 1.3), 2) + mueff);
        cmu = Math.min(1 - c1, 2 * (mueff - 2 + 1 / mueff) / (Math.pow((size + 2), 2) + 2 * mueff / 2d));

        // initialize other variables
        chiN = Math.sqrt(size) * (1d - 1d / (4d * size) + 1d / (21d * Math.pow(size, 2)));
    }

    @Override
    public Collection<double[]> solve(Problem<double[], F> problem, Random random, ExecutorService executor, Listener listener) throws InterruptedException, ExecutionException {
        // Mean value of the search distribution.
        double[] distrMean = new double[size];
        // Evolution path for step-size.
        double[] sEvolutionPath = new double[size];
        // Evolution path for covariance matrix, a sequence of successive (normalized) steps, the strategy
        // takes over a number of generations.
        double[] CEvolutionPath = new double[size];
        // Orthogonal matrix. Columns of B are eigenvectors of C with unit length and correspond to the diagonal
        // element of D.
        RealMatrix B = MatrixUtils.createRealIdentityMatrix(size);
        // Diagonal matrix. The diagonal elements of D are square roots of eigenvalues of C and correspond to the
        // respective columns of B.
        RealMatrix D = MatrixUtils.createRealIdentityMatrix(size);
        // Covariance matrix at the current generation.
        RealMatrix C = MatrixUtils.createRealIdentityMatrix(size);
        // Last generation when the eigendecomposition was calculated.
        AtomicInteger lastEigenUpdate = new AtomicInteger();

        // objective variables initial point
        for (int i = 0; i < size; i++) {
            distrMean[i] = random.nextDouble() * (initMax - initMin) + initMin;
        }

        L.fine("Starting");
        int generations = 0;
        AtomicInteger births = new AtomicInteger();
        AtomicInteger fitnessEvaluations = new AtomicInteger();
        Stopwatch stopwatch = Stopwatch.createStarted();
        NonDeterministicFunction<double[], F> fitnessFunction = problem.getFitnessFunction();
        if (cacheSize > 0) {
            if (fitnessFunction instanceof Bounded) {
                fitnessFunction = new CachedBoundedNonDeterministicFunction<>(fitnessFunction, cacheSize);
            } else {
                fitnessFunction = fitnessFunction.cached(cacheSize);
            }
        }
        List<Individual<double[], double[], F>> population = new ArrayList<>();
        //iterate
        while (true) {
            generations = generations + 1;
            // sample population
            L.fine(String.format("Sampling population (g=%d)", generations));
            List<Individual<double[], double[], F>> newPopulation = samplePopulation(fitnessFunction, generations, births, fitnessEvaluations, random, listener, executor, distrMean, B, D);
            // update population
            L.fine(String.format("Updating population (g=%d)", generations));
            population.clear();
            population.addAll(newPopulation);
            // update distribution
            L.fine(String.format("Updating distribution (g=%d)", generations));
            updateDistribution(population, generations, random, distrMean, sEvolutionPath, CEvolutionPath, B, D, C);
            // update B and D from C
            if ((generations - lastEigenUpdate.get()) > (1d / (c1 + cmu) / size / 10d)) {
                L.fine(String.format("Eigendecomposition of covariance matrix (g=%d)", generations));
                lastEigenUpdate.getAndSet(generations);
                EigenDecomposition eig = new EigenDecomposition(C);
                // normalized eigenvectors
                B = eig.getV();
                D = eig.getD();
                // D contains standard deviations now
                for (int i = 0; i < size; i++) {
                    // numerical problem?
                    if (D.getEntry(i, i) < 0) {
                        L.warning("An eigenvalue has become negative");
                        D.setEntry(i, i, 0d);
                    }
                    D.setEntry(i, i, Math.sqrt(D.getEntry(i, i)));
                }
            }
            // send event
            L.fine(String.format("Ranking population (g=%d)", generations));
            List<Collection<Individual<double[], double[], F>>> rankedPopulation = ranker.rank(population, random);
            EvolutionEvent event = new EvolutionEvent(
                    generations,
                    births.get(),
                    (fitnessFunction instanceof CachedNonDeterministicFunction) ? ((CachedNonDeterministicFunction) fitnessFunction).getActualCount() : fitnessEvaluations.get(),
                    (List) rankedPopulation,
                    stopwatch.elapsed(TimeUnit.MILLISECONDS)
            );
            listener.listen(event);
            //check stopping conditions
            StopCondition stopCondition = checkStopConditions(event);
            if (stopCondition != null) {
                listener.listen(new EvolutionEndEvent(
                        stopCondition,
                        generations,
                        births.get(),
                        (fitnessFunction instanceof CachedNonDeterministicFunction) ? ((CachedNonDeterministicFunction) fitnessFunction).getActualCount() : fitnessEvaluations.get(),
                        rankedPopulation,
                        stopwatch.elapsed(TimeUnit.MILLISECONDS))
                );
                L.fine(String.format("Stopping, criterion %s met (g=%d)", stopCondition.getClass().getSimpleName(), generations));
                break;
            }
            // escape flat fitness, or better terminate?
            if (rankedPopulation.get(0).size() >= Math.ceil(0.7 * lambda)) {
                stepSize = stepSize * Math.exp(0.2 + cs / damps);
                L.warning("Flat fitness, consider reformulating the objective");
            }
        }
        L.fine("Ending");
        //take out solutions
        List<Collection<Individual<double[], double[], F>>> rankedPopulation = ranker.rank(population, random);
        Collection<double[]> solutions = new ArrayList<>();
        for (Individual<double[], double[], F> individual : rankedPopulation.get(0)) {
            solutions.add(individual.getSolution());
        }
        return solutions;
    }

    protected StopCondition checkStopConditions(EvolutionEvent event) {
        for (StopCondition stopCondition : stopConditions) {
            if (stopCondition.shouldStop(event)) {
                return stopCondition;
            }
        }
        return null;
    }

    protected Callable<Individual<double[], double[], F>> birthCallable(
            final double[] point,
            final int birthIteration,
            final NonDeterministicFunction<double[], F> fitnessFunction,
            final Random random,
            final Listener listener) {
        return () -> {
            Stopwatch stopwatch = Stopwatch.createUnstarted();
            Capturer capturer = new Capturer();
            long elapsed;
            //solution -> fitness
            stopwatch.reset().start();
            F fitness = fitnessFunction.apply(point, random, capturer);
            elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
            Map<String, Object> fitnessInfo = Misc.fromInfoEvents(capturer.getEvents(), "fitness.");
            listener.listen(new TimedEvent(elapsed, TimeUnit.NANOSECONDS, new FunctionEvent(point, fitness, fitnessInfo)));
            //merge info
            return new Individual<>(point, point, fitness, birthIteration, null, fitnessInfo);
        };
    }

    protected List<Individual<double[], double[], F>> samplePopulation(
            final NonDeterministicFunction<double[], F> fitnessFunction,
            final int generation,
            final AtomicInteger births,
            final AtomicInteger fitnessEvaluations,
            final Random random,
            final Listener listener,
            final ExecutorService executor,
            final double[] distrMean,
            final RealMatrix B,
            final RealMatrix D) throws InterruptedException, ExecutionException {
        List<Callable<Individual<double[], double[], F>>> tasks = new ArrayList<>();
        while (tasks.size() < lambda) {
            // new point
            double[] newPoint = new double[size];
            // z ∼ N (0, I) normally distributed vector
            double[] arz = new double[size];
            for (int i = 0; i < size; i++) {
                arz[i] = random.nextGaussian();
            }
            // y ∼ N (0, C) y = (B*(D*z))
            double[] ary = B.preMultiply(D.preMultiply(arz));
            for (int i = 0; i < size; i++) {
                // add mutation (sigma*B*(D*z))
                newPoint[i] = distrMean[i] + stepSize * ary[i];
            }
            tasks.add(birthCallable(newPoint, generation, fitnessFunction, random, listener));
            births.incrementAndGet();
            fitnessEvaluations.incrementAndGet();
        }
        return Misc.getAll(executor.invokeAll(tasks));
    }

    protected void updateDistribution(
            final List<Individual<double[], double[], F>> population,
            final int generation,
            final Random random,
            final double[] distrMean,
            final double[] sEvolutionPath,
            final double[] CEvolutionPath,
            final RealMatrix B,
            final RealMatrix D,
            final RealMatrix C) {
        // sort by fitness and compute weighted mean into distrMean
        List<Collection<Individual<double[], double[], F>>> rankedPopulation = ranker.rank(population, random);
        // best mu ranked points
        List<Individual<double[], double[], F>> bestMuPoints = new ArrayList<>();
        int paretoLevel = 0;
        while (bestMuPoints.size() < mu) {
            bestMuPoints.addAll(rankedPopulation.get(paretoLevel).stream().limit(mu - bestMuPoints.size()).collect(Collectors.toList()));
            paretoLevel++;
        }
        double[] oldDistrMean = Arrays.copyOf(distrMean, distrMean.length);
        double[] artmp = new double[size];
        // recombination
        for (int i = 0; i < size; i++) {
            distrMean[i] = 0;
            for (int j = 0; j < mu; j++) {
                distrMean[i] += weights[j] * bestMuPoints.get(j).getGenotype()[i];
            }
            artmp[i] = (distrMean[i] - oldDistrMean[i]) / stepSize;
        }
        // (D^-1*B'*(xmean-xold)/sigma)
        double[] zmean = MatrixUtils.inverse(D).preMultiply(B.transpose().preMultiply(artmp));

        // cumulation: update evolution paths
        double[] Bzmean = B.preMultiply(zmean);
        for (int i = 0; i < size; i++) {
            sEvolutionPath[i] = (1d - cs) * sEvolutionPath[i] + (Math.sqrt(cs * (2d - cs) * mueff)) * Bzmean[i];
        }

        // calculate step-size evolution path norm
        double psNorm = 0.0;
        for (int i = 0; i < size; i++) {
            psNorm += sEvolutionPath[i] * sEvolutionPath[i];
        }
        psNorm = Math.sqrt(psNorm);

        // Heaviside function
        int hsig = 0;
        if (psNorm / Math.sqrt(1 - Math.pow((1d - cs), 2 * generation)) / chiN < (1.4 + 2d / (size + 1))) {
            hsig = 1;
        }

        for (int i = 0; i < size; i++) {
            CEvolutionPath[i] = (1 - cc) * CEvolutionPath[i] + hsig * Math.sqrt(cc * (2 - cc) * mueff) * artmp[i];
        }

        // adapt covariance matrix C
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                double rankOneUpdate = CEvolutionPath[i] * CEvolutionPath[j] + (1 - hsig) * cc * (2 - cc) * C.getEntry(i, j);
                double rankMuUpdate = 0d;
                for (int k = 0; k < mu; k++) {
                    rankMuUpdate += weights[k] * ((bestMuPoints.get(k).getGenotype()[i] - oldDistrMean[i]) / stepSize) * ((bestMuPoints.get(k).getGenotype()[j] - oldDistrMean[j]) / stepSize);
                }
                C.setEntry(i, j, (1 - c1 - cmu) * C.getEntry(i, j) + c1 * rankOneUpdate + cmu * rankMuUpdate);
            }
        }

        // adapt step size sigma
        stepSize = stepSize * Math.exp((cs / damps) * ((psNorm / chiN) - 1));
    }

    protected void eigenDecomposition(
            final RealMatrix B,
            final RealMatrix D,
            final RealMatrix C) {
        // update B and D from C
        EigenDecomposition eig = new EigenDecomposition(C);
        for (int i = 0; i < size; i++) {
            B.setColumnVector(i, eig.getV().getColumnVector(i));
            D.setEntry(i, i, Math.sqrt(eig.getD().getEntry(i, i)));
        }
    }
}
