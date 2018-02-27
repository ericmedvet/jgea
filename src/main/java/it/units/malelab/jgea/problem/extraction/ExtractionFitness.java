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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;

/**
 *
 * @author eric
 */
public class ExtractionFitness<E> implements ComposedFunction<E, List<Range<Integer>>, List<Double>> {

  public static enum Metric {

    PREC(0d, Double.POSITIVE_INFINITY),
    REC(0d, 1d),
    FM(0d, Double.POSITIVE_INFINITY),
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

  private static class Aggregator implements Function<List<Range<Integer>>, List<Double>>, Bounded<List<Double>> {

    private final String text;
    private final List<Range<Integer>> desiredExtractions;
    private final SortedSet<Metric> metrics;

    public Aggregator(String text, List<Range<Integer>> desiredExtractions, SortedSet<Metric> metrics) {
      this.text = text;
      this.desiredExtractions = desiredExtractions;
      this.metrics = metrics;
    }

    @Override
    public List<Double> apply(List<Range<Integer>> extractions, Listener listener) throws FunctionException {
      Map<Metric, Double> values = new EnumMap<Metric, Double>(Metric.class);
      if (metrics.contains(Metric.FM) || metrics.contains(Metric.PREC) || metrics.contains(Metric.REC)) {
        //precision and recall
        Set<Range<Integer>> correctExtractions = new LinkedHashSet<>(extractions);
        correctExtractions.retainAll(desiredExtractions);
        double recall = (double) correctExtractions.size() / (double) desiredExtractions.size();
        double precision = (double) correctExtractions.size() / (double) extractions.size();
        double fMeasure = 2d * precision * recall / (precision + recall);
        values.put(Metric.PREC, 1-precision);
        values.put(Metric.REC, 1-recall);
        values.put(Metric.FM, 1-fMeasure);
      }
      if (metrics.contains(Metric.CHAR_ERROR) || metrics.contains(Metric.CHAR_FNR) || metrics.contains(Metric.CHAR_FPR)) {
        //TODO
      }
      List<Double> results = new ArrayList<>(metrics.size());
      for (Metric metric : metrics) {
        results.add(values.get(metric));
      }
      return results;
    }

    @Override
    public List<Double> bestValue() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Double> worstValue() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  }

  private final String text;
  private final List<Range<Integer>> desiredExtractions;
  private final BiFunction<E, String, List<Range<Integer>>> extractionFunction;
  private final Function<List<Range<Integer>>, List<Double>> aggregatorFunction;

  public ExtractionFitness(String text, List<Range<Integer>> desiredExtractions, BiFunction<E, String, List<Range<Integer>>> extractionFunction) {
    this.text = text;
    this.desiredExtractions = desiredExtractions;
    this.extractionFunction = extractionFunction;
    aggregatorFunction = null;
  }

  public String getText() {
    return text;
  }

  public BiFunction<E, String, List<Range<Integer>>> getExtractionFunction() {
    return extractionFunction;
  }

  @Override
  public Function<E, List<Range<Integer>>> first() {
    return (e, listener) -> extractionFunction.apply(e, text, listener);
  }

  @Override
  public Function<? super List<Range<Integer>>, ? extends List<Double>> second() {
    return aggregatorFunction;
  }

}
