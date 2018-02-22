/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.event;

import java.util.Map;

/**
 *
 * @author eric
 */
public class FunctionEvent<A, B> implements InfoEvent {
  
  private final A source;
  private final B destination;
  private final Map<String, Object> info;

  public FunctionEvent(A source, B destination, Map<String, Object> info) {
    this.source = source;
    this.destination = destination;
    this.info = info;
  }

  public A getSource() {
    return source;
  }

  public B getDestination() {
    return destination;
  }

  public Map<String, Object> getInfo() {
    return info;
  }  
  
}
