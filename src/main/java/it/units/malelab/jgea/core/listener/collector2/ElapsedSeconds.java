package it.units.malelab.jgea.core.listener.collector2;

import it.units.malelab.jgea.core.listener.Event;

/**
 * @author eric on 2021/01/02 for jgea
 */
public class ElapsedSeconds extends AbstractCollector<Object, Object, Object, Float> {
  public ElapsedSeconds() {
    super("elapsed.seconds", "%6.1f");
  }

  @Override
  public Float apply(Event<?, ?, ?> event) {
    return event.getState().getElapsedMillis() / 1000f;
  }
}
