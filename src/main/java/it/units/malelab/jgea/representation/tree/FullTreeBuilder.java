/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
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

package it.units.malelab.jgea.representation.tree;

import it.units.malelab.jgea.core.IndependentFactory;

import java.util.Random;
import java.util.function.ToIntFunction;

/**
 * @author eric
 */
public class FullTreeBuilder<N> implements TreeBuilder<N> {

  protected final ToIntFunction<N> arityFunction;
  protected final IndependentFactory<N> nonTerminalFactory;
  protected final IndependentFactory<N> terminalFactory;

  public FullTreeBuilder(ToIntFunction<N> arityFunction, IndependentFactory<N> nonTerminalFactory, IndependentFactory<N> terminalFactory) {
    this.arityFunction = arityFunction;
    this.nonTerminalFactory = nonTerminalFactory;
    this.terminalFactory = terminalFactory;
  }

  @Override
  public Tree<N> build(Random random, int h) {
    if (h == 1) {
      return Tree.of(terminalFactory.build(random));
    }
    Tree<N> t = Tree.of(nonTerminalFactory.build(random));
    int nChildren = arityFunction.applyAsInt(t.content());
    for (int i = 0; i < nChildren; i++) {
      t.addChild(build(random, h - 1));
    }
    return t;
  }

}
