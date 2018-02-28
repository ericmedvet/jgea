/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.extraction;

import com.google.common.collect.Range;
import it.units.malelab.jgea.core.function.BiFunction;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.ComposedFunction;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.WithNames;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class ExtractionFitness<E> implements ComposedFunction<E, Set<Range<Integer>>, List<Double>>, WithNames {

  public static enum Metric {

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

  };

  private static class Aggregator implements Function<Set<Range<Integer>>, List<Double>>, Bounded<List<Double>> {

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
    public List<Double> apply(Set<Range<Integer>> extractions, Listener listener) throws FunctionException {
      Map<Metric, Double> values = new EnumMap<Metric, Double>(Metric.class);
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

    @Override
    public List<Double> bestValue() {
      List<Double> results = new ArrayList<>(metrics.size());
      for (Metric metric : metrics) {
        results.add(metric.best);
      }
      return results;
    }

    @Override
    public List<Double> worstValue() {
      List<Double> results = new ArrayList<>(metrics.size());
      for (Metric metric : metrics) {
        results.add(metric.worst);
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
  public Function<E, Set<Range<Integer>>> first() {
    return (e, listener) -> extractionFunction.apply(e, getText(), listener);
  }

  @Override
  public Function<? super Set<Range<Integer>>, ? extends List<Double>> second() {
    return aggregator;
  }

  @Override
  public List<String> names() {
    return aggregator.metrics.stream().map(m -> m.toString().toLowerCase().replace("_", ".")).collect(Collectors.toList());
  }

}
