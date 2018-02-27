/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.ranker;

import it.units.malelab.jgea.core.Individual;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class ParetoRanker<G, S> implements Ranker<Individual<G, S, List<Double>>>, Comparator<List<Double>> {

  private final ComparableRanker<Individual<G, S, List<Double>>> innerRanker;

  public ParetoRanker() {
    this.innerRanker = new ComparableRanker<>(
            (i1, i2) -> Double.compare(i1.getFitness().get(0), i2.getFitness().get(0))
    );
  }

  @Override
  public <K extends Individual<G, S, List<Double>>> List<Collection<K>> rank(Collection<K> individuals, Random random) {
    //check if can use inner ranker
    if (individuals.stream().findFirst().get().getFitness().size()==1) {
      return innerRanker.rank(individuals, random);
    }
    //rank using pareto dominance
    List<Collection<K>> ranks = new ArrayList<>();
    List<K> localIndividuals = new ArrayList<>(individuals);
    while (!localIndividuals.isEmpty()) {
      int[] counts = dominanceCounts(localIndividuals);
      List<K> paretoFront = new ArrayList<>();
      for (int i = 0; i < counts.length; i++) {
        if (counts[i] == 0) {
          paretoFront.add(localIndividuals.get(i));
        }
      }
      localIndividuals.removeAll(paretoFront);
      ranks.add(paretoFront);
    }
    return ranks;
  }

  private <K extends Individual<G, S, List<Double>>> int[] dominanceCounts(List<K> individuals) {
    int[] counts = new int[individuals.size()];
    for (int i = 0; i < individuals.size(); i++) {
      for (int j = i + 1; j < individuals.size(); j++) {
        int outcome = compare(individuals.get(i).getFitness(), individuals.get(j).getFitness());
        if (outcome < 0) {
          counts[j] = counts[j] + 1;
        } else if (outcome > 0) {
          counts[i] = counts[i] + 1;
        }
      }
    }
    return counts;
  }

  @Override
  public int compare(List<Double> f1, List<Double> f2) {
    int better = 0;
    int worse = 0;
    for (int i = 0; i < f1.size(); i++) {
      int outcome = f1.get(i).compareTo(f2.get(i));
      better = better + ((outcome < 0) ? 1 : 0);
      worse = worse + ((outcome > 0) ? 1 : 0);
    }
    if (better > 0 && worse == 0) {
      return -1;
    }
    if (worse > 0 && better == 0) {
      return 1;
    }
    return 0;
  }

}
