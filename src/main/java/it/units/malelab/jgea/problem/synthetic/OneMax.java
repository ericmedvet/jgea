/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.synthetic;

import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.representation.sequence.bit.BitString;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicBiFunction;
import it.units.malelab.jgea.representation.sequence.bit.BitStringFactory;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.problem.surrogate.TunablePrecisionProblem;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.math3.stat.StatUtils;

/**
 *
 * @author eric
 */
public class OneMax implements Problem<BitString, Double>, TunablePrecisionProblem<BitString, Double> {

  private static class FitnessFunction implements Function<BitString, Double>, Bounded<Double> {

    @Override
    public Double worstValue() {
      return 1d;
    }

    @Override
    public Double bestValue() {
      return 0d;
    }

    @Override
    public Double apply(BitString b, Listener listener) throws FunctionException {
      return 1d - (double) b.count() / (double) b.size();
    }

  }

  private static class TunablePrecisionFitnessFunction implements NonDeterministicBiFunction<BitString, Double, Double>, Bounded<Double> {

    @Override
    public Double worstValue() {
      return 1d;
    }

    @Override
    public Double bestValue() {
      return 0d;
    }

    @Override
    public Double apply(BitString b, Double precision, Random random, Listener listener) throws FunctionException {
      int[] indexes = new int[b.size()];
      for (int i = 0; i < indexes.length; i++) {
        indexes[i] = i;
      }
      shuffleArray(indexes, random);
      double count = 0;
      double significant = Math.round((1d - precision) * (double) b.size());
      for (int i = 0; i < significant; i++) {
        count = count + (b.get(indexes[i]) ? 1d : 0d);
      }
      return 1d - count / significant;
    }

    private static void shuffleArray(int[] a, Random r) {
      // https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
      for (int i = 0; i < a.length - 1; i++) {
        int j = r.nextInt(a.length - i) + i;
        int b = a[i];
        a[i] = a[j];
        a[j] = b;
      }
    }

    public static void main(String[] args) {
      int[] a = new int[]{1, 2, 3, 4, 5, 6};
      System.out.println(Arrays.toString(a));
      shuffleArray(a, new Random());
      System.out.println(Arrays.toString(a));
    }

  }

  private final FitnessFunction fitnessFunction;
  private final TunablePrecisionFitnessFunction tunablePrecisionFitnessFunction;

  public OneMax() {
    this.fitnessFunction = new FitnessFunction();
    this.tunablePrecisionFitnessFunction = new TunablePrecisionFitnessFunction();
  }

  @Override
  public Function<BitString, Double> getFitnessFunction() {
    return fitnessFunction;
  }

  @Override
  public NonDeterministicBiFunction<BitString, Double, Double> getTunablePrecisionFitnessFunction() {
    return tunablePrecisionFitnessFunction;
  }
  
  //to check descriptive statistics of tunable fitness
  public static void main(String[] args) {
    TunablePrecisionFitnessFunction f = new TunablePrecisionFitnessFunction();
    int n = 10000;
    int l = 50;
    double[] fidelities = new double[] {0d, 0.1d, 0.25d, 0.5d, 0.9d};
    double[][] values = new double[fidelities.length][];
    for (int i = 0; i<fidelities.length; i++) {
      values[i] = new double[n];
    }
    BitStringFactory factory = new BitStringFactory(l);
    Random r = new Random(1l);
    for (int i = 0; i<n; i++) {
      BitString b = factory.build(r);
      for (int j = 0; j<fidelities.length; j++) {
        values[j][i] = f.apply(b, fidelities[j], r, Listener.deaf());
      }
    }
    for (int i = 0; i<fidelities.length; i++) {
      System.out.printf("%4.2f:\tmin=%5.3f\t25p=%5.3f\tmean=%5.3f\t50p=%5.3f\t75p=%5.3f\tmax=%5.3f\n",
              fidelities[i],
              StatUtils.min(values[i]),
              StatUtils.percentile(values[i], 25d),
              StatUtils.mean(values[i]),
              StatUtils.percentile(values[i], 5d),
              StatUtils.percentile(values[i], 75d),
              StatUtils.max(values[i])
              );
      
    }
  }

}
