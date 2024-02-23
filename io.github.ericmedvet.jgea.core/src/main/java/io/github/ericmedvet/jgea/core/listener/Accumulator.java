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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Accumulator<E, O> extends Listener<E> {

  O get();

  static <E, O> Accumulator<E, List<O>> collector(Function<E, O> function) {
    return new Accumulator<>() {
      private final List<O> os = new ArrayList<>();

      @Override
      public List<O> get() {
        return Collections.unmodifiableList(os);
      }

      @Override
      public void listen(E e) {
        os.add(function.apply(e));
      }
    };
  }

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

  default Accumulator<E, O> thenOnDone(Consumer<O> consumer) {
    Accumulator<E, O> thisAccumulator = this;
    return new Accumulator<>() {
      @Override
      public O get() {
        return thisAccumulator.get();
      }

      @Override
      public void listen(E e) {
        thisAccumulator.listen(e);
      }

      @Override
      public void done() {
        thisAccumulator.done();
        consumer.accept(thisAccumulator.get());
      }
    };
  }
}
