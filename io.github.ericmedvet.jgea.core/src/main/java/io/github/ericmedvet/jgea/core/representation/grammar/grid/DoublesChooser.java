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
import io.github.ericmedvet.jsdynsym.core.DoubleRange;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2023/06/16 for jgea
 */
public class DoublesChooser<S, O> implements Chooser<S, O> {
  private final List<Double> values;
  private final Grammar<S, O> grammar;
  private int i = 0;

  public DoublesChooser(List<Double> values, Grammar<S, O> grammar) {
    this.values = values;
    this.grammar = grammar;
  }

  public static <T, D, O> Function<List<Double>, D> mapper(
      Grammar<T,O> grammar,
      Developer<T, D, O> developer,
      D defaultDeveloped
  ) {
    return values -> {
      DoublesChooser<T, O> chooser = new DoublesChooser<>(values, grammar);
      return developer.develop(chooser).orElse(defaultDeveloped);
    };
  }

  @Override
  public Optional<O> chooseFor(S s) {
    if (i >= values.size()) {
      return Optional.empty();
    }
    List<O> options = grammar.rules().get(s);
    double v = DoubleRange.UNIT.clip(values.get(i));
    v = v * options.size();
    v = Math.floor(v);
    int index = (int) Math.min(v, options.size() - 1);
    i = i + 1;
    return Optional.of(options.get(index));
  }

}
