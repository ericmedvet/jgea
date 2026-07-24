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

package io.github.ericmedvet.jgea.core.representation.grammar.grid;

import io.github.ericmedvet.jgea.core.representation.grammar.Grammar;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.Utils;
import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

public interface GridGrammar<T> extends Grammar<T, GridGrammar.ReferencedGrid<T>> {

  String RULE_ASSIGNMENT_STRING = "::=";
  String RULE_OPTION_SEPARATOR_STRING = "|";

  static <T> GridGrammar<T> from(T startingSymbol, Map<T, List<ReferencedGrid<T>>> rules) {
    record HardStringGrammar<T>(T startingSymbol, Map<T, List<ReferencedGrid<T>>> rules) implements GridGrammar<T> {

      @Override
      public String toString() {
        return GridGrammar.toString(this);
      }
    }
    return new HardStringGrammar<>(startingSymbol, Collections.unmodifiableMap(new LinkedHashMap<>(rules)));
  }

  static <T> GridGrammar<T> from(SequencedMap<T, List<ReferencedGrid<T>>> rules) {
    return from(rules.firstEntry().getKey(), rules);
  }

  static GridGrammar<String> load(InputStream inputStream) throws IOException {
    return load(inputStream, "UTF-8");
  }

  static GridGrammar<String> load(InputStream inputStream, String charset) throws IOException {
    SequencedMap<String, List<ReferencedGrid<String>>> rules = new LinkedHashMap<>();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, charset))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] components = line.split(Pattern.quote(RULE_ASSIGNMENT_STRING));
        String toReplaceSymbol = components[0].trim();
        String[] optionStrings = components[1].split(Pattern.quote(RULE_OPTION_SEPARATOR_STRING));
        List<ReferencedGrid<String>> options = new ArrayList<>();
        for (String optionString : optionStrings) {
          String[] rule = optionString.replaceAll("\\s+", "").split(";");
          String coordReference = rule[0].replaceAll("[()]", "");
          Grid.Key referencePoint = new Grid.Key(
              Integer.parseInt(coordReference.split(",")[0]),
              Integer.parseInt(coordReference.split(",")[1])
          );
          String[] gridRows = Arrays.copyOfRange(rule, 1, rule.length);
          int height = gridRows.length;
          int width = gridRows[0].split(",", -1).length;
          Grid<String> polyomino = Grid.create(width, height);
          int ycoord = 0;
          for (String gridRow : gridRows) {
            int xcoord = 0;
            for (String element : gridRow.split(",", -1)) {
              if (!element.isEmpty()) {
                polyomino.set(xcoord, ycoord, element);
              }
              xcoord += 1;
            }
            ycoord += 1;
          }
          ReferencedGrid<String> productionRule = new ReferencedGrid<>(referencePoint, polyomino);
          options.add(productionRule);
        }
        rules.put(toReplaceSymbol, options);
      }
    }
    return from(rules);
  }

  static <T> String toString(GridGrammar<T> gridGrammar) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<T, List<ReferencedGrid<T>>> rule : gridGrammar.rules().entrySet()) {
      sb.append(rule.getKey())
          .append(" ")
          .append(rule.getKey().equals(gridGrammar.startingSymbol()) ? "*" : "")
          .append(RULE_ASSIGNMENT_STRING + " ");
      for (ReferencedGrid<T> option : rule.getValue()) {
        sb.append(option);
        sb.append(RULE_OPTION_SEPARATOR_STRING + " ");
      }
      sb.delete(sb.length() - 2 - RULE_OPTION_SEPARATOR_STRING.length(), sb.length());
      sb.append("\n");
    }
    return sb.toString();
  }

  default <X> GridGrammar<X> map(Function<? super T, ? extends X> mapper) {
    return from(
        mapper.apply(startingSymbol()),
        rules().entrySet()
            .stream()
            .collect(
                Utils.toSequencedMap(
                    e -> mapper.apply(e.getKey()),
                    e -> e.getValue()
                        .stream()
                        .map(rg -> new ReferencedGrid<>(rg.referenceKey, rg.grid.map(t -> (X) mapper.apply(t))))
                        .toList()
                )
            )
    );
  }

  @Override
  default Collection<T> usedSymbols(ReferencedGrid<T> referencedGrid) {
    return referencedGrid.grid().values().stream().filter(Objects::nonNull).toList();
  }

  record ReferencedGrid<T>(Grid.Key referenceKey, Grid<T> grid) {}
}