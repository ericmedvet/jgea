/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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
package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

@Discoverable(prefixTemplate = "ea.function|f")
public class Functions {

  private static final Logger L = Logger.getLogger(Functions.class.getName());
  private static final String DEFAULT_FORMAT = "%.0s";

  static <X, I, O, Y> FormattedNamedFunction<X, Y> build(
      Function<X, I> beforeF, Function<I, O> f, Function<O, Y> afterF, String format, String name) {
    NamedFunction<X, Y> nf = NamedFunction.from(f, name).compose(beforeF).andThen(afterF);
    if (nf instanceof FormattedNamedFunction<X, Y> fnf) {
      if (format.equals(DEFAULT_FORMAT)) {
        return fnf;
      }
      return fnf.reformatted(format);
    }
    return FormattedNamedFunction.from(nf, format, nf.name());
  }

  public static <X, I extends Individual<G, S, Q>, G, S, Q, Y> NamedFunction<X, Y> all(
      @Param(value = "beforeF", dNPM = "ea.f.identity()") Function<X, POCPopulationState<I, G, S, Q, ?>> beforeF,
      @Param(value = "afterF", dNPM = "ea.f.identity()") Function<Collection<I>, Y> afterF,
      @Param(value = "format", dS = DEFAULT_FORMAT) String format) {
    Function<POCPopulationState<I, G, S, Q, ?>, Collection<I>> f =
        state -> state.pocPopulation().all();
    return build(beforeF, f, afterF, format, "all");
  }

  public static <X, Z, Y> NamedFunction<X, Y> doubleString(
      @Param(value = "beforeF", dNPM = "ea.f.identity()") Function<X, Z> beforeF,
      @Param(value = "afterF", dNPM = "ea.f.identity()") Function<List<Double>, Y> afterF,
      @Param(value = "format", dS = DEFAULT_FORMAT) String format) {
    Function<Z, List<Double>> f = z -> {
      if (z instanceof IntString is) {
        return is.asDoubleString();
      }
      if (z instanceof BitString bs) {
        return bs.asDoubleString();
      }
      if (z instanceof List<?> list) {
        return list.stream()
            .map(i -> {
              if (i instanceof Number n) {
                return n.doubleValue();
              }
              throw new IllegalArgumentException("Cannot convert %s to double"
                  .formatted(i.getClass().getSimpleName()));
            })
            .toList();
      }
      throw new IllegalArgumentException(
          "Cannot convert %s to double string".formatted(z.getClass().getSimpleName()));
    };
    return build(beforeF, f, afterF, format, "toDoubleString");
  }

  public static <X, Y> NamedFunction<X, Y> avg(
      @Param(value = "beforeF", dNPM = "ea.f.identity()") Function<X, List<? extends Number>> beforeF,
      @Param(value = "afterF", dNPM = "ea.f.identity()") Function<Double, Y> afterF,
      @Param(value = "format", dS = "%.1f") String format) {
    Function<List<? extends Number>, Double> f =
        vs -> vs.stream().mapToDouble(Number::doubleValue).average().orElseThrow();
    return build(beforeF, f, afterF, format, "avg");
  }

  public static <X, Y> NamedFunction<X, Y> base64(
      @Param(value = "beforeF", dNPM = "ea.f.identity()") Function<X, Object> beforeF,
      @Param(value = "afterF", dNPM = "ea.f.identity()") Function<String, Y> afterF,
      @Param(value = "format", dS = "%.1f") String format) {
    Function<Object, String> f = x -> {
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
          ObjectOutputStream oos = new ObjectOutputStream(baos)) {
        oos.writeObject(x);
        oos.flush();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
      } catch (Throwable t) {
        L.warning("Cannot serialize  due to %s".formatted(t));
        return "not-serializable";
      }
    };
    return build(beforeF, f, afterF, format, "base64");
  }

  public static <X, I extends Individual<G, S, Q>, G, S, Q, Y> NamedFunction<X, Y> best(
      @Param(value = "beforeF", dNPM = "ea.f.identity()") Function<X, POCPopulationState<I, G, S, Q, ?>> beforeF,
      @Param(value = "afterF", dNPM = "ea.f.identity()") Function<I, Y> afterF,
      @Param(value = "format", dS = DEFAULT_FORMAT) String format) {
    Function<POCPopulationState<I, G, S, Q, ?>, I> f =
        state -> state.pocPopulation().all().iterator().next();
    return build(beforeF, f, afterF, format, "best");
  }

  public static <X, I extends Individual<G, S, Q>, G, S, Q, Y> NamedFunction<X, Y> quality(
      @Param(value = "beforeF", dNPM = "ea.f.identity()") Function<X, I> beforeF,
      @Param(value = "afterF", dNPM = "ea.f.identity()") Function<Q, Y> afterF,
      @Param(value = "format", dS = DEFAULT_FORMAT) String format) {
    Function<I, Q> f = Individual::quality;
    return build(beforeF, f, afterF, format, "quality");
  }
}
