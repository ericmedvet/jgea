/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Accumulator<E, O> extends Listener<E> {

  O get();

  static <E, O> Accumulator<E, List<O>> collector(Function<E, O> function) {
    return from(
        "collector[%s]".formatted(function),
        ArrayList::new,
        (e, os) -> {
          os.add(function.apply(e));
          return os;
        },
        os -> {});
  }

  static <OE, OO, IE, IO> Accumulator<OE, OO> from(
      String name,
      Accumulator<IE, IO> accumulator,
      Function<OE, IE> eFunction,
      Function<IO, OO> oGetterFunction,
      Consumer<IO> ioConsumer) {
    return new Accumulator<OE, OO>() {
      @Override
      public OO get() {
        return oGetterFunction.apply(accumulator.get());
      }

      @Override
      public void listen(OE oe) {
        accumulator.listen(eFunction.apply(oe));
      }

      @Override
      public void done() {
        ioConsumer.accept(accumulator.get());
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  static <E, O> Accumulator<E, O> from(
      String name, Supplier<O> oInitializer, BiFunction<E, O, O> oUpdater, Consumer<O> doneOConsumer) {
    return new Accumulator<>() {
      private O o = oInitializer.get();

      @Override
      public O get() {
        return o;
      }

      @Override
      public void listen(E e) {
        o = oUpdater.apply(e, o);
      }

      @Override
      public void done() {
        doneOConsumer.accept(o);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  static <E> Accumulator<E, E> last() {
    return from("last", () -> null, (e, oldE) -> e, oldE -> {});
  }

  static <E, O> Accumulator<E, O> nullAccumulator() {
    return from("null", () -> null, (e, o) -> null, o -> {});
  }

  @Override
  default <X> Accumulator<X, O> on(Function<X, E> function) {
    return from("%s[on:%s]".formatted(this, function), this, function, Function.identity(), o -> {});
  }

  default <Q> Accumulator<E, Q> then(Function<O, Q> function) {
    return from("%s[then:%s]".formatted(this, function), this, Function.identity(), function, o -> {});
  }

  default Accumulator<E, O> thenOnDone(Consumer<O> consumer) {
    return from(
        "%s[thenOnDone:%s]".formatted(this, consumer),
        this,
        Function.identity(),
        Function.identity(),
        consumer);
  }
}
