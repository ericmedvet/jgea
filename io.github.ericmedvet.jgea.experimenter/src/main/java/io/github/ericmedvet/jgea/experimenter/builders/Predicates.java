/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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
package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Discoverable(prefixTemplate = "ea.predicate")
public class Predicates {
  private Predicates() {}

  @SuppressWarnings("unused")
  public static <X> Predicate<X> all(@Param("conditions") List<Predicate<X>> conditions) {
    return x -> conditions.stream().allMatch(p -> p.test(x));
  }

  @SuppressWarnings("unused")
  public static Predicate<?> always() {
    return t -> true;
  }

  @SuppressWarnings("unused")
  public static <X> Predicate<X> any(@Param("conditions") List<Predicate<X>> conditions) {
    return x -> conditions.stream().anyMatch(p -> p.test(x));
  }

  @SuppressWarnings("unused")
  public static <X, T> Predicate<X> eq(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<X, T> function, @Param("v") T v) {
    return x -> function.apply(x).equals(v);
  }

  @SuppressWarnings("unused")
  public static <X> Predicate<X> gt(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<X, ? extends Number> function,
      @Param("t") double t) {
    return x -> function.apply(x).doubleValue() > t;
  }

  @SuppressWarnings("unused")
  public static <X> Predicate<X> gtEq(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<X, ? extends Number> function,
      @Param("t") double t) {
    return x -> function.apply(x).doubleValue() >= t;
  }

  @SuppressWarnings("unused")
  public static <X> Predicate<X> inD(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<X, Double> function,
      @Param("values") List<Double> values) {
    return x -> values.contains(function.apply(x));
  }

  @SuppressWarnings("unused")
  public static <X> Predicate<X> inL(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<X, Long> function,
      @Param("values") List<Integer> values) {
    return x -> values.contains(function.apply(x).intValue());
  }

  @SuppressWarnings("unused")
  public static <X> Predicate<X> inI(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<X, Integer> function,
      @Param("values") List<Integer> values) {
    return x -> values.contains(function.apply(x));
  }

  @SuppressWarnings("unused")
  public static <X> Predicate<X> inS(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<X, String> function,
      @Param("values") List<String> values) {
    return x -> values.contains(function.apply(x));
  }

  @SuppressWarnings("unused")
  public static <X> Predicate<X> lt(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<X, ? extends Number> function,
      @Param("t") double t) {
    return x -> function.apply(x).doubleValue() < t;
  }

  @SuppressWarnings("unused")
  public static <X> Predicate<X> ltEq(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<X, ? extends Number> function,
      @Param("t") double t) {
    return x -> function.apply(x).doubleValue() <= t;
  }

  @SuppressWarnings("unused")
  public static <X> Predicate<X> matches(
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<X, String> function,
      @Param("regex") String regex) {
    Pattern p = Pattern.compile(regex);
    return x -> p.matcher(function.apply(x)).matches();
  }
}
