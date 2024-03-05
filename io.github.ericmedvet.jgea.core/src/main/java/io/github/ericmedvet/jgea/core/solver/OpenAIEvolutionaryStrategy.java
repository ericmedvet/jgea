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

package io.github.ericmedvet.jgea.core.solver;

import static io.github.ericmedvet.jgea.core.util.VectorUtils.*;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.representation.sequence.FixedLengthListFactory;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
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
        ListPopulationState<
            Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>,
        TotalOrderQualityBasedProblem<S, Q>,
        Individual<List<Double>, S, Q>,
        List<Double>,
        S,
        Q> {

  private final int batchSize;

  private final FixedLengthListFactory<Double> gaussianSamplesFactory;

  public OpenAIEvolutionaryStrategy(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      Predicate<
              ? super
                  ListPopulationState<
                      Individual<List<Double>, S, Q>,
                      List<Double>,
                      S,
                      Q,
                      TotalOrderQualityBasedProblem<S, Q>>>
          stopCondition,
      int batchSize,
      double sigma) {
    super(solutionMapper, genotypeFactory, stopCondition, false);
    this.batchSize = batchSize;
    int p = genotypeFactory.build(1, new Random(0)).get(0).size();
    gaussianSamplesFactory = new FixedLengthListFactory<>(p, r -> r.nextGaussian() * sigma);
  }

  public record State<S, Q>(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      TotalOrderQualityBasedProblem<S, Q> problem,
      Predicate<io.github.ericmedvet.jgea.core.solver.State<?, ?>> stopCondition,
      long nOfBirths,
      long nOfQualityEvaluations,
      PartiallyOrderedCollection<Individual<List<Double>, S, Q>> pocPopulation,
      List<Individual<List<Double>, S, Q>> listPopulation,
      boolean wDecay,
      double stepSize,
      double beta1,
      double beta2,
      double epsilon,
      double[] center,
      double[] m,
      double[] v)
      implements ListPopulationState<
              Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>,
          io.github.ericmedvet.jgea.core.solver.State.WithComputedProgress<
              TotalOrderQualityBasedProblem<S, Q>, S> {
    public static <S, Q> State<S, Q> from(
        TotalOrderQualityBasedProblem<S, Q> problem,
        Collection<Individual<List<Double>, S, Q>> individuals,
        Comparator<? super Individual<List<Double>, S, Q>> comparator,
        Predicate<io.github.ericmedvet.jgea.core.solver.State<?, ?>> stopCondition) {
      int n = individuals.iterator().next().genotype().size();
      return new State<>(
          LocalDateTime.now(),
          0,
          0,
          problem,
          stopCondition,
          0,
          0,
          PartiallyOrderedCollection.from(individuals, comparator),
          individuals.stream().sorted(comparator).toList(),
          false,
          0.02,
          0.9,
          0.999,
          1e-08,
          meanArray(
              individuals.stream().map(i -> unboxed(i.genotype())).toList()),
          new double[n],
          new double[n]);
    }

    public static <S, Q> State<S, Q> from(
        State<S, Q> state,
        Collection<Individual<List<Double>, S, Q>> individuals,
        Comparator<? super Individual<List<Double>, S, Q>> comparator,
        double[] center,
        double[] m,
        double[] v) {
      return new State<>(
          state.startingDateTime,
          ChronoUnit.MILLIS.between(state.startingDateTime, LocalDateTime.now()),
          state.nOfIterations() + 1,
          state.problem,
          state.stopCondition,
          state.nOfBirths + individuals.size(),
          state.nOfQualityEvaluations + individuals.size(),
          PartiallyOrderedCollection.from(individuals, comparator),
          individuals.stream().sorted(comparator).toList(),
          state.wDecay,
          state.stepSize,
          state.beta1,
          state.beta2,
          state.epsilon,
          center,
          m,
          v);
    }
  }

  @Override
  protected Individual<List<Double>, S, Q> newIndividual(
      List<Double> genotype,
      ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>
          state,
      TotalOrderQualityBasedProblem<S, Q> problem) {
    S solution = solutionMapper.apply(genotype);
    return Individual.of(
        genotype,
        solution,
        problem.qualityFunction().apply(solution),
        state == null ? 0 : state.nOfIterations(),
        state == null ? 0 : state.nOfIterations());
  }

  @Override
  protected Individual<List<Double>, S, Q> updateIndividual(
      Individual<List<Double>, S, Q> individual,
      ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>
          state,
      TotalOrderQualityBasedProblem<S, Q> problem) {
    throw new UnsupportedOperationException("This method should not be called");
  }

  @Override
  public ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>
      init(TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor)
          throws SolverException {
    Collection<Individual<List<Double>, S, Q>> individuals =
        map(genotypeFactory.build(2 * batchSize, random), List.of(), null, problem, executor);
    return State.from(problem, individuals, comparator(problem), stopCondition());
  }

  @Override
  public ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>
      update(
          TotalOrderQualityBasedProblem<S, Q> problem,
          RandomGenerator random,
          ExecutorService executor,
          ListPopulationState<
                  Individual<List<Double>, S, Q>,
                  List<Double>,
                  S,
                  Q,
                  TotalOrderQualityBasedProblem<S, Q>>
              state)
          throws SolverException {
    // see https://bacrobotics.com/ section 6.2.2
    State<S, Q> esState = (State<S, Q>) state;
    // produce noise vectors
    List<List<Double>> samples = IntStream.range(0, batchSize)
        .mapToObj(i -> gaussianSamplesFactory.build(random))
        .toList();
    // evaluates scores (ie., map genotypes to individuals)
    List<List<Double>> plusGenotypes =
        samples.stream().map(s -> sum(s, esState.center)).toList();
    List<List<Double>> minusGenotypes =
        samples.stream().map(s -> sum(mult(s, -1), esState.center)).toList();
    List<Individual<List<Double>, S, Q>> newIndividuals = map(
            Stream.of(plusGenotypes, minusGenotypes)
                .flatMap(List::stream)
                .toList(),
            List.of(),
            state,
            problem,
            executor)
        .stream()
        .toList();
    // compute normalized ranks
    Comparator<Integer> integerComparator =
        partialComparator(problem).comparing(newIndividuals::get).comparator();
    List<Double> normalizedRanks = IntStream.range(0, newIndividuals.size())
        .boxed()
        .sorted(integerComparator)
        .map(i -> (double) i / (double) (newIndividuals.size() - 1) - 0.5d)
        .toList();
    // compute estimated gradient
    double[] g = unboxed(meanList(IntStream.range(0, normalizedRanks.size())
        .mapToObj(i -> mult(diff(newIndividuals.get(i).genotype(), esState.center), normalizedRanks.get(i)))
        .toList()));
    // optimize with adam (see https://en.wikipedia.org/wiki/Stochastic_gradient_descent#Adam)
    double[] m = sum(mult(esState.m, esState.beta1), mult(g, 1 - esState.beta1));
    double[] v = sum(mult(esState.v, esState.beta2), mult(mult(g, g), 1d - esState.beta2));
    double[] hatM = mult(m, 1d / (1d - esState.beta1));
    double[] hatV = mult(v, 1d / (1d - esState.beta2));
    double a = esState.stepSize
        * Math.sqrt(1d - Math.pow(esState.beta2, esState.nOfIterations() + 1))
        / (1d - Math.pow(esState.beta1, esState.nOfIterations() + 1));
    double[] dCenter = mult(div(hatM, sum(sqrt(hatV), esState.epsilon)), -a);
    double[] center = sum(esState.center, dCenter);
    return State.from(esState, newIndividuals, comparator(problem), center, m, v);
  }
}
