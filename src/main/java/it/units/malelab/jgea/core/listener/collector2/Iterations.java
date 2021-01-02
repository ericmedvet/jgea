package it.units.malelab.jgea.core.listener.collector2;

import it.units.malelab.jgea.core.listener.Event;

/**
 * @author eric on 2021/01/02 for jgea
 */
public class Iterations extends AbstractCollector<Object, Object, Object, Integer> {
  public Iterations() {
    super("iterations", "%4d");
  }

  @Override
  public Integer apply(Event<?, ?, ?> event) {
    return event.getState().getIterations();
  }
}
