
package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.problem.grid.CharShapeApproximation;
import io.github.ericmedvet.jgea.problem.synthetic.*;
import io.github.ericmedvet.jnb.core.Param;

import java.io.IOException;

public class SyntheticProblems {

  private SyntheticProblems() {
  }

  @SuppressWarnings("unused")
  public static Ackley ackley(
      @Param(value = "p", dI = 100) int p
  ) {
    return new Ackley(p);
  }

  @SuppressWarnings("unused")
  public static CharShapeApproximation charShapeApproximation(
      @Param("target") String syntheticTargetName,
      @Param(value = "translation", dB = true) boolean translation,
      @Param(value = "smoothed", dB = true) boolean smoothed,
      @Param(value = "weighted", dB = true) boolean weighted
  ) {
    try {
      return new CharShapeApproximation(syntheticTargetName, translation, smoothed, weighted);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unused")
  public static DoublesOneMax doublesOneMax(
      @Param(value = "p", dI = 100) int p
  ) {
    return new DoublesOneMax(p);
  }

  @SuppressWarnings("unused")
  public static DoublesVariableTarget doublesVariableTarget(
      @Param(value = "p", dI = 100) int p,
      @Param(value = "target") double target
  ) {
    return new DoublesVariableTarget(p, target);
  }

  @SuppressWarnings("unused")
  public static IntOneMax intOneMax(
      @Param(value = "p", dI = 100) int p,
      @Param(value = "upperBound", dI = 100) int upperBound
  ) {
    return new IntOneMax(p, upperBound);
  }

  @SuppressWarnings("unused")
  public static LinearPoints linearPoints(
      @Param(value = "p", dI = 100) int p
  ) {
    return new LinearPoints(p);
  }

  @SuppressWarnings("unused")
  public static OneMax oneMax(
      @Param(value = "p", dI = 100) int p
  ) {
    return new OneMax(p);
  }

  @SuppressWarnings("unused")
  public static Rastrigin rastrigin(
      @Param(value = "p", dI = 100) int p
  ) {
    return new Rastrigin(p);
  }

  @SuppressWarnings("unused")
  public static Sphere sphere(
      @Param(value = "p", dI = 100) int p
  ) {
    return new Sphere(p);
  }
}
