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
      public String getFormat() {
        return thisNamedFunction.getFormat();
      }

      @Override
      public String getName() {
        return NAME_COMPOSER.apply(thisNamedFunction.getName(), before.getName());
      }

      @Override
      public T apply(V v) {
        return thisNamedFunction.apply(before.apply(v));
      }
    };
  }

  default <V> List<NamedFunction<V, ? extends T>> of(List<NamedFunction<V, ? extends F>> befores) {
    return befores.stream().map(this::of).collect(Collectors.toList());
  }

  default <V> NamedFunction<F, V> then(NamedFunction<? super T, ? extends V> after) {
    NamedFunction<F, T> thisNamedFunction = this;
    return new NamedFunction<>() {
      @Override
      public String getFormat() {
        return after.getFormat();
      }

      @Override
      public String getName() {
        return NAME_COMPOSER.apply(after.getName(), thisNamedFunction.getName());
      }

      @Override
      public V apply(F f) {
        return after.apply(thisNamedFunction.apply(f));
      }
    };
  }

  static <F, T, V> List<NamedFunction<F, ? extends V>> then(NamedFunction<F, T> before, List<NamedFunction<T, ? extends V>> afters) {
    return afters.stream().map(before::then).collect(Collectors.toList());
  }

  static <F, T> NamedFunction<F, T> build(String name, String format, Function<F, T> function) {
    return new NamedFunction<>() {
      @Override
      public String getFormat() {
        return format;
      }

      @Override
      public String getName() {
        return name;
      }

      @Override
      public T apply(F f) {
        try {
          return function.apply(f);
        } catch (Throwable t) {
          throw new RuntimeException(String.format("Cannot compute function %s: %s", getName(), t), t);
        }
      }
    };
  }

  default NamedFunction<F, T> rename(String name) {
    NamedFunction<F, T> thisNamedFunction = this;
    return new NamedFunction<F, T>() {
      @Override
      public String getFormat() {
        return thisNamedFunction.getFormat();
      }

      @Override
      public String getName() {
        return name;
      }

      @Override
      public T apply(F f) {
        return thisNamedFunction.apply(f);
      }
    };
  }

  default NamedFunction<F, T> reformat(String format) {
    NamedFunction<F, T> thisNamedFunction = this;
    return new NamedFunction<>() {
      @Override
      public String getFormat() {
        return format;
      }

      @Override
      public String getName() {
        return thisNamedFunction.getName();
      }

      @Override
      public T apply(F f) {
        return thisNamedFunction.apply(f);
      }
    };
  }

  static String name(Function<?, ?> function) {
    if (function instanceof NamedFunction<?, ?>) {
      return ((NamedFunction<?, ?>) function).getName();
    }
    return function.getClass().getSimpleName();
  }

  static String format(Function<?, ?> function) {
    if (function instanceof NamedFunction<?, ?>) {
      return ((NamedFunction<?, ?>) function).getFormat();
    }
    return "%s";
  }

  static String formatOfLongest(List<?> items) {
    int l = items.stream().map(Object::toString).mapToInt(String::length).max().orElse(1);
    return format(l);
  }

  static String format(int l) {
    return "%" + l + "." + l + "s";
  }

}
