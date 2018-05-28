/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.distance;

import it.units.malelab.jgea.core.Sequence;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class StringSequence implements Distance<String> {
  
  private final Distance<Sequence<Character>> innerDistance;

  public StringSequence(Distance<Sequence<Character>> innerDistance) {
    this.innerDistance = innerDistance;
  }

  @Override
  public Double apply(String string1, String string2, Listener listener) throws FunctionException {
    Sequence<Character> s1 = Sequence.from(string1.chars().mapToObj(c -> (char)c).toArray(Character[]::new));
    Sequence<Character> s2 = Sequence.from(string1.chars().mapToObj(c -> (char)c).toArray(Character[]::new));
    return innerDistance.apply(s1, s2);
  }
  
}
