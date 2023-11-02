/*-
 * ========================LICENSE_START=================================
 * jgea-problem
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

package io.github.ericmedvet.jgea.problem.extraction;

import io.github.ericmedvet.jgea.core.representation.graph.finiteautomata.Extractor;
import io.github.ericmedvet.jgea.core.util.IntRange;
import java.util.*;
import java.util.function.Function;

public class ExtractionFitness<S> implements Function<Extractor<S>, List<Double>> {

  private final Aggregator<S> aggregator;

  public ExtractionFitness(List<S> sequence, Set<IntRange> desiredExtractions, Metric... metrics) {
    aggregator = new Aggregator<S>(sequence, desiredExtractions, metrics);
  }

  public enum Metric {
    ONE_MINUS_PREC,
    ONE_MINUS_REC,
    ONE_MINUS_FM,
    SYMBOL_FNR,
    SYMBOL_FPR,
    SYMBOL_ERROR,
    SYMBOL_WEIGHTED_ERROR;
  }

  private static class Aggregator<S> implements Function<Set<IntRange>, List<Double>> {

    private final List<S> sequence;
    private final Set<IntRange> desiredExtractions;
    private final List<Metric> metrics;
    private final BitSet desiredExtractionMask;
    private final int positiveSymbols;

    public Aggregator(List<S> sequence, Set<IntRange> desiredExtractions, Metric... metrics) {
      this.sequence = sequence;
      this.desiredExtractions = desiredExtractions;
      this.metrics = Arrays.asList(metrics);
      desiredExtractionMask = buildMask(desiredExtractions, sequence.size());
      positiveSymbols = desiredExtractionMask.cardinality();
    }

    @Override
    public List<Double> apply(Set<IntRange> extractions) {
      Map<Metric, Double> values = new EnumMap<>(Metric.class);
      if (metrics.contains(Metric.ONE_MINUS_FM)
          || metrics.contains(Metric.ONE_MINUS_PREC)
          || metrics.contains(Metric.ONE_MINUS_REC)) {
        // precision and recall
        Set<IntRange> correctExtractions = new LinkedHashSet<>(extractions);
        correctExtractions.retainAll(desiredExtractions);
        double recall = (double) correctExtractions.size() / (double) desiredExtractions.size();
        double precision = (double) correctExtractions.size() / (double) extractions.size();
        double fMeasure = 2d * precision * recall / (precision + recall);
        values.put(Metric.ONE_MINUS_PREC, 1 - precision);
        values.put(Metric.ONE_MINUS_REC, 1 - recall);
        values.put(Metric.ONE_MINUS_FM, 1 - fMeasure);
      }
      if (metrics.contains(Metric.SYMBOL_ERROR)
          || metrics.contains(Metric.SYMBOL_FNR)
          || metrics.contains(Metric.SYMBOL_FPR)
          || metrics.contains(Metric.SYMBOL_WEIGHTED_ERROR)) {
        BitSet extractionMask = buildMask(extractions, sequence.size());
        int extractedSymbols = extractionMask.cardinality();
        extractionMask.and(desiredExtractionMask);
        double truePositiveSymbols = extractionMask.cardinality();
        double falseNegativeSymbols = positiveSymbols - truePositiveSymbols;
        double falsePositiveSymbols = extractedSymbols - truePositiveSymbols;
        double trueNegativeChars = desiredExtractionMask.length()
            - falsePositiveSymbols
            - truePositiveSymbols
            - falseNegativeSymbols;
        values.put(Metric.SYMBOL_FPR, falsePositiveSymbols / (trueNegativeChars + falsePositiveSymbols));
        values.put(Metric.SYMBOL_FNR, falseNegativeSymbols / (truePositiveSymbols + falseNegativeSymbols));
        values.put(
            Metric.SYMBOL_ERROR, (falsePositiveSymbols + falseNegativeSymbols) / (double) sequence.size());
        values.put(
            Metric.SYMBOL_WEIGHTED_ERROR,
            (falsePositiveSymbols / (trueNegativeChars + falsePositiveSymbols)
                    + falseNegativeSymbols / (truePositiveSymbols + falseNegativeSymbols))
                / 2d);
      }
      List<Double> results = new ArrayList<>(metrics.size());
      for (Metric metric : metrics) {
        results.add(values.get(metric));
      }
      return results;
    }

    public Set<IntRange> getDesiredExtractions() {
      return desiredExtractions;
    }

    public List<Metric> getMetrics() {
      return metrics;
    }

    public List<S> getSequence() {
      return sequence;
    }

    private Set<IntRange> intersections(IntRange range, Set<IntRange> others) {
      Set<IntRange> intersections = new HashSet<>();
      for (IntRange other : others) {
        if (range.overlaps(other)) {
          intersections.add(range.intersection(other).orElseThrow());
        }
      }
      return intersections;
    }
  }

  private static BitSet buildMask(Set<IntRange> extractions, int size) {
    BitSet bitSet = new BitSet(size);
    extractions.forEach(r -> bitSet.set(r.min(), r.max()));
    return bitSet;
  }

  @Override
  public List<Double> apply(Extractor<S> e) {
    return aggregator.apply(e.extractNonOverlapping(aggregator.sequence));
  }

  public ExtractionFitness<S> changeMetrics(Metric... metrics) {
    return new ExtractionFitness<>(aggregator.sequence, aggregator.desiredExtractions, metrics);
  }

  public Set<IntRange> getDesiredExtractions() {
    return aggregator.getDesiredExtractions();
  }

  public List<Metric> getMetrics() {
    return aggregator.getMetrics();
  }

  public List<S> getSequence() {
    return aggregator.sequence;
  }
}
