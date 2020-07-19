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
import it.units.malelab.jgea.core.util.WithNames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class ExtractionFitness<E> implements Function<E, List<Double>>, WithNames {

  public enum Metric {

    ONE_MINUS_PREC(0d, Double.POSITIVE_INFINITY),
    ONE_MINUS_REC(0d, 1d),
    ONE_MINUS_FM(0d, Double.POSITIVE_INFINITY),
    CHAR_FNR(0d, 1d),
    CHAR_FPR(0d, 1d),
    CHAR_ERROR(0d, 1d);

    private final double best;
    private final double worst;

    Metric(double best, double worst) {
      this.best = best;
      this.worst = worst;
    }

  }

  private static class Aggregator implements Function<Set<Range<Integer>>, List<Double>> {

    private final String text;
    private final Set<Range<Integer>> desiredExtractions;
    private final List<Metric> metrics;
    private final double posChars;

    public Aggregator(String text, Set<Range<Integer>> desiredExtractions, Metric... metrics) {
      this.text = text;
      this.desiredExtractions = desiredExtractions;
      this.metrics = Arrays.asList(metrics);
      posChars = desiredExtractions.stream()
          .mapToInt(range -> (range.upperEndpoint() - range.lowerEndpoint()))
          .sum();
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
      if (metrics.contains(Metric.CHAR_ERROR) || metrics.contains(Metric.CHAR_FNR) || metrics.contains(Metric.CHAR_FPR)) {
        double asPosChars = extractions.stream()
            .mapToInt(range -> (range.upperEndpoint() - range.lowerEndpoint()))
            .sum();
        double truePosChars = extractions.stream()
            .mapToInt(e -> intersections(e, desiredExtractions).stream().mapToInt(range -> (range.upperEndpoint() - range.lowerEndpoint())).sum())
            .sum();
        double falseNegChars = posChars - truePosChars;
        double falsePosChars = asPosChars - truePosChars;
        values.put(Metric.CHAR_FPR, falsePosChars / ((double) text.length() - posChars));
        values.put(Metric.CHAR_FNR, falseNegChars / posChars);
        values.put(Metric.CHAR_ERROR, (falseNegChars + falsePosChars) / (double) text.length());
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

    public String getText() {
      return text;
    }

    public Set<Range<Integer>> getDesiredExtractions() {
      return desiredExtractions;
    }

    public List<Metric> getMetrics() {
      return metrics;
    }

  }

  private final BiFunction<E, String, Set<Range<Integer>>> extractionFunction;
  private final Aggregator aggregator;

  public ExtractionFitness(String text, Set<Range<Integer>> desiredExtractions, BiFunction<E, String, Set<Range<Integer>>> extractionFunction, Metric... metrics) {
    this.extractionFunction = extractionFunction;
    aggregator = new Aggregator(text, desiredExtractions, metrics);
  }

  public ExtractionFitness<E> changeMetrics(Metric... metrics) {
    return new ExtractionFitness<>(aggregator.text, aggregator.desiredExtractions, extractionFunction, metrics);
  }

  public String getText() {
    return aggregator.getText();
  }

  public Set<Range<Integer>> getDesiredExtractions() {
    return aggregator.getDesiredExtractions();
  }

  public List<Metric> getMetrics() {
    return aggregator.getMetrics();
  }

  public BiFunction<E, String, Set<Range<Integer>>> getExtractionFunction() {
    return extractionFunction;
  }

  @Override
  public List<Double> apply(E e) {
    return aggregator.apply(extractionFunction.apply(e, getText()));
  }

  @Override
  public List<String> names() {
    return aggregator.metrics.stream().map(m -> m.toString().toLowerCase().replace("_", ".")).collect(Collectors.toList());
  }

}
