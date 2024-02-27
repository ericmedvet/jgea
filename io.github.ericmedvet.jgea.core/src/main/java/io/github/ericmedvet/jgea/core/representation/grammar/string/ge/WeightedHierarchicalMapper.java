/*-
 * ========================LICENSE_START=================================
 * jgea-core
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

package io.github.ericmedvet.jgea.core.representation.grammar.string.ge;

import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.util.IntRange;
import io.github.ericmedvet.jgea.core.util.Misc;
import java.util.*;

public class WeightedHierarchicalMapper<T> extends HierarchicalMapper<T> {

  protected final Map<T, Integer> weightsMap;
  private final int expressivenessDepth;
  private final boolean weightOptions;
  private final boolean weightChildren;

  public WeightedHierarchicalMapper(int expressivenessDepth, StringGrammar<T> grammar) {
    this(expressivenessDepth, false, true, grammar);
  }

  public WeightedHierarchicalMapper(
      int expressivenessDepth, boolean weightOptions, boolean weightChildren, StringGrammar<T> grammar) {
    super(grammar);
    this.expressivenessDepth = expressivenessDepth;
    this.weightOptions = weightOptions;
    this.weightChildren = weightChildren;
    weightsMap = new HashMap<>();
    for (List<List<T>> options : grammar.rules().values()) {
      for (List<T> option : options) {
        for (T symbol : option) {
          if (!weightsMap.containsKey(symbol)) {
            weightsMap.put(symbol, countOptions(symbol, 0, expressivenessDepth, grammar));
          }
        }
      }
    }
    for (T symbol : weightsMap.keySet()) { // modify to log_2 for non-terminals: the terminals have 1 (should be set
      // to 0)
      int options = weightsMap.get(symbol);
      int bits = (int) Math.ceil(Math.log10(options) / Math.log10(2d));
      weightsMap.put(symbol, bits);
    }
  }

  private static <T> int countOptions(T symbol, int level, int maxLevel, StringGrammar<T> g) {
    List<List<T>> options = g.rules().get(symbol);
    if (options == null) {
      return 1;
    }
    if (level >= maxLevel) {
      return options.size();
    }
    int count = 0;
    for (List<T> option : options) {
      for (T optionSymbol : option) {
        count = count + countOptions(optionSymbol, level + 1, maxLevel, g);
      }
    }
    return count;
  }

  @Override
  protected List<IntRange> getChildrenSlices(IntRange range, List<T> symbols) {
    if (!weightChildren) {
      return super.getChildrenSlices(range, symbols);
    }
    List<IntRange> ranges;
    if (symbols.size() > range.extent()) {
      ranges = Collections.nCopies(symbols.size(), range);
    } else {
      List<Integer> sizes = new ArrayList<>(symbols.size());
      int overallWeight = 0;
      for (T symbol : symbols) {
        overallWeight = overallWeight + weightsMap.get(symbol);
      }
      for (T symbol : symbols) {
        sizes.add((int)
            Math.floor((double) weightsMap.get(symbol) / (double) overallWeight * (double) range.extent()));
      }
      ranges = Misc.slices(range, sizes);
    }
    return ranges;
  }

  @Override
  protected List<IntRange> getOptionSlices(IntRange range, List<List<T>> options) {
    if (!weightOptions) {
      return super.getOptionSlices(range, options);
    }
    List<Integer> sizes = new ArrayList<>(options.size());
    for (List<T> option : options) {
      int w = 1;
      for (T symbol : option) {
        w = w * Math.max(weightsMap.getOrDefault(symbol, 1), 1);
      }
      sizes.add(w);
    }
    return Misc.slices(range, sizes);
  }

  @Override
  protected double optionSliceWeight(BitString slice) {
    if (!weightOptions) {
      return super.optionSliceWeight(slice);
    }
    return slice.nOfOnes();
  }

  @Override
  public String toString() {
    return "WeightedHierarchicalMapper{"
        + "maxDepth="
        + expressivenessDepth
        + ", weightOptions="
        + weightOptions
        + ", weightChildren="
        + weightChildren
        + '}';
  }
}
