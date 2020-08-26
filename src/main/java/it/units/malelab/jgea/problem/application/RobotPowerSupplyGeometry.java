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

package it.units.malelab.jgea.problem.application;

import it.units.malelab.jgea.core.Problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author eric
 */
public class RobotPowerSupplyGeometry implements Problem<List<Double>, List<Double>> {

  public enum Objective {
    CONTACT_MIN, CONTACT_AVG, DIST_AVG, BALANCE
  }

  private final double w;
  private final double v;
  private final Function<double[], Boolean> constraintFunction;
  private final double rMin;
  private final double rMax;
  private final int steps;
  private final boolean symmetric;
  private final FitnessFunction fitnessFunction;
  private final List<Objective> objectives;

  public RobotPowerSupplyGeometry(double w, double v, Function<double[], Boolean> constraintFunction, double rMin, double rMax, boolean symmetric, int steps, Objective... objectives) {
    this.w = w;
    this.v = v;
    this.constraintFunction = constraintFunction;
    this.rMin = rMin;
    this.rMax = rMax;
    this.steps = steps;
    this.symmetric = symmetric;
    this.objectives = new ArrayList<>(Arrays.asList(objectives));
    this.fitnessFunction = new FitnessFunction();
  }

  private double pinContact(double x, boolean positive) {
    if ((x >= 0 && x % (w + v) > w) || (x <= 0 && -x % (w + v) <= v)) { //not in contact, on insulator
      return 0d;
    }
    double bandIndex = Math.floor(x / (w + v)) % 2;
    if (bandIndex == 0 && positive) {
      return 1d;
    }
    if (bandIndex != 0 && !positive) {
      return 1d;
    }
    return 0d;
  }

  private double pinsContact(List<double[]> pins, double x0, double phi0) {
    double positives = 0d;
    double negatives = 0d;
    for (double[] pin : pins) {
      double r = pin[0];
      double phi = pin[1];
      double x = x0 + r * Math.sin(phi0 + phi);
      positives = positives + pinContact(x, true);
      negatives = negatives + pinContact(x, false);
    }
    return Math.min(positives, negatives);
  }

  private double pinsBalance(List<double[]> pins, double x0, double phi0) {
    double positives = 0d;
    double negatives = 0d;
    for (double[] pin : pins) {
      double r = pin[0];
      double phi = pin[1];
      double x = x0 + r * Math.sin(phi0 + phi);
      positives = positives + pinContact(x, true);
      negatives = negatives + pinContact(x, false);
    }
    return -Math.abs(positives - negatives);
  }

  private double pinsContact(double[] a, boolean average) {
    double min = a.length / 2d;
    double sum = 0d;
    List<double[]> validPins = validPins(a);
    for (double x0 = 0; x0 < 2 * (w + v); x0 = x0 + 2d * (w + v) / (double) steps) {
      for (double phi0 = 0; phi0 < 2d * Math.PI; phi0 = phi0 + 2d * Math.PI / (double) steps) {
        double contacts = pinsContact(validPins, x0, phi0);
        sum = sum + contacts;
        min = Math.min(min, contacts);
      }
    }
    return average ? (sum / (double) (steps * steps)) : min;
  }

  private double pinsBalance(double[] a) {
    double sum = 0d;
    List<double[]> validPins = validPins(a);
    for (double x0 = 0; x0 < 2 * (w + v); x0 = x0 + 2d * (w + v) / (double) steps) {
      for (double phi0 = 0; phi0 < 2d * Math.PI; phi0 = phi0 + 2d * Math.PI / (double) steps) {
        double contacts = pinsBalance(validPins, x0, phi0);
        sum = sum + contacts;
      }
    }
    return sum / (double) (steps * steps);
  }

  private double avgDistanceToValidClosest(double[] a) {
    List<double[]> validPins = validPins(a);
    if (validPins.size() <= 1) {
      return 0d;
    }
    //compute distances
    double[][] dists = new double[validPins.size()][validPins.size()];
    for (int i = 0; i < validPins.size(); i++) {
      for (int j = 0; j <= i; j++) {
        double d;
        if (i == j) {
          d = Double.POSITIVE_INFINITY;
        } else {
          double xi = validPins.get(i)[0] * Math.cos(validPins.get(i)[1]);
          double yi = validPins.get(i)[0] * Math.sin(validPins.get(i)[1]);
          double xj = validPins.get(j)[0] * Math.cos(validPins.get(j)[1]);
          double yj = validPins.get(j)[0] * Math.sin(validPins.get(j)[1]);
          d = Math.sqrt((xi - xj) * (xi - xj) + (yi - yj) * (yi - yj));
        }
        dists[i][j] = d;
        dists[j][i] = d;
      }
    }
    //find average of mins
    return Arrays.stream(dists).mapToDouble(ds -> Arrays.stream(ds).min().orElse(0d)).average().orElse(0d);
  }

  private List<double[]> validPins(double[] a) {
    //get valid pins
    List<double[]> validPins = new ArrayList<>();
    for (int i = 0; i < a.length - 1; i = i + 2) {
      if (constraintFunction.apply(new double[]{a[i], a[i + 1]})) {
        validPins.add(new double[]{a[i], a[i + 1]});
      }
    }
    return validPins;
  }

  private double[] scale(double[] a) {
    double[] s = new double[a.length * (symmetric ? 2 : 1)];
    int c = 0;
    for (int i = 0; i < a.length; i = i + 2) {
      double r = a[i] * (rMax - rMin) + rMin;
      double phi = a[i + 1] * 2 * Math.PI + Math.PI;
      s[c] = r;
      s[c + 1] = phi;
      c = c + 2;
      if (symmetric) {
        s[c] = r;
        s[c + 1] = -phi;
        c = c + 2;
      }
    }
    return s;
  }

  private class FitnessFunction implements Function<List<Double>, List<Double>> {

    @Override
    public List<Double> apply(List<Double> sequence) {
      List<Double> values = new ArrayList<>();
      double[] s = new double[sequence.size()];
      for (int i = 0; i < sequence.size(); i++) {
        s[i] = sequence.get(i);
      }
      s = scale(s);
      for (Objective objective : objectives) {
        if (objective.equals(Objective.CONTACT_AVG)) {
          values.add(pinsContact(s, true));
        }
        if (objective.equals(Objective.CONTACT_MIN)) {
          values.add(pinsContact(s, false));
        }
        if (objective.equals(Objective.DIST_AVG)) {
          values.add(avgDistanceToValidClosest(s));
        }
        if (objective.equals(Objective.BALANCE)) {
          values.add(pinsBalance(s));
        }
      }
      return values;
    }

  }

  @Override
  public Function<List<Double>, List<Double>> getFitnessFunction() {
    return fitnessFunction;
  }

  public Function<double[], Integer> getValidContactsFunction() {
    return a -> validPins(scale(a)).size();
  }

  public Function<double[], Double> getMinContactsFunction() {
    return a -> pinsContact(scale(a), false);
  }

  public Function<double[], Double> getAvgContactsFunction() {
    return a -> pinsContact(scale(a), true);
  }

  public Function<double[], Double> getAvgDistFunction() {
    return a -> avgDistanceToValidClosest(scale(a));
  }

  public Function<double[], Double> getAvgBalanceFunction() {
    return a -> pinsBalance(scale(a));
  }

}
