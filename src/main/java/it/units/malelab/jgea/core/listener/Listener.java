/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.listener.event.Event;
import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author eric
 */
@FunctionalInterface
public interface Listener extends Serializable {
  
  public void listen(Event event);

  public static Listener deaf() {
    return (Event event) -> {
    };
  }

  public default Listener then(Listener other) {
    return (Event event) -> {
      listen(event);
      other.listen(event);
    };
  }

  public static Listener onExecutor(final Listener listener, final ExecutorService executor) {
    return (final Event event) -> {
      executor.submit(new Runnable() {
        @Override
        public void run() {
          listener.listen(event);
        }
      });
    };
  }

}
