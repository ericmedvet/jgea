/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.problem.mapper;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.representation.sequence.bit.BitString;
import it.units.malelab.jgea.representation.sequence.bit.BitStringFactory;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.representation.sequence.bit.BitFlipMutation;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.core.util.WithNames;
import it.units.malelab.jgea.distance.Distance;
import it.units.malelab.jgea.distance.Hamming;
import it.units.malelab.jgea.problem.mapper.element.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 * @author eric
 */
public class FitnessFunction implements
    Function<Pair<Node<Element>, Node<Element>>, List<Double>>,
    WithNames {

  private final static int EXPRESSIVENESS_DEPTH = 2;

  public enum Property {
    DEGENERACY, NON_UNIFORMITY, NON_LOCALITY
  }

  private final List<EnhancedProblem> problems;
  private final int maxMappingDepth;
  private final List<Property> properties;

  private final List<BitString> genotypes;
  private final double[] genotypeDistances;

  public FitnessFunction(List<EnhancedProblem> problems, int genotypeSize, int n, int maxMappingDepth, List<Property> properties, long seed) {
    this.problems = problems;
    this.maxMappingDepth = maxMappingDepth;
    this.properties = properties;
    Random random = new Random(seed);
    //build genotypes
    GeneticOperator<BitString> mutation = new BitFlipMutation(0.01d);
    BitStringFactory factory = new BitStringFactory(genotypeSize);
    Set<BitString> set = new LinkedHashSet<>();
    for (int i = 0; i < Math.floor(Math.sqrt(n)); i++) {
      set.addAll(consecutiveMutations(factory.build(random), (int) Math.floor(Math.sqrt(n)), mutation, random));
    }
    while (set.size() < n) {
      set.add(factory.build(random));
    }
    genotypes = new ArrayList<>(set);
    //compute distances
    genotypeDistances = computeDistances(genotypes, new Hamming<>());
  }

  private List<BitString> consecutiveMutations(BitString g, int n, GeneticOperator<BitString> mutation, Random random) {
    Set<BitString> set = new LinkedHashSet<>();
    while (set.size() < n) {
      set.add(g);
      g = mutation.apply(Collections.singletonList(g), random).get(0);
    }
    return new ArrayList<>(set);
  }

  @Override
  public List<Double> apply(Pair<Node<Element>, Node<Element>> pair) {
    List<List<Double>> valuesLists = new ArrayList<>();
    for (EnhancedProblem problem : problems) {
      List<Double> localValues = apply(pair, problem);
      if (valuesLists.isEmpty()) {
        localValues.forEach(v -> {
          List<Double> valuesList = new ArrayList<>(problems.size());
          valuesLists.add(valuesList);
        });
      }
      for (int i = 0; i < localValues.size(); i++) {
        valuesLists.get(i).add(localValues.get(i));
      }
    }
    return valuesLists.stream()
        .map(valuesList -> valuesList.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN))
        .collect(Collectors.toList());
  }

  protected <N, S, F> List<Double> apply(Pair<Node<Element>, Node<Element>> pair, EnhancedProblem<N, S, F> problem) {
    //build mapper
    RecursiveMapper<N> recursiveMapper = new RecursiveMapper<>(
        pair.first(),
        pair.second(),
        maxMappingDepth,
        EXPRESSIVENESS_DEPTH,
        problem.getProblem().getGrammar());
    //map
    List<S> solutions = genotypes.stream()
        .map(g -> recursiveMapper.apply(g))
        .map(t -> problem.getProblem().getSolutionMapper().apply(t))
        .collect(Collectors.toList());
    Multiset<S> multiset = LinkedHashMultiset.create();
    multiset.addAll(solutions);
    //compute properties
    List<Double> values = new ArrayList<>();
    for (Property property : properties) {
      if (property.equals(Property.DEGENERACY)) {
        values.add(1d - (double) multiset.elementSet().size() / (double) genotypes.size());
      } else if (property.equals(Property.NON_UNIFORMITY)) {
        double[] sizes = multiset.entrySet().stream().mapToDouble(e -> (double) e.getCount()).toArray();
        values.add(Math.sqrt(StatUtils.variance(sizes)) / StatUtils.mean(sizes));
      } else if (property.equals(Property.NON_LOCALITY)) {
        double[] solutionDistances = computeDistances(solutions, problem.getDistance());
        double locality = 1d - (1d + (new PearsonsCorrelation().correlation(genotypeDistances, solutionDistances))) / 2d;
        values.add(Double.isNaN(locality) ? 1d : locality);
      } else {
        values.add(0d);
      }
    }
    return values;
  }

  private <E> double[] computeDistances(List<E> elements, Distance<? super E> distance) {
    double[] dists = new double[elements.size() * (elements.size() - 1) / 2];
    int c = 0;
    for (int i = 0; i < elements.size() - 1; i++) {
      for (int j = i + 1; j < elements.size(); j++) {
        dists[c] = distance.apply(elements.get(i), elements.get(j));
        c = c + 1;
      }
    }
    return dists;
  }

  @Override
  public List<String> names() {
    return properties.stream().map(p -> p.toString().toLowerCase().replace("_", ".")).collect(Collectors.toList());
  }

}
