/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.fitness;

import com.google.common.collect.EnumMultiset;
import com.google.common.collect.Multiset;
import it.units.malelab.jgea.core.function.BiFunction;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.core.util.WithNames;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class Classification<C, O, E extends Enum<E>> extends CaseBasedFitness<C, O, E, List<Double>> implements Bounded<List<Double>>, WithNames {

  public static enum ErrorMetric {
    CLASS_ERROR_RATE, ERROR_RATE, BALANCED_ERROR_RATE
  };

  private static class ClassErrorRate<E extends Enum<E>> implements Function<List<E>, List<Pair<Integer, Integer>>> {

    private final List<E> actualLabels;

    public ClassErrorRate(List<E> actualLabels) {
      this.actualLabels = actualLabels;
    }

    @Override
    public List<Pair<Integer, Integer>> apply(List<E> predictedLabels, Listener listener) throws FunctionException {
      E protoLabel = actualLabels.get(0);
      E[] allLabels = (E[]) protoLabel.getClass().getEnumConstants();
      Multiset<E> counts = EnumMultiset.create((Class<E>) protoLabel.getClass());
      Multiset<E> errors = EnumMultiset.create((Class<E>) protoLabel.getClass());
      for (int i = 0; i < actualLabels.size(); i++) {
        counts.add(actualLabels.get(i));
        if (!actualLabels.get(i).equals(predictedLabels.get(i))) {
          errors.add(actualLabels.get(i));
        }
      }
      List<Pair<Integer, Integer>> pairs = new ArrayList<>(allLabels.length);
      for (E currentLabel : allLabels) {
        pairs.add(Pair.build(errors.count(currentLabel), counts.count(currentLabel)));
      }
      return pairs;
    }

  }

  private static <E extends Enum<E>> Function<List<E>, List<Double>> getAggregator(List<E> actualLabels, ErrorMetric metric) {
    final ClassErrorRate<E> classErrorRate = new ClassErrorRate<>(actualLabels);
    if (metric.equals(ErrorMetric.CLASS_ERROR_RATE)) {
      return (List<E> predictedLabels, Listener listener) -> {
        List<Pair<Integer, Integer>> pairs = classErrorRate.apply(predictedLabels);
        return pairs.stream()
                .map(p -> ((double)p.first()/(double)p.second()))
                .collect(Collectors.toList());
      };
    }
    if (metric.equals(ErrorMetric.ERROR_RATE)) {
      return (List<E> predictedLabels, Listener listener) -> {
        List<Pair<Integer, Integer>> pairs = classErrorRate.apply(predictedLabels);
        int errors = pairs.stream().map(Pair::first).mapToInt(Integer::intValue).sum();
        int count = pairs.stream().map(Pair::second).mapToInt(Integer::intValue).sum();
        return Arrays.asList((double)errors/(double)count);
      };
    }
    if (metric.equals(ErrorMetric.BALANCED_ERROR_RATE)) {
      return (List<E> predictedLabels, Listener listener) -> {
        List<Pair<Integer, Integer>> pairs = classErrorRate.apply(predictedLabels);
        return Arrays.asList(pairs.stream()
                .map(p -> ((double)p.first()/(double)p.second()))
                .mapToDouble(Double::doubleValue)
                .average().orElse(Double.NaN));
      };
    }
    return null;
  }
  
  private final List<String> names;

  public Classification(List<Pair<O, E>> data, BiFunction<C, O, E> observationFitnessFunction, ErrorMetric errorMetric) {
    super(
            data.stream().map(Pair::first).collect(Collectors.toList()),
            observationFitnessFunction,
            getAggregator(data.stream().map(Pair::second).collect(Collectors.toList()), errorMetric)
    );
    names = new ArrayList<>();
    if (errorMetric.equals(ErrorMetric.CLASS_ERROR_RATE)) {
      E protoLabel = data.get(0).second();
      for (E label : (E[]) protoLabel.getClass().getEnumConstants()) {
        names.add(label.toString().toLowerCase()+".error.rate");
      }
    } else if (errorMetric.equals(ErrorMetric.ERROR_RATE)){
      names.add("error.rate");
    } else if (errorMetric.equals(ErrorMetric.BALANCED_ERROR_RATE)){
      names.add("balanced.error.rate");
    }
  }

  @Override
  public List<String> names() {
    return names;
  }

  @Override
  public List<Double> bestValue() {
    return Collections.nCopies(names.size(), 0d);
  }

  @Override
  public List<Double> worstValue() {
    return Collections.nCopies(names.size(), 1d);
  }

}
