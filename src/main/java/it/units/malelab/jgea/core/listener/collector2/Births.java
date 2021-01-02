package it.units.malelab.jgea.core.listener.collector2;

import it.units.malelab.jgea.core.listener.Event;

/**
 * @author eric on 2021/01/02 for jgea
 */
public class Births extends AbstractCollector<Object, Object, Object, Integer> {
  public Births() {
    super("births", "%5d");
  }

  @Override
  public Integer apply(Event<?, ?, ?> event) {
    return event.getState().getBirths();
  }
}
