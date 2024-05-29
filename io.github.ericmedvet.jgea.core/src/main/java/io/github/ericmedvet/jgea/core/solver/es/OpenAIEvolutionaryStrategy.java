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
import io.github.ericmedvet.jgea.core.representation.sequence.FixedLengthListFactory;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

// https://bacrobotics.com/
// https://github.com/snolfi/evorobotpy2/blob/master/bin/openaies.py
// https://arxiv.org/abs/1703.03864

public class OpenAIEvolutionaryStrategy<S, Q>
    extends AbstractPopulationBasedIterativeSolver<
        OpenAIESState<S, Q>,
        TotalOrderQualityBasedProblem<S, Q>,
        Individual<List<Double>, S, Q>,
        List<Double>,
        S,
        Q> {

  private final int batchSize;
  private final double stepSize;
  private final double beta1;
  private final double beta2;
  private final double epsilon;

  private final FixedLengthListFactory<Double> gaussianSamplesFactory;

  public OpenAIEvolutionaryStrategy(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      Predicate<? super OpenAIESState<S, Q>> stopCondition,
      int batchSize,
      double sigma,
      double stepSize,
      double beta1,
      double beta2,
      double epsilon) {
    super(solutionMapper, genotypeFactory, stopCondition, false);
    this.batchSize = batchSize;
    this.stepSize = stepSize;
    this.beta1 = beta1;
    this.beta2 = beta2;
    this.epsilon = epsilon;
    int p = genotypeFactory.build(1, new Random(0)).get(0).size();
    gaussianSamplesFactory = new FixedLengthListFactory<>(p, r -> r.nextGaussian() * sigma);
  }

  @Override
  public OpenAIESState<S, Q> init(
      TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor)
      throws SolverException {
    OpenAIESState<S, Q> newState = OpenAIESState.empty(
        problem,
        stopCondition(),
        unboxed(genotypeFactory.build(1, random).get(0)));
    AtomicLong counter = new AtomicLong(0);
    Collection<Individual<List<Double>, S, Q>> newIndividuals = getAll(map(
        genotypeFactory.build(2 * batchSize, random).stream()
            .map(g -> new ChildGenotype<List<Double>>(counter.getAndIncrement(), g, List.of()))
            .toList(),
        (cg, s, r) -> Individual.from(cg, solutionMapper, s.problem().qualityFunction(), s.nOfIterations()),
        newState,
        random,
        executor));
    return newState.updatedWithIteration(
        newIndividuals,
        meanArray(
            newIndividuals.stream().map(i -> unboxed(i.genotype())).toList()),
        newState.m(),
        newState.v());
  }

  @Override
  public OpenAIESState<S, Q> update(RandomGenerator random, ExecutorService executor, OpenAIESState<S, Q> state)
      throws SolverException {
    // produce noise vectors
    List<List<Double>> samples = IntStream.range(0, batchSize)
        .mapToObj(i -> gaussianSamplesFactory.build(random))
        .toList();
    // evaluates scores (ie., map genotypes to individuals)
    List<List<Double>> plusGenotypes =
        samples.stream().map(s -> sum(s, state.center())).toList();
    List<List<Double>> minusGenotypes =
        samples.stream().map(s -> sum(mult(s, -1), state.center())).toList();
    AtomicLong counter = new AtomicLong(state.nOfBirths());
    List<Long> parentIds =
        state.listPopulation().stream().map(Individual::id).toList();
    List<Individual<List<Double>, S, Q>> newIndividuals = getAll(map(
            Stream.of(plusGenotypes, minusGenotypes).flatMap(List::stream).toList().stream()
                .map(g -> new ChildGenotype<>(counter.getAndIncrement(), g, parentIds))
                .toList(),
            (cg, s, r) ->
                Individual.from(cg, solutionMapper, s.problem().qualityFunction(), s.nOfIterations()),
            state,
            random,
            executor))
        .stream()
        .toList();
    // compute normalized ranks
    Comparator<Integer> integerComparator = partialComparator(state.problem())
        .comparing(newIndividuals::get)
        .comparator();
    List<Double> normalizedRanks = IntStream.range(0, newIndividuals.size())
        .boxed()
        .sorted(integerComparator)
        .map(i -> (double) i / (double) (newIndividuals.size() - 1) - 0.5d)
        .toList();
    // compute estimated gradient
    double[] g = unboxed(meanList(IntStream.range(0, normalizedRanks.size())
        .mapToObj(i -> mult(diff(newIndividuals.get(i).genotype(), state.center()), normalizedRanks.get(i)))
        .toList()));
    // optimize with adam (see https://en.wikipedia.org/wiki/Stochastic_gradient_descent#Adam)
    double[] m = sum(mult(state.m(), beta1), mult(g, 1 - beta1));
    double[] v = sum(mult(state.v(), beta2), mult(mult(g, g), 1d - beta2));
    double[] hatM = mult(m, 1d / (1d - beta1));
    double[] hatV = mult(v, 1d / (1d - beta2));
    double a = stepSize
        * Math.sqrt(1d - Math.pow(beta2, state.nOfIterations() + 1))
        / (1d - Math.pow(beta1, state.nOfIterations() + 1));
    double[] dCenter = mult(div(hatM, sum(sqrt(hatV), epsilon)), -a);
    double[] center = sum(state.center(), dCenter);
    return state.updatedWithIteration(newIndividuals, center, m, v);
  }
}
