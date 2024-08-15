/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.problem.grid.CharShapeApproximation;
import io.github.ericmedvet.jgea.problem.synthetic.IntOneMax;
import io.github.ericmedvet.jgea.problem.synthetic.MultiModalIntOneMax;
import io.github.ericmedvet.jgea.problem.synthetic.MultiObjectiveIntOneMax;
import io.github.ericmedvet.jgea.problem.synthetic.OneMax;
import io.github.ericmedvet.jgea.problem.synthetic.numerical.*;
import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Discoverable(prefixTemplate = "ea.problem|p.synthetic|s")
public class SyntheticProblems {

  private SyntheticProblems() {}

  @SuppressWarnings("unused")
  @Cacheable
  public static Ackley ackley(
      @Param(value = "name", iS = "ackley-{p}") String name, @Param(value = "p", dI = 100) int p) {
    return new Ackley(p);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static BentCigar bentCigar(
      @Param(value = "name", iS = "bentCigar-{p}") String name, @Param(value = "p", dI = 100) int p) {
    return new BentCigar(p);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static CharShapeApproximation charShapeApproximation(
      @Param(value = "name", dS = "shape-{target}") String name,
      @Param("target") String syntheticTargetName,
      @Param(value = "translation", dB = true) boolean translation,
      @Param(value = "smoothed", dB = true) boolean smoothed,
      @Param(value = "weighted", dB = true) boolean weighted) {
    try {
      return new CharShapeApproximation(syntheticTargetName, translation, smoothed, weighted);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static CircularPointsAiming circularPointsAiming(
      @Param(value = "name", iS = "circularPointsAiming-{p}-{n}") String name,
      @Param(value = "p", dI = 100) int p,
      @Param(value = "n", dI = 5) int n,
      @Param(value = "radius", dD = 0.5d) double radius,
      @Param(value = "center", dD = 1d) double center,
      @Param(value = "seed", dI = 1) int seed) {
    return new CircularPointsAiming(p, n, radius, center, seed);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static Discus discus(
      @Param(value = "name", iS = "discus-{p}") String name, @Param(value = "p", dI = 100) int p) {
    return new Discus(p);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static GaussianMixture2D gaussianMixture2D(
      @Param(value = "name", dS = "gm2D") String name,
      @Param(
              value = "targets",
              dDs = {-3, -2, 2, 2, 2, 1})
          List<Double> targets,
      @Param(value = "c", dD = 1d) double c) {
    if (targets.size() % 3 != 0) {
      throw new IllegalArgumentException(
          "targets should be a list of triplets of numbers; wrong size is %d".formatted(targets.size()));
    }
    return new GaussianMixture2D(
        IntStream.range(0, targets.size() / 3)
            .mapToObj(i -> Map.entry(targets.subList(i * 3, i * 3 + 2), targets.get(i * 3 + 2)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
        c);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static HighConditionedElliptic highConditionedElliptic(
      @Param(value = "name", iS = "highConditionedElliptic-{p}") String name,
      @Param(value = "p", dI = 100) int p) {
    return new HighConditionedElliptic(p);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static IntOneMax intOneMax(
      @Param(value = "name", iS = "iOneMax-{p}") String name,
      @Param(value = "p", dI = 100) int p,
      @Param(value = "upperBound", dI = 100) int upperBound) {
    return new IntOneMax(p, upperBound);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static LinearPoints linearPoints(
      @Param(value = "name", iS = "lPoints-{p}") String name, @Param(value = "p", dI = 100) int p) {
    return new LinearPoints(p);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static MultiModalIntOneMax multiModalIntOneMax(
      @Param(value = "name", iS = "mmIOneMax-{p}-{nOfTargets}") String name,
      @Param(value = "p", dI = 100) int p,
      @Param(value = "upperBound", dI = 10) int upperBound,
      @Param(value = "nOfTargets", dI = 3) int nOfTargets) {
    return new MultiModalIntOneMax(p, upperBound, nOfTargets);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static MultiObjectiveIntOneMax multiObjectiveIntOneMax(
      @Param(value = "name", iS = "moIOneMax-{p}") String name,
      @Param(value = "p", dI = 100) int p,
      @Param(value = "upperBound", dI = 3) int upperBound) {
    return new MultiObjectiveIntOneMax(p, upperBound);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static OneMax oneMax(
      @Param(value = "name", iS = "oneMax-{p}") String name, @Param(value = "p", dI = 100) int p) {
    return new OneMax(p);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static PointsAiming pointAiming(
      @Param(value = "name", iS = "pointAiming-{p}") String name,
      @Param(value = "p", dI = 100) int p,
      @Param(value = "target", dD = 1d) double target) {
    return new PointsAiming(List.of(Collections.nCopies(p, target)));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static Rastrigin rastrigin(
      @Param(value = "name", iS = "rastrigin-{p}") String name, @Param(value = "p", dI = 100) int p) {
    return new Rastrigin(p);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static Rosenbrock rosenbrock(
      @Param(value = "name", iS = "rosenbrock-{p}") String name, @Param(value = "p", dI = 100) int p) {
    return new Rosenbrock(p);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static Sphere sphere(
      @Param(value = "name", iS = "sphere-{p}") String name, @Param(value = "p", dI = 100) int p) {
    return new Sphere(p);
  }
}
