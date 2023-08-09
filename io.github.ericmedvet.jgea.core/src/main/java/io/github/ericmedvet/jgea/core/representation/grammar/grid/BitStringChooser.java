/*
 * Copyright 2023 eric
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

package io.github.ericmedvet.jgea.core.representation.grammar.grid;

import io.github.ericmedvet.jgea.core.representation.grammar.Chooser;
import io.github.ericmedvet.jgea.core.representation.grammar.Developer;
import io.github.ericmedvet.jgea.core.representation.grammar.Grammar;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2023/06/16 for jgea
 */
public class BitStringChooser<S, O> implements Chooser<S, O> {
  private final BitString bitString;
  private final Grammar<S, O> grammar;
  private int i = 0;

  public BitStringChooser(BitString bitString, Grammar<S, O> grammar) {
    this.bitString = bitString;
    this.grammar = grammar;
  }

  public static <S, D, O> Function<BitString, D> mapper(
      Grammar<S, O> grammar,
      Developer<S, D, O> developer,
      D defaultDeveloped
  ) {
    return is -> {
      BitStringChooser<S, O> chooser = new BitStringChooser<>(is, grammar);
      return developer.develop(chooser).orElse(defaultDeveloped);
    };
  }

  @Override
  public Optional<O> chooseFor(S s) {
    //count options
    List<O> options = grammar.rules().get(s);
    if (options.size() == 1) {
      return Optional.of(options.get(0));
    }
    int bits = (int) Math.ceil(Math.log(options.size()) / Math.log(2d));
    if (i + bits >= bitString.size()) {
      return Optional.empty();
    }
    int index = bitString.slice(i, i + bits).toInt() % options.size();
    i = i + bits;
    return Optional.of(options.get(index));
  }

}
