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

package it.units.malelab.jgea.problem.extraction;

import com.google.common.collect.Range;
import it.units.malelab.jgea.core.util.WithNames;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class ExtractionFitness<S> implements Function<Extractor<S>, List<Double>>, WithNames {

  public enum Metric {

    ONE_MINUS_PREC,
    ONE_MINUS_REC,
    ONE_MINUS_FM,
    SYMBOL_FNR,
    SYMBOL_FPR,
    SYMBOL_ERROR,
    SYMBOL_WEIGHTED_ERROR;

  }

  private static class Aggregator<S> implements Function<Set<Range<Integer>>, List<Double>> {

    private final List<S> sequence;
    private final Set<Range<Integer>> desiredExtractions;
    private final List<Metric> metrics;
    private final BitSet desiredExtractionMask;
    private final int positiveSymbols;

    public Aggregator(List<S> sequence, Set<Range<Integer>> desiredExtractions, Metric... metrics) {
      this.sequence = sequence;
      this.desiredExtractions = desiredExtractions;
      this.metrics = Arrays.asList(metrics);
      desiredExtractionMask = buildMask(desiredExtractions, sequence.size());
      positiveSymbols = desiredExtractionMask.cardinality();
    }

    @Override
    public List<Double> apply(Set<Range<Integer>> extractions) {
      Map<Metric, Double> values = new EnumMap<>(Metric.class);
      if (metrics.contains(Metric.ONE_MINUS_FM) || metrics.contains(Metric.ONE_MINUS_PREC) || metrics.contains(Metric.ONE_MINUS_REC)) {
        //precision and recall
        Set<Range<Integer>> correctExtractions = new LinkedHashSet<>(extractions);
        correctExtractions.retainAll(desiredExtractions);
        double recall = (double) correctExtractions.size() / (double) desiredExtractions.size();
        double precision = (double) correctExtractions.size() / (double) extractions.size();
        double fMeasure = 2d * precision * recall / (precision + recall);
        values.put(Metric.ONE_MINUS_PREC, 1 - precision);
        values.put(Metric.ONE_MINUS_REC, 1 - recall);
        values.put(Metric.ONE_MINUS_FM, 1 - fMeasure);
      }
      if (metrics.contains(Metric.SYMBOL_ERROR) || metrics.contains(Metric.SYMBOL_FNR) || metrics.contains(Metric.SYMBOL_FPR) || metrics.contains(Metric.SYMBOL_WEIGHTED_ERROR)) {
        BitSet extractionMask = buildMask(extractions, sequence.size());
        int extractedSymbols = extractionMask.cardinality();
        extractionMask.and(desiredExtractionMask);
        double truePositiveSymbols = extractionMask.cardinality();
        double falseNegativeSymbols = positiveSymbols - truePositiveSymbols;
        double falsePositiveSymbols = extractedSymbols - truePositiveSymbols;
        double trueNegativeChars = desiredExtractionMask.length() - falsePositiveSymbols - truePositiveSymbols - falseNegativeSymbols;
        values.put(Metric.SYMBOL_FPR, falsePositiveSymbols / (trueNegativeChars + falsePositiveSymbols));
        values.put(Metric.SYMBOL_FNR, falseNegativeSymbols / (truePositiveSymbols + falseNegativeSymbols));
        values.put(Metric.SYMBOL_ERROR, (falsePositiveSymbols + falseNegativeSymbols) / (double) sequence.size());
        values.put(
            Metric.SYMBOL_WEIGHTED_ERROR,
            (falsePositiveSymbols / (trueNegativeChars + falsePositiveSymbols) + falseNegativeSymbols / (truePositiveSymbols + falseNegativeSymbols)) / 2d
        );
      }
      List<Double> results = new ArrayList<>(metrics.size());
      for (Metric metric : metrics) {
        results.add(values.get(metric));
      }
      return results;
    }

    private Set<Range<Integer>> intersections(Range<Integer> range, Set<Range<Integer>> others) {
      Set<Range<Integer>> intersections = new HashSet<>();
      for (Range<Integer> other : others) {
        if (range.isConnected(other)) {
          intersections.add(range.intersection(other));
        }
      }
      return intersections;
    }

    public List<S> getSequence() {
      return sequence;
    }

    public Set<Range<Integer>> getDesiredExtractions() {
      return desiredExtractions;
    }

    public List<Metric> getMetrics() {
      return metrics;
    }

  }

  private final Aggregator<S> aggregator;

  public ExtractionFitness(List<S> sequence, Set<Range<Integer>> desiredExtractions, Metric... metrics) {
    aggregator = new Aggregator<S>(sequence, desiredExtractions, metrics);
  }

  public ExtractionFitness<S> changeMetrics(Metric... metrics) {
    return new ExtractionFitness<>(aggregator.sequence, aggregator.desiredExtractions, metrics);
  }

  public List<S> getSequence() {
    return aggregator.sequence;
  }

  public Set<Range<Integer>> getDesiredExtractions() {
    return aggregator.getDesiredExtractions();
  }

  public List<Metric> getMetrics() {
    return aggregator.getMetrics();
  }

  @Override
  public List<Double> apply(Extractor<S> e) {
    return aggregator.apply(e.extractNonOverlapping(aggregator.sequence));
  }

  @Override
  public List<String> names() {
    return aggregator.metrics.stream().map(m -> m.toString().toLowerCase().replace("_", ".")).collect(Collectors.toList());
  }

  private static BitSet buildMask(Set<Range<Integer>> extractions, int size) {
    BitSet bitSet = new BitSet(size);
    extractions.forEach(r -> bitSet.set(r.lowerEndpoint(), r.upperEndpoint()));
    return bitSet;
  }

}
