package io.github.ericmedvet.jgea.sample;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

public class GMain {
  public static void main(String[] args) {
    Function<Double, String> f1 = v -> v.toString();
    Function<Number, Double> f2 = v -> v.doubleValue();
    Double v = 3d;
    f2.apply(v);
    List<?> results = List.of(f1, f2)
        .stream()
        .map(f -> ((Function)f).apply(v))
        .toList();
    System.out.println(results);
  }
}
