/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.application;

import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author eric
 */
public class RobotPowerSupplyGeometry implements Problem<double[], List<Double>> {

  public enum Objective {
    CONTACT_MIN, CONTACT_AVG, DIST_AVG, BALANCE
  };

  private final double w;
  private final double v;
  private final Function<double[], Boolean> constraintFunction;
  private double rMin;
  private double rMax;
  private final int steps;
  private final FitnessFunction fitnessFunction;
  private final List<Objective> objectives;

  public RobotPowerSupplyGeometry(double w, double v, Function<double[], Boolean> constraintFunction, double rMin, double rMax, int steps, Objective... objectives) {
    this.w = w;
    this.v = v;
    this.constraintFunction = constraintFunction;
    this.rMin = rMin;
    this.rMax = rMax;
    this.steps = steps;
    this.objectives = new ArrayList<>(Objective.values().length);
    for (Objective objective : objectives) {
      this.objectives.add(objective);
    }
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
    return -Math.abs(positives-negatives);
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

  private List<double[]> validPins(double[] a) throws FunctionException {
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
    double[] s = new double[a.length];
    for (int i = 0; i<a.length; i++) {
      if (i % 2 == 0) {
        s[i] = a[i]*(rMax-rMin)+rMin;
      } else {
        s[i] = a[i]*2*Math.PI+Math.PI;
      }
    }
    return s;
  }

  private class FitnessFunction implements Function<double[], List<Double>>, Bounded<List<Double>> {

    @Override
    public List<Double> bestValue() {
      List<Double> values = new ArrayList<>();
      for (Objective objective : objectives) {
        if (objective.equals(Objective.CONTACT_AVG)) {
          values.add(Double.POSITIVE_INFINITY);
        }
        if (objective.equals(Objective.CONTACT_MIN)) {
          values.add(Double.POSITIVE_INFINITY);
        }
        if (objective.equals(Objective.DIST_AVG)) {
          values.add(Double.POSITIVE_INFINITY);
        }
        if (objective.equals(Objective.BALANCE)) {
          values.add(0d);
        }
      }
      return values;
    }

    @Override
    public List<Double> worstValue() {
      List<Double> values = new ArrayList<>();
      for (Objective objective : objectives) {
        if (objective.equals(Objective.CONTACT_AVG)) {
          values.add(0d);
        }
        if (objective.equals(Objective.CONTACT_MIN)) {
          values.add(0d);
        }
        if (objective.equals(Objective.DIST_AVG)) {
          values.add(0d);
        }
        if (objective.equals(Objective.DIST_AVG)) {
          values.add(Double.NEGATIVE_INFINITY);
        }
      }
      return values;
    }

    @Override
    public List<Double> apply(double[] a, Listener listener) throws FunctionException {
      List<Double> values = new ArrayList<>();
      double[] s = scale(a);
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
          values.add(pinsBalance(a));
        }
      }
      return values;
    }

  }

  @Override
  public NonDeterministicFunction<double[], List<Double>> getFitnessFunction() {
    return fitnessFunction;
  }
  
  public Function<double[], Integer> getValidContactsFunction() {
    return (a, l) -> validPins(scale(a)).size();
  }

}
