/*
 * Copyright 2022 eric
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

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author eric on 2021/01/02 for jgea
 */
@FunctionalInterface
public interface NamedFunction<F, T> extends Function<F, T> {

  BiFunction<String, String, String> NAME_COMPOSER = (after, before) -> before + "→" + after;

  static <F, T> NamedFunction<F, T> build(String name, Function<F, T> function) {
    return build(name, "%s", function);
  }

  static <F, T> NamedFunction<F, T> build(String name, String format, Function<F, T> function) {
    return new NamedFunction<>() {
      @Override
      public T apply(F f) {
        try {
          return function.apply(f);
        } catch (Throwable t) {
          throw new RuntimeException(String.format("Cannot compute function %s: %s", getName(), t), t);
        }
      }

      @Override
      public String getFormat() {
        return format;
      }

      @Override
      public String getName() {
        return name;
      }
    };
  }

  static String format(Function<?, ?> function) {
    if (function instanceof NamedFunction<?, ?>) {
      return ((NamedFunction<?, ?>) function).getFormat();
    }
    return "%s";
  }

  static String format(int l) {
    return "%" + l + "." + l + "s";
  }

  static String formatOfLongest(List<?> items) {
    int l = items.stream().map(Object::toString).mapToInt(String::length).max().orElse(1);
    return format(l);
  }

  static String name(Function<?, ?> function) {
    if (function instanceof NamedFunction<?, ?>) {
      return ((NamedFunction<?, ?>) function).getName();
    }
    return function.getClass().getSimpleName();
  }

  @SuppressWarnings("unused")
  static <F, T, V> List<NamedFunction<F, ? extends V>> then(
      NamedFunction<F, T> before, List<NamedFunction<T, ? extends V>> afters
  ) {
    return afters.stream().map(before::then).collect(Collectors.toList());
  }

  @SuppressWarnings("unused")
  default String applyAndFormat(F f) {
    return String.format(getFormat(), apply(f));
  }

  default String getFormat() {
    return "%s";
  }

  default String getName() {
    return getClass().getSimpleName();
  }

  default <V> NamedFunction<V, T> of(NamedFunction<? super V, ? extends F> before) {
    NamedFunction<F, T> thisNamedFunction = this;
    return new NamedFunction<>() {
      @Override
      public T apply(V v) {
        return thisNamedFunction.apply(before.apply(v));
      }

      @Override
      public String getFormat() {
        return thisNamedFunction.getFormat();
      }

      @Override
      public String getName() {
        return NAME_COMPOSER.apply(thisNamedFunction.getName(), before.getName());
      }
    };
  }

  default <V> List<NamedFunction<V, ? extends T>> of(List<NamedFunction<V, ? extends F>> befores) {
    return befores.stream().map(this::of).collect(Collectors.toList());
  }

  default <K> NamedFunction<F, K> as(@SuppressWarnings("unused") Class<K> kClass) {
    NamedFunction<F, T> thisNamedFunction = this;
    return new NamedFunction<>() {
      @SuppressWarnings("unchecked")
      @Override
      public K apply(F f) {
        return (K) thisNamedFunction.apply(f);
      }

      @Override
      public String getFormat() {
        return thisNamedFunction.getFormat();
      }

      @Override
      public String getName() {
        return thisNamedFunction.getName();
      }
    };
  }

  default NamedFunction<F, T> reformat(String format) {
    NamedFunction<F, T> thisNamedFunction = this;
    return new NamedFunction<>() {
      @Override
      public T apply(F f) {
        return thisNamedFunction.apply(f);
      }

      @Override
      public String getFormat() {
        return format;
      }

      @Override
      public String getName() {
        return thisNamedFunction.getName();
      }
    };
  }

  @SuppressWarnings("unused")
  default NamedFunction<F, T> rename(String name) {
    NamedFunction<F, T> thisNamedFunction = this;
    return new NamedFunction<>() {
      @Override
      public T apply(F f) {
        return thisNamedFunction.apply(f);
      }

      @Override
      public String getFormat() {
        return thisNamedFunction.getFormat();
      }

      @Override
      public String getName() {
        return name;
      }
    };
  }

  default <V> NamedFunction<F, V> then(NamedFunction<? super T, ? extends V> after) {
    NamedFunction<F, T> thisNamedFunction = this;
    return new NamedFunction<>() {
      @Override
      public V apply(F f) {
        return after.apply(thisNamedFunction.apply(f));
      }

      @Override
      public String getFormat() {
        return after.getFormat();
      }

      @Override
      public String getName() {
        return NAME_COMPOSER.apply(after.getName(), thisNamedFunction.getName());
      }
    };
  }

  default <V> List<? extends NamedFunction<F, ? extends V>> then(List<NamedFunction<? super T, ? extends V>> afters) {
    NamedFunction<F, T> thisNamedFunction = this;
    return afters.stream().map(thisNamedFunction::then).toList();
  }
}
