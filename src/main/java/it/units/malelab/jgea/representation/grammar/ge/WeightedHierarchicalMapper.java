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

package it.units.malelab.jgea.representation.grammar.ge;

import com.google.common.collect.Range;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.sequence.bit.BitString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author eric
 */
public class WeightedHierarchicalMapper<T> extends HierarchicalMapper<T> {

  protected final Map<T, Integer> weightsMap;
  private final int expressivenessDepth;
  private final boolean weightOptions;
  private final boolean weightChildren;

  public WeightedHierarchicalMapper(int expressivenessDepth, Grammar<T> grammar) {
    this(expressivenessDepth, false, true, grammar);
  }

  public WeightedHierarchicalMapper(int expressivenessDepth, boolean weightOptions, boolean weightChildren, Grammar<T> grammar) {
    super(grammar);
    this.expressivenessDepth = expressivenessDepth;
    this.weightOptions = weightOptions;
    this.weightChildren = weightChildren;
    weightsMap = new HashMap<>();
    for (List<List<T>> options : grammar.getRules().values()) {
      for (List<T> option : options) {
        for (T symbol : option) {
          if (!weightsMap.keySet().contains(symbol)) {
            weightsMap.put(symbol, countOptions(symbol, 0, expressivenessDepth, grammar));
          }
        }
      }
    }
    for (T symbol : weightsMap.keySet()) { //modify to log_2 for non-terminals: the terminals have 1 (should be set to 0)
      int options = weightsMap.get(symbol);
      int bits = (int) Math.ceil(Math.log10(options) / Math.log10(2d));
      weightsMap.put(symbol, bits);
    }
  }

  private static <T> int countOptions(T symbol, int level, int maxLevel, Grammar<T> g) {
    List<List<T>> options = g.getRules().get(symbol);
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
  protected List<Range<Integer>> getChildrenSlices(Range<Integer> range, List<T> symbols) {
    if (!weightChildren) {
      return super.getChildrenSlices(range, symbols);
    }
    List<Range<Integer>> ranges;
    if (symbols.size() > (range.upperEndpoint() - range.lowerEndpoint())) {
      ranges = new ArrayList<>(symbols.size());
      for (T symbol : symbols) {
        ranges.add(Range.closedOpen(range.lowerEndpoint(), range.lowerEndpoint()));
      }
    } else {
      List<Integer> sizes = new ArrayList<>(symbols.size());
      int overallWeight = 0;
      for (T symbol : symbols) {
        overallWeight = overallWeight + weightsMap.get(symbol);
      }
      for (T symbol : symbols) {
        sizes.add((int) Math.floor((double) weightsMap.get(symbol) / (double) overallWeight * (double) (range.upperEndpoint() - range.lowerEndpoint())));
      }
      ranges = slices(range, sizes);
    }
    return ranges;
  }

  @Override
  protected double optionSliceWeight(BitString slice) {
    if (!weightOptions) {
      return super.optionSliceWeight(slice);
    }
    return (double) slice.count();
  }

  @Override
  protected List<Range<Integer>> getOptionSlices(Range<Integer> range, List<List<T>> options) {
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
    return slices(range, sizes);
  }

  @Override
  public String toString() {
    return "WeightedHierarchicalMapper{" + "maxDepth=" + expressivenessDepth + ", weightOptions=" + weightOptions + ", weightChildren=" + weightChildren + '}';
  }

}
