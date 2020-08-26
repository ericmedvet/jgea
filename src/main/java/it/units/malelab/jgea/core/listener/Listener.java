/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.malelab.jgea.core.listener;

import java.util.concurrent.ExecutorService;

/**
 * @author eric
 */
@FunctionalInterface
public interface Listener<G, S, F> {

  void listen(Event<? extends G, ? extends S, ? extends F> event);

  static Listener<Object, Object, Object> deaf() {
    return (Listener<Object, Object, Object>) event -> {
    };
  }

  default Listener<G, S, F> then(Listener<? super G, ? super S, ? super F> other) {
    return (Event<? extends G, ? extends S, ? extends F> event) -> {
      listen(event);
      other.listen(event);
    };
  }

  static <G1, S1, F1> Listener<G1, S1, F1> onExecutor(final Listener<G1, S1, F1> listener, final ExecutorService executor) {
    return (final Event<? extends G1, ? extends S1, ? extends F1> event) -> {
      executor.submit(() -> {
        listener.listen(event);
      });
    };
  }

}
