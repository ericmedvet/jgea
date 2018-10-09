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

/**
 *
 * @author eric
 */
public class RobotPowerSupplyGeometry implements Problem<double[], Double> {

  private static class FitnessFunction implements Function<double[], Double>, Bounded<Double> {

    private final double w;
    private final double v;
    private final Function<double[], Boolean> constraintFunction;
    private final int steps;

    public FitnessFunction(double w, double v, Function<double[], Boolean> constraintFunction, int steps) {
      this.w = w;
      this.v = v;
      this.constraintFunction = constraintFunction;
      this.steps = steps;
    }

    @Override
    public Double apply(double[] a, Listener listener) throws FunctionException {
      //check constraints
      for (int i = 0; i < a.length - 1; i = i + 2) {
        if (!constraintFunction.apply(new double[]{a[i], a[i + 1]})) {
          return worstValue();
        }
      }
      //compute
      return 1d-pinsContact(a)/((double)a.length/2d);
    }

    private double pinContact(double x, boolean positive) {
      if ((x>=0 && x % (w + v) > w) || (x<=0 && -x%(w+v)<=v)) { //not in contact, on insulator
        return 0d;
      }
      double bandIndex = Math.floor(x/(w+v))%2;
      if (bandIndex==0 && positive) {
        return 1d;
      }
      if (bandIndex!=1 && !positive) {
        return 1d;
      }
      return 0d;
    }
    
    private double pinsContact(double[] a, double x0, double phi0) {
      double positives = 0d;
      double negatives = 0d;
      for (int i = 0; i < a.length - 1; i = i + 2) {
        double r = a[i];
        double phi = a[i+1];
        double x = x0+r*Math.sin(phi0+phi);
        positives = positives+pinContact(x, true);
        negatives = negatives+pinContact(x, false);
      }
      return Math.min(positives, negatives);
    }
    
    private double pinsContact(double[] a) {
      double min = a.length/2d;
      for (double x0 = 0; x0<2*(w+v); x0=x0+2d*(w+v)/(double)steps) {
        for (double phi0 = 0; phi0<2d*Math.PI; phi0=phi0+2d*Math.PI/(double)steps) {
          min = Math.min(min, pinsContact(a, x0, phi0));
        }
      }
      return min;
    }

    @Override
    public Double bestValue() {
      return 0d;
    }

    @Override
    public Double worstValue() {
      return 1d;
    }

  }

  private final FitnessFunction fitnessFunction;

  public RobotPowerSupplyGeometry(double w, double v, Function<double[], Boolean> constraintFunction, int steps) {
    fitnessFunction = new FitnessFunction(w, v, constraintFunction, steps);
  }

  @Override
  public NonDeterministicFunction<double[], Double> getFitnessFunction() {
    return fitnessFunction;
  }

}
