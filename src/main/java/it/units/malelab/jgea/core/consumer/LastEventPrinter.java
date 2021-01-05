package it.units.malelab.jgea.core.consumer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author eric on 2021/01/05 for jgea
 */
public class LastEventPrinter<G, S, F> implements Consumer.Factory<G, S, F, String> {

  private final List<NamedFunction<Event<? extends G, ? extends S, ? extends F>, ?>> functions;

  public LastEventPrinter(List<NamedFunction<Event<? extends G, ? extends S, ? extends F>, ?>> functions) {
    this.functions = functions;
  }

  @Override
  public Consumer<G, S, F, String> build() {
    return new Consumer<>() {

      private Event<? extends G, ? extends S, ? extends F> lastEvent;

      @Override
      public String produce() {
        return functions.stream()
            .map(f -> String.format(
                "%s : " + f.getFormat() + "",
                f.getName(),
                f.apply(lastEvent)
            ))
            .collect(Collectors.joining("\n"));
      }

      @Override
      public void consume(Event<? extends G, ? extends S, ? extends F> event) {
        lastEvent = event;
      }
    };
  }

}
