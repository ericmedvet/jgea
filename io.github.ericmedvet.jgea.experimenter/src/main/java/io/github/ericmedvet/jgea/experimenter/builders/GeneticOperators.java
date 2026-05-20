/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
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

import io.github.ericmedvet.jgea.core.operator.Crossover;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitStringFlipMutation;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitStringUniformCrossover;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntStringFlipMutation;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntStringSymbolCopy;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntStringUniformCrossover;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.GaussianMutation;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.HypercubeGeometricCrossover;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.SegmentGeometricCrossover;
import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Utils;
import java.util.List;
import java.util.function.Function;

@Discoverable(prefixTemplate = "ea.representation|r.geneticOperator|go")
public class GeneticOperators {
  private GeneticOperators() {
  }

  @Cacheable
  public static Function<BitString, Mutation<BitString>> bsFlipMutation(
      @Param(value = "pMutRate", dD = 1d) double pMutRate
  ) {
    return eBs -> new BitStringFlipMutation(pMutRate * (double) eBs.size());
  }


  @Cacheable
  public static Function<BitString, Crossover<BitString>> bsUniformXover() {
    return eIs -> new BitStringUniformCrossover();
  }

  @Cacheable
  public static <X> Function<X, Mutation<X>> composedMutation(
      @Param("mutation1") Function<X, Mutation<X>> mutation1,
      @Param("mutation2") Function<X, Mutation<X>> mutation2
  ) {
    return eX -> Mutation.from(mutation1.apply(eX).andThen(mutation2.apply(eX)));
  }

  @Cacheable
  public static <X> Function<X, Crossover<X>> composedXover(
      @Param("xover") Function<X, Crossover<X>> xover,
      @Param("mutation") Function<X, Mutation<X>> mutation
  ) {
    return eX -> Crossover.from(xover.apply(eX).andThen(mutation.apply(eX)));
  }

  @Cacheable
  public static <X> Function<X, Crossover<X>> deterministicXover(
      @Param("first") boolean first
  ) {
    return eX -> Crossover.deterministicCopy(first);
  }

  @Cacheable
  public static Function<List<Double>, Mutation<List<Double>>> dsGaussianMutation(
      @Param(value = "sigmaMut", dD = 0.35d) double sigmaMut
  ) {
    return eDs -> new GaussianMutation(sigmaMut);
  }

  @Cacheable
  public static Function<List<Double>, Crossover<List<Double>>> dsHypercubeGeometricXover(
      @Param(value = "ext", dD = 1d) double ext
  ) {
    return eDs -> new HypercubeGeometricCrossover(DoubleRange.UNIT.extend(ext));
  }

  @Cacheable
  public static Function<List<Double>, Crossover<List<Double>>> dsSegmentGeometricXover(
      @Param(value = "ext", dD = 1d) double ext
  ) {
    return eDs -> new SegmentGeometricCrossover(DoubleRange.UNIT.extend(ext));
  }

  @Cacheable
  public static Function<IntString, Mutation<IntString>> isFlipMutation(
      @Param(value = "pMutRate", dD = 1d) double pMutRate
  ) {
    return eIs -> new IntStringFlipMutation(pMutRate * (double) eIs.size());
  }

  @Cacheable
  public static Function<IntString, Mutation<IntString>> isSymbolCopyMutation() {
    return eIs -> new IntStringSymbolCopy();
  }

  @Cacheable
  public static Function<IntString, Crossover<IntString>> isUniformXover() {
    return eIs -> new IntStringUniformCrossover();
  }

  @Cacheable
  public static <X> Function<List<X>, Crossover<List<X>>> listCrossover(
      @Param("crossover") Function<X, Crossover<X>> crossover
  ) {
    return eXs -> crossover.apply(eXs.getFirst()).list();
  }

  @Cacheable
  public static <X> Function<List<X>, Mutation<List<X>>> listMutation(
      @Param("mutation") Function<X, Mutation<X>> mutation
  ) {
    return eXs -> mutation.apply(eXs.getFirst()).list();
  }

  @Cacheable
  public static <X> Function<X, Mutation<X>> oneMutation(
      @Param("mutations") List<Function<X, Mutation<X>>> mutations
  ) {
    return eX -> Mutation.oneOf(
        mutations.stream()
            .map(m -> m.apply(eX))
            .collect(Utils.toSequencedMap(m -> 1d))
    );
  }

  @Cacheable
  public static <X> Function<X, Crossover<X>> oneXover(
      @Param("xovers") List<Function<X, Crossover<X>>> xovers
  ) {
    return eX -> Crossover.oneOf(
        xovers.stream()
            .map(c -> c.apply(eX))
            .collect(Utils.toSequencedMap(c -> 1d))
    );
  }

}