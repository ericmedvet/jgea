/*-
 * ========================LICENSE_START=================================
 * jgea-core
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
package io.github.ericmedvet.jgea.core.listener;

import java.util.function.Function;

public interface Accumulator<E, O> extends Listener<E> {

  O get();

  static <E> Accumulator<E, E> last() {
    return new Accumulator<>() {
      E last;

      @Override
      public E get() {
        return last;
      }

      @Override
      public void listen(E e) {
        last = e;
      }
    };
  }

  default <Q> Accumulator<E, Q> then(Function<O, Q> function) {
    Accumulator<E, O> thisAccumulator = this;
    return new Accumulator<>() {
      @Override
      public Q get() {
        return function.apply(thisAccumulator.get());
      }

      @Override
      public void listen(E e) {
        thisAccumulator.listen(e);
      }

      @Override
      public void done() {
        thisAccumulator.done();
      }
    };
  }

  default Listener<E> withAutoGet() {
    Accumulator<E, O> thisAccumulator = this;
    return new Listener<>() {
      @Override
      public void listen(E e) {
        thisAccumulator.listen(e);
      }

      @Override
      public void done() {
        thisAccumulator.get();
        thisAccumulator.done();
      }
    };
  }
}
