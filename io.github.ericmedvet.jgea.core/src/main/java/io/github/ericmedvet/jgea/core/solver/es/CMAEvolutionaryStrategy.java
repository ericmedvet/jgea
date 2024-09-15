/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.jgea.core.solver.es;

import static io.github.ericmedvet.jgea.core.util.VectorUtils.*;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

// source -> https://arxiv.org/pdf/1604.00772.pdf

public class CMAEvolutionaryStrategy<S, Q>
    extends AbstractPopulationBasedIterativeSolver<
        CMAESState<S, Q>, TotalOrderQualityBasedProblem<S, Q>, CMAESIndividual<S, Q>, List<Double>, S, Q> {

  private static final Logger L = Logger.getLogger(CMAEvolutionaryStrategy.class.getName());
  private final int mu;
  private final double[] weights;
  private final double cSigma;
  private final double dSigma;
  private final double cc;
  private final double c1;
  private final double cMu;
  private final double muEff;
  private final int p;
  private final double chiN;
  private final int populationSize;

  public CMAEvolutionaryStrategy(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      Predicate<? super CMAESState<S, Q>> stopCondition) {
    this(
        solutionMapper,
        genotypeFactory,
        stopCondition,
        genotypeFactory.build(1, new Random(0)).getFirst().size());
  }

  private CMAEvolutionaryStrategy(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      Predicate<? super CMAESState<S, Q>> stopCondition,
      int p) {
    super(solutionMapper, genotypeFactory, stopCondition, false);
    populationSize = 4 + (int) Math.floor(3 * Math.log(p));
    // see table 1 of the linked paper for parameters values
    this.p = p;
    chiN = Math.sqrt(p) * (1d - 1d / (4d * (double) p) + 1d / (21d * Math.pow(p, 2)));
    // selection and recombination
    mu = (int) Math.floor(populationSize / 2d);
    double[] unnormalizedWeights = buildArray(mu, i -> Math.log((populationSize + 1) / 2d) - Math.log(i + 1));
    double sumOfWeights = Arrays.stream(unnormalizedWeights).sum();
    double sumOfSquaredWeights =
        Arrays.stream(mult(unnormalizedWeights, unnormalizedWeights)).sum();
    weights = mult(unnormalizedWeights, 1d / sumOfWeights);
    muEff = Math.pow(sumOfWeights, 2) / sumOfSquaredWeights;
    // step size control
    cSigma = (muEff + 2) / (p + muEff + 5);
    dSigma = 1 + 2 * Math.max(0, Math.sqrt((muEff - 1) / (p + 1d)) - 1) + cSigma;
    // initialize covariance matrix adaptation
    cc = (4d + muEff / p) / (p + 4d + 2 * muEff / p);
    c1 = 2 / (Math.pow((p + 1.3), 2) + muEff);
    cMu = Math.min(1 - c1, 2 * (muEff - 2 + 1 / muEff) / (Math.pow((p + 2), 2) + 2 * muEff / 2d));
  }

  private CMAESIndividual<S, Q> buildNewIndividual(
      ChildGenotype<List<Double>> childGenotype, CMAESState<S, Q> state, RandomGenerator random) {
    double[] zK = buildArray(p, random::nextGaussian);
    double[] yK = state.B().preMultiply(state.D().preMultiply(zK));
    double[] xK = sum(state.means(), mult(yK, state.sigma()));
    List<Double> g = boxed(xK);
    S solution = solutionMapper.apply(g);
    return CMAESIndividual.of(
        childGenotype.id(),
        g,
        solution,
        state.problem().qualityFunction().apply(solution),
        state.nOfIterations(),
        state.nOfIterations(),
        childGenotype.parentIds(),
        xK,
        yK,
        zK);
  }

  private CMAESState<S, Q> eigenDecomposition(CMAESState<S, Q> state) {
    L.fine(String.format("Eigen decomposition of covariance matrix (i=%d)", state.nOfIterations()));
    EigenDecomposition eig = new EigenDecomposition(state.C());
    RealMatrix B = eig.getV();
    RealMatrix D = eig.getD();
    for (int i = 0; i < p; i++) {
      if (D.getEntry(i, i) < 0) {
        L.warning("An eigenvalue has become negative");
        D.setEntry(i, i, 0d);
      }
      D.setEntry(i, i, Math.sqrt(D.getEntry(i, i)));
    }
    return state.updatedWithMatrices(B, D);
  }

  @Override
  public CMAESState<S, Q> init(
      TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor)
      throws SolverException {
    CMAESState<S, Q> newState = CMAESState.empty(
        problem,
        stopCondition(),
        unboxed(genotypeFactory.build(1, random).getFirst()));
    AtomicLong counter = new AtomicLong(0);
    List<? extends List<Double>> genotypes = genotypeFactory.build(populationSize, random);
    Collection<CMAESIndividual<S, Q>> newIndividuals = getAll(map(
        genotypes.stream()
            .map(g -> new ChildGenotype<List<Double>>(counter.getAndIncrement(), g, List.of()))
            .toList(),
        this::buildNewIndividual,
        newState,
        random,
        executor));
    return newState.updatedWithIteration(newIndividuals);
  }

  @Override
  public CMAESState<S, Q> update(RandomGenerator random, ExecutorService executor, CMAESState<S, Q> state)
      throws SolverException {
    // find best individuals
    List<CMAESIndividual<S, Q>> bestMuIndividuals = state.listPopulation().stream()
        .sorted(comparator(state.problem()))
        .limit(mu)
        .toList();
    // update distribution
    state = updateDistribution(bestMuIndividuals, state);
    // update B and D from C
    if ((state.nOfIterations() - state.lastEigenUpdateIteration()) > (1d / (c1 + cMu) / p / 10d)) {
      state = eigenDecomposition(state);
    }
    // sample new population
    List<Long> parentIds = bestMuIndividuals.stream().map(Individual::id).toList();
    List<Double> emptyGenotype = List.of();
    AtomicLong counter = new AtomicLong(state.nOfBirths());
    Collection<CMAESIndividual<S, Q>> newIndividuals = getAll(map(
        IntStream.range(0, populationSize)
            .mapToObj(i -> new ChildGenotype<>(counter.getAndIncrement(), emptyGenotype, parentIds))
            .toList(),
        this::buildNewIndividual,
        state,
        random,
        executor));
    return state.updatedWithIteration(newIndividuals);
  }

  private CMAESState<S, Q> updateDistribution(List<CMAESIndividual<S, Q>> bestIndividuals, CMAESState<S, Q> state) {
    // best mu ranked points
    double[][] xMu = new double[mu][p];
    double[][] yMu = new double[mu][p];
    double[][] zMu = new double[mu][p];
    for (int i = 0; i < mu; i++) {
      xMu[i] = bestIndividuals.get(i).x();
      yMu[i] = bestIndividuals.get(i).y();
      zMu[i] = bestIndividuals.get(i).z();
    }
    // selection and recombination
    double[] updatedDistributionMeans = weightedMeanArray(xMu, weights);
    double[] yW = mult(diff(updatedDistributionMeans, state.means()), 1d / state.sigma());
    // step size control
    double[] zM = weightedMeanArray(zMu, weights);
    double[] bzM = state.B().preMultiply(zM);
    double[] sEvolutionPath = buildArray(
        p,
        i -> (1d - cSigma) * state.sEvolutionPath()[i] + (Math.sqrt(cSigma * (2d - cSigma) * muEff)) * bzM[i]);
    double psNorm = norm(sEvolutionPath, 2d);
    double sigma = state.sigma() * Math.exp((cSigma / dSigma) * ((psNorm / chiN) - 1));
    // check flat fitness
    if (state.pocPopulation().firsts().size() >= Math.ceil(0.7 * populationSize)) {
      sigma *= Math.exp(0.2 + cSigma / dSigma);
      L.warning("Flat fitness, consider reformulating the objective");
    }
    // covariance matrix adaptation
    int hSigma =
        psNorm / Math.sqrt(1 - Math.pow((1d - cSigma), 2 * state.nOfIterations())) / chiN < (1.4 + 2d / (p + 1))
            ? 1
            : 0;
    double[] cEvolutionPath = buildArray(
        p, i -> (1 - cc) * state.cEvolutionPath()[i] + hSigma * Math.sqrt(cc * (2 - cc) * muEff) * yW[i]);
    double deltaH = (1 - hSigma) * cc * (2 - cc);
    IntStream.range(0, p).forEach(i -> IntStream.range(0, i + 1).forEach(j -> {
      double cij = (1 + c1 * deltaH - c1 - cMu) * state.C().getEntry(i, j)
          + c1 * cEvolutionPath[i] * cEvolutionPath[j]
          + cMu
              * IntStream.range(0, mu)
                  .mapToDouble(k -> weights[k] * yMu[k][i] * yMu[k][j])
                  .sum();
      state.C().setEntry(i, j, cij);
      state.C().setEntry(j, i, cij);
    }));
    return state.updatedWithPaths(updatedDistributionMeans, sigma, sEvolutionPath, cEvolutionPath, state.C());
  }
}
