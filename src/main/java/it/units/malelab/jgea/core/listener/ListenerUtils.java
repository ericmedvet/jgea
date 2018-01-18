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
import java.util.concurrent.ExecutorService;

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
  
  public static Listener deafListener() {
    return new Listener() {
      @Override
      public void listen(Event event) {       
      }
    };
  }
  
  public static Listener chain(final Listener... listeners) {
    return new Listener() {
      @Override
      public void listen(Event event) {
        for (Listener listener : listeners) {
          listener.listen(event);
        }
      }
    };
  }
  
  public static Listener onExecutor(final Listener listener, final ExecutorService executor) {
    return new Listener() {
      @Override
      public void listen(final Event event) {
        executor.submit(new Runnable() {
          @Override
          public void run() {
            listener.listen(event);
          }
        });
      }
    };
  }
  
}
