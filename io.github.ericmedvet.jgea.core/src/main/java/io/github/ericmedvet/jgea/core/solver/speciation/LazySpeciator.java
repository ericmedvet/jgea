
package io.github.ericmedvet.jgea.core.solver.speciation;

import io.github.ericmedvet.jgea.core.distance.Distance;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.solver.Individual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
public class LazySpeciator<G, S, F> implements SpeciatedEvolver.Speciator<Individual<G, S, F>> {
  private final Distance<Individual<G, S, F>> distance;
  private final double distanceThreshold;

  public LazySpeciator(Distance<Individual<G, S, F>> distance, double distanceThreshold) {
    this.distance = distance;
    this.distanceThreshold = distanceThreshold;
  }

  @Override
  public Collection<SpeciatedEvolver.Species<Individual<G, S, F>>> speciate(
      PartiallyOrderedCollection<Individual<G, S, F>> population
  ) {
    List<List<Individual<G, S, F>>> clusters = new ArrayList<>();
    for (Individual<G, S, F> individual : population.all()) {
      List<Double> distances = clusters.stream().map(c -> distance.apply(individual, c.get(0))).toList();
      if (distances.isEmpty()) {
        List<Individual<G, S, F>> cluster = new ArrayList<>();
        cluster.add(individual);
        clusters.add(cluster);
      } else {
        int closestIndex = 0;
        for (int i = 1; i < distances.size(); i++) {
          if (distances.get(i) < distances.get(closestIndex)) {
            closestIndex = i;
          }
        }
        if (distances.get(closestIndex) < distanceThreshold) {
          clusters.get(closestIndex).add(individual);
        } else {
          List<Individual<G, S, F>> cluster = new ArrayList<>();
          cluster.add(individual);
          clusters.add(cluster);
        }
      }
    }
    return clusters.stream().map(c -> new SpeciatedEvolver.Species<>(c, c.get(0))).toList();
  }

}
