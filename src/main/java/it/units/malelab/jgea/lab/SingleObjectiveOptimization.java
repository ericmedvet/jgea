package it.units.malelab.jgea.lab;

import com.google.common.collect.Lists;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.evolver.CMAEvolutionStrategy;
import it.units.malelab.jgea.core.evolver.DifferentialEvolution;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.FitnessEvaluations;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.MultiFileListenerFactory;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.ranker.ComparableRanker;
import it.units.malelab.jgea.core.ranker.FitnessComparator;
import it.units.malelab.jgea.core.ranker.selector.Tournament;
import it.units.malelab.jgea.core.ranker.selector.Worst;
import it.units.malelab.jgea.problem.synthetic.LinearPoints;
import it.units.malelab.jgea.problem.synthetic.Rastrigin;
import it.units.malelab.jgea.problem.synthetic.Sphere;
import it.units.malelab.jgea.representation.sequence.Sequence;
import it.units.malelab.jgea.representation.sequence.numeric.DoubleSequenceFactory;
import it.units.malelab.jgea.representation.sequence.numeric.GaussianMutation;
import it.units.malelab.jgea.representation.sequence.numeric.GeometricCrossover;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static it.units.malelab.jgea.core.util.Args.*;

public class SingleObjectiveOptimization extends Worker {

    private final static Logger L = Logger.getLogger(SingleObjectiveOptimization.class.getName());

    public final static void main(String[] args) throws FileNotFoundException {
        new SingleObjectiveOptimization(args);
    }

    public SingleObjectiveOptimization(String[] args) throws FileNotFoundException {
        super(args);
    }

    @Override
    public void run() {
        // prepare parameters
        int[] runs = ri(a("runs", "0:10"));
        int size = i(a("dim", "2"));
        int evaluations = i(a("nev", "1000"));
        int population = i(a("npop", "100"));
        boolean overlapping = b(a("overlapping", "true"));
        int cacheSize = i(a("cache", "10000"));
        double crossoverRate = d(a("cr", "0.8"));
        double differentialWeight = d(a("dw", "0.5"));

        // prepare problems
        Map<String, Problem<double[], Double>> problems = new LinkedHashMap<>();
//        problems.put("LinearPoints", new LinearPoints());
//        problems.put("Sphere", new Sphere());
        problems.put("Rastrigin", new Rastrigin());

        // prepare evolvers
        Set<String> evolverNames = new LinkedHashSet<>();
        evolverNames.add("GA");
        evolverNames.add("DE");
        evolverNames.add("CMAES");

        //prepare things
        MultiFileListenerFactory listenerFactory = new MultiFileListenerFactory(a("dir", "."), a("file", null));

        // iterate
        for (Map.Entry<String, Problem<double[], Double>> problemEntry : problems.entrySet()) {
            for (String evolverName : evolverNames) {
                for (int run : runs) {
                    Problem<double[], Double> problem = problemEntry.getValue();
                    if (evolverName.equals("GA")) {
                        //prepare problem-dependent components
                        Factory genotypeFactory = null;
                        Function<Sequence<Double>, double[]> mapper = (genotype, listener) -> {
                            final double[] doubles = new double[genotype.size()];
                            for (int i = 0; i < genotype.size(); i++) {
                                doubles[i] = genotype.get(i);
                            }
                            return doubles;
                        };
                        Map<GeneticOperator, Double> operators = new LinkedHashMap<>();
                        if (problemEntry.getKey().equals("LinearPoints")) {
                            genotypeFactory = new DoubleSequenceFactory(5, 10, size);
                            operators.put(new GaussianMutation(0.5), 0.2);
                            operators.put(new GeometricCrossover(), 0.8);
                        } else if (problemEntry.getKey().equals("Sphere")) {
                            genotypeFactory = new DoubleSequenceFactory(-10, 10, size);
                            operators.put(new GaussianMutation(0.5), 0.2);
                            operators.put(new GeometricCrossover(), 0.8);
                        } else if ((problemEntry.getKey().equals("Rastrigin"))) {
                            genotypeFactory = new DoubleSequenceFactory(-5.12, 5.12, size);
                            operators.put(new GaussianMutation(0.5), 0.2);
                            operators.put(new GeometricCrossover(), 0.8);
                        }
                        //prepare evolver
                        StandardEvolver<Sequence<Double>, double[], Double> evolver = new StandardEvolver(
                                population,
                                genotypeFactory,
                                new ComparableRanker(new FitnessComparator<>(Function.identity())),
                                mapper,
                                operators,
                                new Tournament<>(3),
                                new Worst<>(),
                                population,
                                overlapping,
                                Lists.newArrayList(new FitnessEvaluations(evaluations)),
                                cacheSize,
                                false
                        );
                        //prepare keys
                        Map<String, String> keys = new LinkedHashMap<>();
                        keys.put("run", Integer.toString(run));
                        keys.put("problem", problemEntry.getKey());
                        keys.put("max.evaluations", Integer.toString(evaluations));
                        keys.put("evolver", evolverName);
                        keys.put("overlapping", Boolean.toString(overlapping));
                        System.out.println(keys);
                        // run evolver
                        Random r = new Random(run);
                        try {
                            evolver.solve(problem, r, executorService, Listener.onExecutor(listenerFactory.build(
                                    new Static(keys),
                                    new Basic(),
                                    new Population(),
                                    new BestInfo<>((Function) problem.getFitnessFunction(), "%5.3f"),
                                    new BestPrinter(new DoubleArrayPrinter("%+5.3f"), "%s"),
                                    new FunctionOfBest<>("actual.fitness", (Function) problem.getFitnessFunction().cached(0), "%6.4f")
                            ), executorService));
                        } catch (InterruptedException | ExecutionException ex) {
                            L.log(Level.SEVERE, String.format("Cannot solve problem: %s", ex), ex);

                            ex.printStackTrace();
                            System.exit(0);
                        }
                    } else if (evolverName.equals("DE")) {
                        // prepare problem-dependent components
                        Double initMin = null;
                        Double initMax = null;
                        if (problemEntry.getKey().equals("LinearPoints")) {
                            initMin = 5d;
                            initMax = 10d;
                        } else if (problemEntry.getKey().equals("Sphere")) {
                            initMin = -10d;
                            initMax = 10d;
                        } else if ((problemEntry.getKey().equals("Rastrigin"))) {
                            initMin = -5.12;
                            initMax = 5.12;
                        }
                        // prepare evolver
                        DifferentialEvolution<Double> de = new DifferentialEvolution<>(
                                population, i(a("threads", Integer.toString(Runtime.getRuntime().availableProcessors()))),
                                crossoverRate, differentialWeight,
                                size,
                                initMin, initMax,
                                new ComparableRanker(new FitnessComparator(Function.identity())),
                                Lists.newArrayList(new FitnessEvaluations(evaluations)), cacheSize);
                        // prepare keys
                        Map<String, String> keys = new LinkedHashMap<>();
                        keys.put("run", Integer.toString(run));
                        keys.put("problem", problemEntry.getKey());
                        keys.put("max.evaluations", Integer.toString(evaluations));
                        keys.put("evolver", evolverName);
                        keys.put("overlapping", Boolean.toString(overlapping));
                        System.out.println(keys);
                        // run evolver
                        Random random = new Random(run);
                        try {
                            de.solve(problem, random, executorService, Listener.onExecutor(listenerFactory.build(
                                    new Static(keys),
                                    new Basic(),
                                    new Population(),
                                    new BestInfo<>((Function) problem.getFitnessFunction(), "%5.3f"),
                                    new BestPrinter(new DoubleArrayPrinter("%+5.3f"), "%s"),
                                    new FunctionOfBest<>("actual.fitness", (Function) problem.getFitnessFunction().cached(0), "%6.4f")
                            ), executorService));
                        } catch (InterruptedException | ExecutionException ex) {
                            L.log(Level.SEVERE, String.format("Cannot solve problem: %s", ex), ex);
                        }
                    } else if (evolverName.equals("CMAES")) {
                        // prepare problem-dependent components
                        Double initMin = null;
                        Double initMax = null;
                        if (problemEntry.getKey().equals("LinearPoints")) {
                            initMin = 5d;
                            initMax = 10d;
                        } else if (problemEntry.getKey().equals("Sphere")) {
                            initMin = -10d;
                            initMax = 10d;
                        } else if ((problemEntry.getKey().equals("Rastrigin"))) {
                            initMin = -5.12;
                            initMax = 5.12;
                        }
                        // prepare evolver
                        Random random = new Random(run);
                        CMAEvolutionStrategy<Double> cmaes = new CMAEvolutionStrategy<>(
                                size,
                                initMin, initMax,
                                new ComparableRanker(new FitnessComparator(Function.identity())),
                                Lists.newArrayList(new FitnessEvaluations(evaluations)),
                                cacheSize);
                        // prepare keys
                        Map<String, String> keys = new LinkedHashMap<>();
                        keys.put("run", Integer.toString(run));
                        keys.put("problem", problemEntry.getKey());
                        keys.put("max.evaluations", Integer.toString(evaluations));
                        keys.put("evolver", evolverName);
                        keys.put("overlapping", Boolean.toString(overlapping));
                        System.out.println(keys);
                        // run evolver
                        try {
                            cmaes.solve(problem, random, executorService, Listener.onExecutor(listenerFactory.build(
                                    new Static(keys),
                                    new Basic(),
                                    new Population(),
                                    new BestInfo<>((Function) problem.getFitnessFunction(), "%5.3f"),
                                    new BestPrinter(new DoubleArrayPrinter("%+5.3f"), "%s"),
                                    new FunctionOfBest<>("actual.fitness", (Function) problem.getFitnessFunction().cached(0), "%6.4f")
                            ), executorService));
                        } catch (InterruptedException | ExecutionException ex) {
                            L.log(Level.SEVERE, String.format("Cannot solve problem: %s", ex), ex);
                        }
                    }
                }
            }
        }
    }
}
