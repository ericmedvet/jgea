/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
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
package io.github.ericmedvet.jgea.core.solver.mapelites;

import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MapOfElites<T> implements PartiallyOrderedCollection<T> {

  public static class Feature {
    private final int nOfBins;
    private final double step;

    private Feature(double min, double max, int nOfBins) {
      this.nOfBins = nOfBins;
      step = (max - min) / nOfBins;
    }

    private int assignToBin(double value) {
      return (int) Math.min(nOfBins - 1, Math.max(0, value / step));
    }
  }

  private final HashMap<List<Integer>, T> elites;
  private final Function<T, List<Double>> featuresExtractor;
  private final List<Feature> features;
  private final PartialComparator<? super T> comparator;
  private List<T> lastAddedPerformance;
  private int notStoredSolutionsCounter = 0;
  private int updatedSolutionsCounter = 0;

  public MapOfElites(
      List<Feature> features,
      Function<T, List<Double>> featuresExtractor,
      PartialComparator<? super T> comparator) {
    elites = new HashMap<>();
    this.featuresExtractor = featuresExtractor;
    this.comparator = comparator;
    this.features = features;
  }

  public MapOfElites(
      List<Integer> featuresSizes,
      List<Double> featuresMinValues,
      List<Double> featuresMaxValues,
      Function<T, List<Double>> featuresExtractor,
      PartialComparator<? super T> comparator) {
    this(
        buildFeatures(featuresSizes, featuresMinValues, featuresMaxValues),
        featuresExtractor,
        comparator);
  }

  public static List<Feature> buildFeatures(
      List<Integer> featuresSizes, List<Double> featuresMinValues, List<Double> featuresMaxValues) {
    int nFeatures = featuresSizes.size();
    if (nFeatures != featuresMinValues.size() || nFeatures != featuresMaxValues.size()) {
      throw new IllegalArgumentException(
          "Conflicting number of features values: all lists must have the same length");
    }
    return IntStream.range(0, nFeatures)
        .mapToObj(
            i ->
                new Feature(
                    featuresMinValues.get(i), featuresMaxValues.get(i), featuresSizes.get(i)))
        .collect(Collectors.toList());
  }

  public MapOfElites<T> copy() {
    MapOfElites<T> newMapOfElites = new MapOfElites<>(features, featuresExtractor, comparator);
    newMapOfElites.addAll(all());
    return newMapOfElites;
  }

  private List<Integer> computeBelongingElite(List<Double> individualsFeatures) {
    return IntStream.range(0, individualsFeatures.size())
        .mapToObj(i -> features.get(i).assignToBin(individualsFeatures.get(i)))
        .collect(Collectors.toList());
  }

  private List<Integer> computeBelongingElite(T individual) {
    return computeBelongingElite(featuresExtractor.apply(individual));
  }

  @Override
  public Collection<T> all() {
    return Collections.unmodifiableCollection(elites.values());
  }

  @Override
  public Collection<T> firsts() {
    return all();
  }

  @Override
  public Collection<T> lasts() {
    return all();
  }

  @Override
  public boolean remove(T t) {
    return elites.remove(computeBelongingElite(t), t);
  }

  @Override
  public void add(T t) {
    List<Integer> elite = computeBelongingElite(t);
    T previousT = elites.get(elite);
    if (previousT == null) {
      elites.put(elite, t);
      lastAddedPerformance.add(t);
    } else {
      if (comparator
          .compare(t, previousT)
          .equals(PartialComparator.PartialComparatorOutcome.BEFORE)) {
        elites.put(elite, t);
        lastAddedPerformance.add(t);
        updatedSolutionsCounter++;
      } else {
        notStoredSolutionsCounter++;
      }
    }
  }

  public void addAll(Collection<T> individuals) {
    lastAddedPerformance = new ArrayList<>();
    individuals.forEach(this::add);
  }

  public List<T> getLastAddedPerformance() {
    return lastAddedPerformance;
  }

  public int getNotStoredSolutionsCounter() {
    return notStoredSolutionsCounter;
  }

  public int getUpdatedSolutionsCounter() {
    return updatedSolutionsCounter;
  }
}
