/*
 * Copyright 2022 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jgea.core.solver;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.order.DAGPartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.representation.sequence.FixedLengthListFactory;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jgea.core.util.Progress;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

// https://bacrobotics.com/
// https://github.com/snolfi/evorobotpy2/blob/master/bin/openaies.py
// https://arxiv.org/abs/1703.03864

public class OpenAIEvolutionaryStrategy<S, Q> extends AbstractPopulationBasedIterativeSolver<OpenAIEvolutionaryStrategy.State<S, Q>, TotalOrderQualityBasedProblem<S, Q>, List<Double>, S, Q> {

  private final int batchSize;
  private final double sigma;

  private final int n;
  private final FixedLengthListFactory<Double> gaussianSamplesFactory;

  public OpenAIEvolutionaryStrategy(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      int batchSize,
      Predicate<? super State<S, Q>> stopCondition,
      double sigma
  ) {
    super(solutionMapper, genotypeFactory, 2 * batchSize, stopCondition);
    this.batchSize = batchSize;
    this.sigma = sigma;
    n = genotypeFactory.build(1, new Random(0)).get(0).size();
    gaussianSamplesFactory = new FixedLengthListFactory<>(n, RandomGenerator::nextGaussian);
  }

  public static class State<S, Q> extends POSetPopulationState<List<Double>, S, Q> {
    private final boolean wDecay = false;
    private final double stepSize = 0.02;
    private final double beta1 = 0.9;
    private final double beta2 = 0.999;
    private final double epsilon = 1e-08;
    protected double[] center;
    private List<List<Double>> samples;
    private List<Integer> indexes;  // indexes of fitness in reversed order
    private double[] m;
    private double[] v;

    public State(int n) {
      center = new double[n];
      m = new double[n];
      v = new double[n];
    }

    protected State(
        LocalDateTime startingDateTime,
        long elapsedMillis,
        long nOfIterations,
        Progress progress,
        long nOfBirths,
        long nOfFitnessEvaluations,
        PartiallyOrderedCollection<Individual<List<Double>, S, Q>> population,
        double[] center,
        List<List<Double>> samples,
        List<Integer> indexes,
        double[] m,
        double[] v
    ) {
      super(startingDateTime, elapsedMillis, nOfIterations, progress, nOfBirths, nOfFitnessEvaluations, population);
      this.center = center;
      this.samples = samples;
      this.indexes = indexes;
      this.m = m;
      this.v = v;
    }

    @Override
    public State<S, Q> immutableCopy() {
      return new State<>(
          startingDateTime,
          elapsedMillis,
          nOfIterations,
          progress,
          nOfBirths,
          nOfFitnessEvaluations,
          population.immutableCopy(),
          Arrays.copyOf(center, center.length),
          new ArrayList<>(samples),
          new ArrayList<>(indexes),
          Arrays.copyOf(m, m.length),
          Arrays.copyOf(v, v.length)
      );
    }
  }

  @Override
  protected State<S, Q> initState(
      TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor
  ) {
    return new State<>(n);
  }

  @Override
  public State<S, Q> init(
      TotalOrderQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor
  ) throws SolverException {
    State<S, Q> state = super.init(problem, random, executor);

    List<List<Double>> genotypes = state.getPopulation().all().stream().map(Individual::genotype).toList();
    state.center = IntStream.range(0, n)
        .mapToDouble(i -> genotypes.stream().mapToDouble(genotype -> genotype.get(i)).sum() / genotypes.size())
        .toArray();
    return state;
  }

  private void optimize(State<S, Q> state) {
    double[] utilities = new double[populationSize];
    IntStream.range(0, populationSize)
        .forEach(i -> utilities[state.indexes.get(i)] = (double) i / (populationSize - 1) - 0.5);
    List<Double> weights = IntStream.range(0, batchSize)
        .mapToObj(i -> utilities[2 * i] - utilities[2 * i + 1])
        .toList();

    double[] g = IntStream.range(0, n)
        .mapToDouble(i -> IntStream.range(0, batchSize)
            .mapToDouble(j -> weights.get(j) * state.samples.get(j).get(i)).sum() / populationSize)
        .toArray();

    double[] globalG = state.wDecay ?
        IntStream.range(0, n).mapToDouble(i -> -g[i] + 0.005 * state.center[i]).toArray() :
        Arrays.stream(g).map(i -> -i).toArray();

    double a = state.stepSize * Math.sqrt(1d - Math.pow(
        state.beta2,
        state.getNOfIterations()
    )) / (1d - Math.pow(state.beta1, state.getNOfIterations()));
    state.m = IntStream.range(0, n)
        .mapToDouble(i -> state.beta1 * state.m[i] + (1d - state.beta1 * globalG[i]))
        .toArray();
    state.v = IntStream.range(0, n)
        .mapToDouble(i -> state.beta2 * state.v[i] + (1d - state.beta2 * globalG[i] * globalG[i]))
        .toArray();
    double[] dCenter = IntStream.range(0, n)
        .mapToDouble(i -> -a * state.m[i] / (Math.sqrt(state.v[i]) + state.epsilon))
        .toArray();
    state.center = IntStream.range(0, n).mapToDouble(i -> state.center[i] + dCenter[i]).toArray();
  }

  @Override
  public void update(
      TotalOrderQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor,
      State<S, Q> state
  ) throws SolverException {

    // create samples
    state.samples = IntStream.range(0, batchSize).mapToObj(i -> gaussianSamplesFactory.build(random)).toList();
    List<List<Double>> genotypes = new ArrayList<>();
    state.samples.forEach(sample -> {
      genotypes.add(IntStream.range(0, n).mapToObj(i -> state.center[i] + sample.get(i) * sigma).toList());
      genotypes.add(IntStream.range(0, n).mapToObj(i -> state.center[i] - sample.get(i) * sigma).toList());
    });

    // individuals and their fitness
    List<Individual<List<Double>, S, Q>> individuals = map(
        genotypes,
        List.of(),
        solutionMapper,
        problem.qualityFunction(),
        executor,
        state
    );

    // sort individuals and compute indexes
    Comparator<Integer> integerComparator = comparator(problem).reversed().comparing(individuals::get).comparator();
    state.indexes = IntStream.range(0, populationSize).boxed().sorted(integerComparator).toList();

    //update state
    state.setPopulation(new DAGPartiallyOrderedCollection<>(individuals, comparator(problem)));
    state.incNOfIterations();
    state.updateElapsedMillis();

    // perform optimization
    optimize(state);
  }

}
