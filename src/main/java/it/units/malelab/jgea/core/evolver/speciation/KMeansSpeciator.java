/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
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

package it.units.malelab.jgea.core.evolver.speciation;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.distance.Distance;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author federico
 */
public class KMeansSpeciator<G, S, F> implements Speciator<Individual<G, S, F>> {

  private final KMeansPlusPlusClusterer<ClusterableIndividual> kMeans;
  private final Function<Individual<G, S, F>, double[]> converter;
  private final Distance<double[]> distance;

  private class ClusterableIndividual implements Clusterable {
    private final Individual<G, S, F> individual;
    private double[] point;

    public ClusterableIndividual(Individual<G, S, F> individual) {
      this.individual = individual;
    }

    @Override
    public double[] getPoint() {
      if (point == null) {
        point = converter.apply(individual);
      }
      return point;
    }
  }

  public KMeansSpeciator(int k, int maxIterations, Distance<double[]> distance, Function<Individual<G, S, F>, double[]> converter) {
    this.kMeans = new KMeansPlusPlusClusterer<>(
        k,
        maxIterations,
        (DistanceMeasure) distance::apply
    );
    this.converter = converter;
    this.distance = distance;
  }

  @Override
  public Collection<Species<Individual<G, S, F>>> speciate(PartiallyOrderedCollection<Individual<G, S, F>> population) {
    Collection<ClusterableIndividual> points = population.all().stream()
        .map(ClusterableIndividual::new)
        .collect(Collectors.toList());
    if (points.stream().mapToInt(p -> p.getPoint().length).distinct().count() != 1) {
      throw new RuntimeException("all points to be clustered must have same length");
    }
    List<CentroidCluster<ClusterableIndividual>> clusters = kMeans.cluster(points);
    List<ClusterableIndividual> representers = clusters.stream().map(c -> {
      ClusterableIndividual closest = c.getPoints().get(0);
      double closestD = distance.apply(closest.point, c.getCenter().getPoint());
      for (int i = 0; i < c.getPoints().size(); i++) {
        double d = distance.apply(c.getPoints().get(i).point, c.getCenter().getPoint());
        if (d < closestD) {
          closestD = d;
          closest = c.getPoints().get(i);
        }
      }
      return closest;
    }).collect(Collectors.toList());
    return IntStream.range(0, clusters.size())
        .mapToObj(i -> new Species<>(
            clusters.get(i).getPoints().stream()
                .map(ci -> ci.individual)
                .collect(Collectors.toList()),
            representers.get(i).individual
        )).collect(Collectors.toList());
  }

}
