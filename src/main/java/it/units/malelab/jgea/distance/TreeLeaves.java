/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.distance;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.Sequence;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class TreeLeaves<T> implements Distance<Node<T>> {
  
  private final Distance<Sequence<T>> innerDistance;

  public TreeLeaves(Distance<Sequence<T>> innerDistance) {
    this.innerDistance = innerDistance;
  }    

  @Override
  public Double apply(Node<T> t1, Node<T> t2, Listener listener) throws FunctionException {
    Sequence<T> s1 = Sequence.from(t1.leafNodes().stream().map(Node::getContent).collect(Collectors.toList()));
    Sequence<T> s2 = Sequence.from(t2.leafNodes().stream().map(Node::getContent).collect(Collectors.toList()));
    return innerDistance.apply(s1, s2);
  }
  
  
}
