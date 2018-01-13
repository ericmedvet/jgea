/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.event;

import it.units.malelab.jgea.core.listener.Listener;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eric
 */
public class Capturer implements Listener {
  
  private final List<Event> events;

  public Capturer() {
    events = new ArrayList<>();
  }

  @Override
  public void listen(Event event) {
    events.add(event);
  }

  public List<Event> getEvents() {
    return events;
  }
  
  public void clear() {
    events.clear();
  }

}
