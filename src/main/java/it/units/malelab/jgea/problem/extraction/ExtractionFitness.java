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

package it.units.malelab.jgea.problem.extraction;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import it.units.malelab.jgea.core.util.WithNames;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    SYMBOL_ERROR;

  }

  private static class Aggregator<S> implements Function<Set<Range<Integer>>, List<Double>> {

    private final List<S> sequence;
    private final Set<Range<Integer>> desiredExtractions;
    private final List<Metric> metrics;
    private final Set<Integer> desiredExtractedSymbolIndexes;

    public Aggregator(List<S> sequence, Set<Range<Integer>> desiredExtractions, Metric... metrics) {
      this.sequence = sequence;
      this.desiredExtractions = desiredExtractions;
      this.metrics = Arrays.asList(metrics);
      desiredExtractedSymbolIndexes = desiredExtractions.stream()
          .map(r -> IntStream.range(r.lowerEndpoint(), r.upperEndpoint())
              .mapToObj(i -> i)
              .collect(Collectors.toSet()))
          .reduce(Sets::union)
          .orElse(Set.of());
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
      if (metrics.contains(Metric.SYMBOL_ERROR) || metrics.contains(Metric.SYMBOL_FNR) || metrics.contains(Metric.SYMBOL_FPR)) {
        //TODO might be maybe faster with bit algebra
        Set<Integer> extractedSymbolIndexes = extractions.stream()
            .map(r -> IntStream.range(r.lowerEndpoint(), r.upperEndpoint())
                .mapToObj(i -> i)
                .collect(Collectors.toSet()))
            .reduce(Sets::union)
            .orElse(Set.of());
        double truePositiveChars = Sets.intersection(desiredExtractedSymbolIndexes, extractedSymbolIndexes).size();
        double falseNegativeChars = desiredExtractedSymbolIndexes.size() - truePositiveChars;
        double falsePositiveChars = extractedSymbolIndexes.size() - truePositiveChars;
        double trueNegativeChars = sequence.size() - falsePositiveChars - truePositiveChars - falseNegativeChars;
        values.put(Metric.SYMBOL_FPR, falsePositiveChars / (trueNegativeChars + falsePositiveChars));
        values.put(Metric.SYMBOL_FNR, falseNegativeChars / (truePositiveChars + falseNegativeChars));
        values.put(Metric.SYMBOL_ERROR, (falsePositiveChars + falseNegativeChars) / (double) sequence.size());
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

}
