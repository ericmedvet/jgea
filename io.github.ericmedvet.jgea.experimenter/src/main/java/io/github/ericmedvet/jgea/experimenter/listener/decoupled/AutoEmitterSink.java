package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class AutoEmitterSink<K, V> {

  private final ScheduledExecutorService executor;

  public AutoEmitterSink(int millisInterval, Supplier<K> kSupplier, Supplier<V> vSupplier, Sink<K, V> sink) {
    executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleAtFixedRate(
        () -> sink.push(kSupplier.get(), vSupplier.get()),
        0,
        millisInterval,
        TimeUnit.MILLISECONDS
    );
  }

  public void shutdown() {
    executor.shutdownNow();
  }


}
