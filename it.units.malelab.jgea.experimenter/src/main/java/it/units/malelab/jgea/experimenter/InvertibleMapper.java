package it.units.malelab.jgea.experimenter;

import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2022/11/21 for 2d-robot-evolution
 */
public interface InvertibleMapper<T, R> extends Function<T, R> {
  T exampleInput();
}
