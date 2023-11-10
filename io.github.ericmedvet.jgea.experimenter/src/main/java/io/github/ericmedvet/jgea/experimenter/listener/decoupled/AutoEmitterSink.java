/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import io.github.ericmedvet.jgea.core.util.Pair;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class AutoEmitterSink<K, V> {

  private final ScheduledExecutorService executor;

  public AutoEmitterSink(int millisInterval, Supplier<Pair<K, V>> pairSupplier, Sink<K, V> sink) {
    executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleAtFixedRate(
        () -> {
          Pair<K, V> pair = pairSupplier.get();
          sink.push(pair.first(), pair.second());
        },
        0, millisInterval, TimeUnit.MILLISECONDS
    );
  }

  public void shutdown() {
    executor.shutdownNow();
  }
}
