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

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.operator.GeneticOperator;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.MultiHomogeneousObjectiveProblem;
import io.github.ericmedvet.jgea.core.util.Misc;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

// source -> https://doi.org/10.1109/4235.996017

public class NsgaII<G, S>
    extends AbstractPopulationBasedIterativeSolver<
        POCPopulationState<
            Individual<G, S, List<Double>>,
            G,
            S,
            List<Double>,
            MultiHomogeneousObjectiveProblem<S, Double>>,
        MultiHomogeneousObjectiveProblem<S, Double>,
        Individual<G, S, List<Double>>,
        G,
        S,
        List<Double>> {

  protected final Map<GeneticOperator<G>, Double> operators;
  private final int populationSize;
  private final int maxUniquenessAttempts;

  public NsgaII(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      int populationSize,
      Predicate<
              ? super
                  POCPopulationState<
                      Individual<G, S, List<Double>>,
                      G,
                      S,
                      List<Double>,
                      MultiHomogeneousObjectiveProblem<S, Double>>>
          stopCondition,
      Map<GeneticOperator<G>, Double> operators,
      int maxUniquenessAttempts,
      boolean remap) {
    super(solutionMapper, genotypeFactory, stopCondition, remap);
    this.operators = operators;
    this.populationSize = populationSize;
    this.maxUniquenessAttempts = maxUniquenessAttempts;
  }

  private record RankedIndividual<G, S>(
      long id,
      G genotype,
      S solution,
      List<Double> quality,
      long qualityMappingIteration,
      long genotypeBirthIteration,
      int rank,
      double crowdingDistance,
      Collection<Long> parentIds)
      implements Individual<G, S, List<Double>> {
    static <G, S> RankedIndividual<G, S> from(Individual<G, S, List<Double>> individual) {
      return new RankedIndividual<>(
          individual.id(),
          individual.genotype(),
          individual.solution(),
          individual.quality(),
          individual.qualityMappingIteration(),
          individual.genotypeBirthIteration(),
          0,
          0,
          individual.parentIds());
    }
  }

  private static <G, S> Comparator<RankedIndividual<G, S>> rankedComparator() {
    return Comparator.comparingInt((RankedIndividual<G, S> i) -> i.rank)
        .thenComparing((i1, i2) -> Double.compare(i2.crowdingDistance, i1.crowdingDistance));
  }

  private static <G, S> List<Double> distances(
      List<? extends Individual<G, S, List<Double>>> individuals, List<Comparator<Double>> comparators) {
    double[] dists = new double[individuals.size()];
    for (int oI = 0; oI < comparators.size(); oI = oI + 1) {
      int finalOI = oI;
      List<Integer> indexes = IntStream.range(0, individuals.size())
          .boxed()
          .sorted((ii1, ii2) -> comparators
              .get(finalOI)
              .compare(
                  individuals.get(ii1).quality().get(finalOI),
                  individuals.get(ii2).quality().get(finalOI)))
          .toList();
      for (int ii = 1; ii < indexes.size() - 1; ii = ii + 1) {
        int previousIndex = indexes.get(ii - 1);
        int nextIndex = indexes.get(ii + 1);
        double dist = Math.abs(individuals.get(previousIndex).quality().get(finalOI)
            - individuals.get(nextIndex).quality().get(finalOI));
        dists[indexes.get(ii)] = dists[indexes.get(ii)] + dist;
      }
      dists[indexes.get(0)] = dists[indexes.get(0)] + Double.POSITIVE_INFINITY;
      dists[indexes.get(indexes.size() - 1)] = dists[indexes.get(indexes.size() - 1)] + Double.POSITIVE_INFINITY;
    }
    return Arrays.stream(dists).boxed().toList();
  }

  private static <G, S> Collection<RankedIndividual<G, S>> decorate(
      Collection<? extends Individual<G, S, List<Double>>> individuals,
      MultiHomogeneousObjectiveProblem<S, Double> problem) {
    List<? extends Collection<? extends Individual<G, S, List<Double>>>> fronts = PartiallyOrderedCollection.from(
            individuals, partialComparator(problem))
        .fronts();
    return IntStream.range(0, fronts.size())
        .mapToObj(fi -> {
          List<? extends Individual<G, S, List<Double>>> is =
              fronts.get(fi).stream().toList();
          List<Double> distances = distances(is, problem.comparators());
          return IntStream.range(0, is.size())
              .mapToObj(ii -> {
                Individual<G, S, List<Double>> individual = is.get(ii);
                return new RankedIndividual<>(
                    individual.id(),
                    individual.genotype(),
                    individual.solution(),
                    individual.quality(),
                    individual.qualityMappingIteration(),
                    individual.genotypeBirthIteration(),
                    fi,
                    distances.get(ii),
                    individual.parentIds());
              })
              .toList();
        })
        .flatMap(List::stream)
        .toList();
  }

  @Override
  public POCPopulationState<
          Individual<G, S, List<Double>>, G, S, List<Double>, MultiHomogeneousObjectiveProblem<S, Double>>
      init(MultiHomogeneousObjectiveProblem<S, Double> problem, RandomGenerator random, ExecutorService executor)
          throws SolverException {
    POCPopulationState<
            Individual<G, S, List<Double>>, G, S, List<Double>, MultiHomogeneousObjectiveProblem<S, Double>>
        newState = POCPopulationState.empty(problem, stopCondition());
    AtomicLong counter = new AtomicLong(0);
    List<? extends G> genotypes = genotypeFactory.build(populationSize, random);
    Collection<Individual<G, S, List<Double>>> individuals = getAll(map(
        genotypes.stream()
            .map(g -> new ChildGenotype<G>(counter.getAndIncrement(), g, List.of()))
            .toList(),
        (cg, s, r) -> RankedIndividual.from(
            Individual.from(cg, solutionMapper, s.problem().qualityFunction(), s.nOfIterations())),
        newState,
        random,
        executor));
    return newState.updatedWithIteration(
        genotypes.size(),
        genotypes.size(),
        PartiallyOrderedCollection.from(individuals, partialComparator(problem)));
  }

  @Override
  public POCPopulationState<
          Individual<G, S, List<Double>>, G, S, List<Double>, MultiHomogeneousObjectiveProblem<S, Double>>
      update(
          RandomGenerator random,
          ExecutorService executor,
          POCPopulationState<
                  Individual<G, S, List<Double>>,
                  G,
                  S,
                  List<Double>,
                  MultiHomogeneousObjectiveProblem<S, Double>>
              state)
          throws SolverException {
    // build offspring
    Collection<ChildGenotype<G>> offspringChildGenotypes = new ArrayList<>();
    Set<G> uniqueOffspringGenotypes = new HashSet<>();
    if (maxUniquenessAttempts > 0) {
      uniqueOffspringGenotypes.addAll(state.pocPopulation().all().stream()
          .map(Individual::genotype)
          .toList());
    }
    int attempts = 0;
    List<Individual<G, S, List<Double>>> individuals =
        state.pocPopulation().all().stream().toList();
    int size = individuals.size();
    AtomicLong counter = new AtomicLong(state.nOfBirths());
    while (offspringChildGenotypes.size() < populationSize) {
      GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
      List<Individual<G, S, List<Double>>> parents = IntStream.range(0, operator.arity())
          .mapToObj(n -> individuals.get(Math.min(random.nextInt(size), random.nextInt(size))))
          .toList();
      List<? extends G> newGenotypes =
          operator.apply(parents.stream().map(Individual::genotype).toList(), random);
      if (attempts >= maxUniquenessAttempts
          || newGenotypes.stream().noneMatch(uniqueOffspringGenotypes::contains)) {
        attempts = 0;
        List<Long> parentIds = parents.stream().map(Individual::id).toList();
        newGenotypes.forEach(
            g -> offspringChildGenotypes.add(new ChildGenotype<>(counter.getAndIncrement(), g, parentIds)));
        uniqueOffspringGenotypes.addAll(newGenotypes);
      } else {
        attempts = attempts + 1;
      }
    }
    // map and decorate and trim
    Collection<Individual<G, S, List<Double>>> newPopulation = mapAll(
        offspringChildGenotypes,
        (cg, s, r) -> RankedIndividual.from(
            Individual.from(cg, solutionMapper, s.problem().qualityFunction(), s.nOfIterations())),
        state.pocPopulation().all(),
        (i, s, r) -> RankedIndividual.from(i.updatedWithQuality(s)),
        state,
        random,
        executor);
    List<RankedIndividual<G, S>> rankedIndividuals = decorate(newPopulation, state.problem()).stream()
        .sorted(rankedComparator())
        .limit(populationSize)
        .toList();
    @SuppressWarnings({"unchecked", "rawtypes"})
    List<Individual<G, S, List<Double>>> newIndividuals = (List) rankedIndividuals;
    return state.updatedWithIteration(
        offspringChildGenotypes.size(),
        remap ? populationSize : 0,
        PartiallyOrderedCollection.from(newIndividuals, partialComparator(state.problem())));
  }
}
