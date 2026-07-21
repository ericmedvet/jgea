/*-
 * ========================LICENSE_START=================================
 * jgea-core
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
package io.github.ericmedvet.jgea.core.representation.tree.numeric;

import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element.Constant;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jnb.datastructure.Tree;
import io.github.ericmedvet.jnb.datastructure.Utils;
import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.random.RandomGenerator;

public class ConstantsMutation implements Mutation<Tree<Element>> {

  private final double sigma;

  public ConstantsMutation(double sigma) {
    this.sigma = sigma;
  }

  @Override
  public Tree<Element> mutate(Tree<Element> parent, RandomGenerator random) {
    SequencedMap<List<Integer>, Element> lineageMap = parent.lineages().stream()
        .collect(Utils.toSequencedMap(lineage -> parent.descendant(lineage).label()));
    List<Integer> toVariateLineage = Misc.pickRandomly(
        lineageMap.keySet().stream().filter(l -> lineageMap.get(l) instanceof Constant).toList(),
        random);
    double currentValue = ((Constant) lineageMap.get(toVariateLineage)).value();
    lineageMap.put(toVariateLineage,
        new Constant(currentValue * (1d + random.nextGaussian() * sigma)));
    return Tree.from(l -> Optional.ofNullable(lineageMap.get(l)));
  }
}