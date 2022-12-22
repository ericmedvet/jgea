package io.github.ericmedvet.jgea.problem.synthetic;

import io.github.ericmedvet.jgea.core.MultiHomogeneousObjectiveProblem;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class Cones implements MultiHomogeneousObjectiveProblem<List<Double>, Double> {

  @Override
  public List<Comparator<Double>> comparators() {
    Comparator<Double> comparator = Double::compareTo;
    return List.of(comparator, comparator, comparator.reversed());
  }

  @Override
  public Function<List<Double>, List<Double>> qualityFunction() {
    return list -> {
      double r = list.get(0);
      double h = list.get(0);
      double s = Math.sqrt(r * r + h * h);
      double lateralSurface = Math.PI * r * s;
      double totalSurface = Math.PI * r * (r + s);
      double volume = Math.PI * r * r * h / 3;
      return List.of(lateralSurface, totalSurface, volume);
    };
  }
}
