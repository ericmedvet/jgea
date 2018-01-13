/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.listener.event.Event;
import it.units.malelab.jgea.core.listener.event.InfoEvent;
import it.units.malelab.jgea.core.util.Misc;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class ListenerUtils {
  
  public static Map<String, Object> fromInfoEvents(List<Event> events, String prefix) {
    Map<String, Object> info = new LinkedHashMap<>();
    for (Event event : events) {
      if (event instanceof InfoEvent) {
        info.putAll(Misc.keyPrefix(prefix, ((InfoEvent)event).getInfo()));
      }
    }
    return info;
  }
  
}
