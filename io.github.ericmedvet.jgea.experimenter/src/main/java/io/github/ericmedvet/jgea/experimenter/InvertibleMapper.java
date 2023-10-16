
package io.github.ericmedvet.jgea.experimenter;

import java.util.function.BiFunction;
import java.util.function.Function;
public interface InvertibleMapper<T, R> {
  T exampleFor(R r);

  Function<T, R> mapperFor(R r);

  static <T, R> InvertibleMapper<T, R> from(BiFunction<R, T, R> mapperF, Function<R, T> exampleF) {
    return new InvertibleMapper<>() {
      @Override
      public T exampleFor(R r) {
        return exampleF.apply(r);
      }

      @Override
      public Function<T, R> mapperFor(R r) {
        return t -> mapperF.apply(r, t);
      }
    };
  }

  static <T> InvertibleMapper<T, T> identity() {
    return InvertibleMapper.from(
        (t, t2) -> t2,
        t -> t
    );
  }

  default <Q> InvertibleMapper<T, Q> andThen(InvertibleMapper<R, Q> otherMapper) {
    InvertibleMapper<T, R> thisMapper = this;
    return from(
        (q, t) -> otherMapper.mapperFor(q).apply(thisMapper.mapperFor(otherMapper.exampleFor(q)).apply(t)),
        q -> thisMapper.exampleFor(otherMapper.exampleFor(q))
    );
  }

}
