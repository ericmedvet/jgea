package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.evolver.Evolver;

import java.util.Map;

/**
 * @author eric on 2021/01/16 for jgea
 */
public class EventAugmenter implements Listener<Evolver.Event<?, ?, ?>> {

  private final Map<String, ?> attributes;

  public EventAugmenter(Map<String, ?> attributes) {
    this.attributes = attributes;
  }

  @Override
  public void listen(Evolver.Event<?, ?, ?> event) {
    attributes.forEach((key, value) -> event.attributes().put(key, value));
  }
}
