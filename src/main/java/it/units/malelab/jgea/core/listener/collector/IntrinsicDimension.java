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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 *
 * @author eric
 */
public class IntrinsicDimension<G> implements DataCollector {

  private final Distance<G> distance;
  private final boolean dropDuplicates;

  public IntrinsicDimension(Distance<G> distance, boolean dropDuplicates) {
    this.distance = distance;
    this.dropDuplicates = dropDuplicates;
  }

  @Override
  public List<Item> collect(EvolutionEvent evolutionEvent) {
    List<Collection<Individual>> rankedPopulation = new ArrayList<>((List) evolutionEvent.getRankedPopulation());
    List<G> genotypes = rankedPopulation.stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList())
            .stream()
            .map(i -> (G)i.getGenotype())
            .collect(Collectors.toList());
    if (dropDuplicates) {
      genotypes = new ArrayList<>(new LinkedHashSet<>(genotypes));
    }
    //compute distance matrix
    double[][] dMatrix = new double[genotypes.size()][genotypes.size()];
    for (int i1 = 0; i1 < genotypes.size(); i1++) {
      for (int i2 = i1 + 1; i2 < genotypes.size(); i2++) {
        dMatrix[i1][i2] = distance.apply(genotypes.get(i1), genotypes.get(i2));
        dMatrix[i2][i1] = dMatrix[i1][i2];
      }
    }
    //find mus
    List<Double> mus = new ArrayList<>();
    for (int i = 0; i < genotypes.size(); i++) {
      double[] ds = dMatrix[i];
      Arrays.sort(ds);
      for (int j = 0; j < ds.length; j++) {
        if ((ds[j] > 0) && (j < ds.length - 1)) {
          mus.add(ds[j + 1] / ds[j]);
          break;
        }
      }
    }
    Collections.sort(mus);
    //build x and y
    double s = mus.size();
    double[] x = mus.stream().mapToDouble(d -> Math.log(d)).toArray();
    double[] y = IntStream.range(0, (int) s).asDoubleStream().map(d -> -Math.log(1 - d / s)).toArray();

    //for (int i = 0; i < mus.size(); i++) {
    //  System.out.printf("%f %f%n", x[i], y[i]);
    //}

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

}
