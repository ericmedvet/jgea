/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.distance.Distance;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 *
 * @author eric
 */
public class IntrinsicDimension implements DataCollector {

  private final static float DEFAULT_FRACTION = 0.9f;

  private final Distance<Individual> distance;
  private final float fraction;

  public IntrinsicDimension(Distance<Individual> distance, float fraction) {
    this.distance = distance;
    this.fraction = fraction;
  }

  public IntrinsicDimension(Distance<Individual> distance) {
    this(distance, DEFAULT_FRACTION);
  }

  @Override
  public List<Item> collect(EvolutionEvent evolutionEvent) {
    List<Collection<Individual>> rankedPopulation = new ArrayList<>((List) evolutionEvent.getRankedPopulation());
    List<Individual> individuals = rankedPopulation.stream().flatMap(Collection::stream).collect(Collectors.toList());
    //compute distance matrix
    double[][] dMatrix = new double[individuals.size()][individuals.size()];
    for (int i1 = 0; i1 < individuals.size(); i1++) {
      for (int i2 = i1 + 1; i2 < individuals.size(); i2++) {
        dMatrix[i1][i2] = distance.apply(individuals.get(i1), individuals.get(i2));
        dMatrix[i2][i1] = dMatrix[i1][i2];
      }
    }
    //find mus
    List<Double> mus = new ArrayList<>();
    for (int i = 0; i < individuals.size(); i++) {
      double[] ds = dMatrix[i];
      Arrays.sort(ds);
      for (int j = 0; j < ds.length; j++) {
        if ((ds[j] > 0) && (j < ds.length - 1)) {
          mus.add(ds[j+1] / ds[j]);
          break;
        }
      }
    }
    Collections.sort(mus);
    //build x and y
    double s = mus.size();
    double[] x = mus.stream().mapToDouble(d -> Math.log(d)).toArray();
    double[] y = IntStream.range(0, (int)s).asDoubleStream().map(d -> -Math.log(1 - d/s)).toArray();
    //do regression
    SimpleRegression regression = new SimpleRegression();
    for (int i = 0; i < mus.size(); i++) {
      regression.addData(x[i], y[i]);
    }
    return Arrays.asList(
            new Item<>("intrinsic.dimension.slope", regression.getSlope(), "%4.1f"),
            new Item<>("intrinsic.dimension.pearson", regression.getR(), "%4.2f")
    );
  }

  private double minGreaterThan(double[] values, double t, boolean exclusive) {
    double m = Double.NaN;
    for (double v : values) {
      if ((exclusive ? (v > t) : (v >= t)) && (Double.isNaN(m) || v < m)) {
        m = v;
      }
    }
    return m;
  }

}
