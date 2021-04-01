package it.units.malelab.jgea.core.evolver;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author eric on 2021/01/14 for jgea
 */
public class BasicEvolutionaryStrategy<S, F> extends AbstractIterativeEvolver<List<Double>, S, F> {

  protected static class ESState extends State {
    private final List<Double> means;

    public ESState(int iterations, int births, int fitnessEvaluations, long elapsedMillis) {
      super(iterations, births, fitnessEvaluations, elapsedMillis);
      this.means = new ArrayList<>();
    }

    public List<Double> getMeans() {
      return means;
    }
  }

  private final double sigma;
  private final int populationSize;
  private final int parentsSize;
  private final int eliteSize;
  private final boolean remap;

  public BasicEvolutionaryStrategy(Function<? super List<Double>, ? extends S> solutionMapper, Factory<? extends List<Double>> genotypeFactory, PartialComparator<? super Individual<List<Double>, S, F>> individualComparator, double sigma, int populationSize, int parentsSize, int eliteSize, boolean remap) {
    super(solutionMapper, genotypeFactory, individualComparator);
    this.sigma = sigma;
    this.populationSize = populationSize;
    this.parentsSize = parentsSize;
    this.eliteSize = eliteSize;
    this.remap = remap;
  }

  @Override
  protected Collection<Individual<List<Double>, S, F>> initPopulation(Function<S, F> fitnessFunction, Random random, ExecutorService executor, State state) throws ExecutionException, InterruptedException {
    return initPopulation(populationSize, fitnessFunction, random, executor, state);
  }

  @Override
  protected Collection<Individual<List<Double>, S, F>> updatePopulation(PartiallyOrderedCollection<Individual<List<Double>, S, F>> orderedPopulation, Function<S, F> fitnessFunction, Random random, ExecutorService executor, State state) throws ExecutionException, InterruptedException {
    List<Individual<List<Double>, S, F>> all = new ArrayList<>(orderedPopulation.all());
    List<Individual<List<Double>, S, F>> parents = new ArrayList<>();
    List<Individual<List<Double>, S, F>> elite = new ArrayList<>();
    //extract parents
    while (parents.size() < parentsSize) {
      Individual<List<Double>, S, F> best = all.stream()
          .reduce((i1, i2) -> individualComparator.compare(i1, i2).equals(PartialComparator.PartialComparatorOutcome.BEFORE) ? i1 : i2)
          .orElse(all.get(0));
      all.remove(best);
      parents.add(best);
      if (elite.size() < eliteSize) {
        elite.add(best);
      }
    }
    //update mean
    if (parents.stream().map(i -> i.getGenotype().size()).distinct().count() > 1) {
      throw new IllegalStateException(String.format(
          "Genotype size should be the same for all parents: found different sizes %s",
          parents.stream().map(i -> i.getGenotype().size()).distinct().collect(Collectors.toList())
      ));
    }
    int l = parents.get(0).getGenotype().size();
    final double[] sums = new double[l];
    ESState s = ((ESState) state);
    parents.forEach(i -> IntStream.range(0, l).forEach(j -> sums[j] = sums[j] + i.getGenotype().get(j)));
    s.getMeans().clear();
    Arrays.stream(sums).forEach(v -> s.getMeans().add(v / (double) parents.size()));
    //build offspring
    List<List<Double>> offspringGenotypes = new ArrayList<>();
    while (offspringGenotypes.size() < populationSize - eliteSize) {
      offspringGenotypes.add(s.getMeans().stream()
          .map(m -> m + random.nextGaussian() * sigma)
          .collect(Collectors.toList()));
    }
    List<Individual<List<Double>, S, F>> offspring = new ArrayList<>();
    if (remap) {
      offspring.addAll(map(offspringGenotypes, elite, solutionMapper, fitnessFunction, executor, state));
    } else {
      offspring.addAll(elite);
      offspring.addAll(map(offspringGenotypes, List.of(), solutionMapper, fitnessFunction, executor, state));
    }
    return offspring;
  }

  @Override
  protected State initState() {
    State state = super.initState();
    return new ESState(state.getIterations(), state.getBirths(), state.getFitnessEvaluations(), state.getElapsedMillis());
  }
}
