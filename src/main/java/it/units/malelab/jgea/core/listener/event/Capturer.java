/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.event;

import it.units.malelab.jgea.core.listener.Listener;

/**
 *
 * @author eric
 */
public class Capturer implements Listener {
  
  private Event lastEvent;

  @Override
  public void listen(Event event) {
    lastEvent = event;
  }

  public Event getLastEvent() {
    return lastEvent;
  }
  
}
