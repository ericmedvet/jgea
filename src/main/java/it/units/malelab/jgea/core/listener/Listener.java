/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.core.listener;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;

/**
 * @author eric
 */
@FunctionalInterface
public interface Listener<G, S, F> extends Serializable {

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
