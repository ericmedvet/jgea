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

package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.listener.Event;
import it.units.malelab.jgea.distance.Distance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * @author eric
 */
public class IntrinsicDimension<G> implements DataCollector<G, Object, Object> {

  private final Distance<G> distance;
  private final boolean dropDuplicates;

  public IntrinsicDimension(Distance<G> distance, boolean dropDuplicates) {
    this.distance = distance;
    this.dropDuplicates = dropDuplicates;
  }

  @Override
  public List<Item> collect(Event<? extends G, ?, ?> event) {
    List<G> genotypes = event.getOrderedPopulation().all().stream()
        .map(Individual::getGenotype)
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
    //do regression
    SimpleRegression regression = new SimpleRegression();
    for (int i = 0; i < mus.size(); i++) {
      regression.addData(x[i], y[i]);
    }
    return Arrays.asList(
        new Item("intrinsic.dimension.slope", regression.getSlope(), "%4.1f"),
        new Item("intrinsic.dimension.pearson", regression.getR(), "%4.2f")
    );
  }

}
