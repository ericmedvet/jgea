package it.units.malelab.jgea.core.listener.collector2;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author eric on 2021/01/02 for jgea
 */
public interface Named<F, T> extends Function<F, T> {

  BiFunction<String, String, String> NAME_COMPOSER = (after, before) -> before+"→"+after;

  String getFormat();

  String getName();

  default <V> Named<V, T> of(Named<V, F> before) {
    Named<F, T> thisNamed = this;
    return new Named<>() {
      @Override
      public String getFormat() {
        return thisNamed.getFormat();
      }

      @Override
      public String getName() {
        return NAME_COMPOSER.apply(thisNamed.getName(), before.getName());
      }

      @Override
      public T apply(V v) {
        return thisNamed.apply(before.apply(v));
      }
    };
  }

  default <V> Named<F, V> then(Named<? super T, ? extends V> after) {
    Named<F, T> thisNamed = this;
    return new Named<>() {
      @Override
      public String getFormat() {
        return after.getFormat();
      }

      @Override
      public String getName() {
        return NAME_COMPOSER.apply(after.getName(), thisNamed.getName());
      }

      @Override
      public V apply(F f) {
        return after.apply(thisNamed.apply(f));
      }
    };
  }

  static <F, T> Named<F, T> build(String name, String format, Function<F, T> function) {
    return new Named<F, T>() {
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
        return function.apply(f);
      }
    };
  }

  default Named<F, T> rename(String name) {
    Named<F, T> thisNamed = this;
    return new Named<F, T>() {
      @Override
      public String getFormat() {
        return thisNamed.getFormat();
      }

      @Override
      public String getName() {
        return name;
      }

      @Override
      public T apply(F f) {
        return thisNamed.apply(f);
      }
    };
  }

  default Named<F, T> reformat(String format) {
    Named<F, T> thisNamed = this;
    return new Named<F, T>() {
      @Override
      public String getFormat() {
        return format;
      }

      @Override
      public String getName() {
        return thisNamed.getName();
      }

      @Override
      public T apply(F f) {
        return thisNamed.apply(f);
      }
    };
  }

  static String name(Function<?, ?> function) {
    if (function instanceof Named<?, ?>) {
      return ((Named<?, ?>) function).getName();
    }
    return function.getClass().getSimpleName();
  }

  static String format(Function<?, ?> function) {
    if (function instanceof Named<?, ?>) {
      return ((Named<?, ?>) function).getFormat();
    }
    return "%s";
  }

}
