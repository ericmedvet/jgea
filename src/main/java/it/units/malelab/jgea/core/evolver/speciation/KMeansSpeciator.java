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
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author federico
 */
public class KMeansSpeciator<G, S, F> implements Speciator<Individual<G, S, F>> {
    private final KMeansPlusPlusClusterer<Clusterable> kmeans;
    private final Function<Individual<G, S, F>, Clusterable> converter;

    public KMeansSpeciator(KMeansPlusPlusClusterer<Clusterable> kmeans, Function<Individual<G, S, F>, Clusterable> converter) {
        this.kmeans = kmeans;
        this.converter = converter;
    }

    @Override
    public Collection<Species<Individual<G, S, F>>> speciate(PartiallyOrderedCollection<Individual<G, S, F>> population) {
        Map<Clusterable, Individual<G, S, F>> fromClusterableToIndividual = new HashMap<>();
        for (Individual<G, S, F> individual : population.all()) {
            fromClusterableToIndividual.put(converter.apply(individual), individual);
        }
        Collection<CentroidCluster<Clusterable>> clusteringOutput = kmeans.cluster(fromClusterableToIndividual.keySet());
        List<Species<Individual<G, S, F>>> allSpecies = new ArrayList<>();
        for (CentroidCluster<Clusterable> c : clusteringOutput) {
            allSpecies.add(new Species<Individual<G, S, F>>(c.getPoints().stream().map(fromClusterableToIndividual::get).collect(Collectors.toList()),
                    individuals -> fromClusterableToIndividual.get(c.getCenter())));
        }
        return allSpecies;
    }

}
