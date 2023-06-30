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

package io.github.ericmedvet.jgea.core.representation.grid;

import io.github.ericmedvet.jsdynsym.grid.Grid;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

public class GridGrammar<T> implements Serializable {

  public static final String RULE_ASSIGNMENT_STRING = "::=";
  public static final String RULE_OPTION_SEPARATOR_STRING = "|";
  Map<T, List<ReferencedGrid<T>>> rules;
  private T startingSymbol;

  public GridGrammar() {
    rules = new LinkedHashMap<>();
  }

  public record ReferencedGrid<T>(Grid.Key referenceKey, Grid<T> grid) {}

  public static GridGrammar<String> load(InputStream inputStream) throws IOException {
    return load(inputStream, "UTF-8");
  }

  public static GridGrammar<String> load(InputStream inputStream, String charset) throws IOException {
    GridGrammar<String> grammar = new GridGrammar<>();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, charset))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] components = line.split(Pattern.quote(RULE_ASSIGNMENT_STRING));
        String toReplaceSymbol = components[0].trim();
        String[] optionStrings = components[1].split(Pattern.quote(RULE_OPTION_SEPARATOR_STRING));
        if (grammar.getStartingSymbol() == null) {
          grammar.setStartingSymbol(toReplaceSymbol);
        }
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
        grammar.getRules().put(toReplaceSymbol, options);
      }
    }
    return grammar;
  }

  public Map<T, List<ReferencedGrid<T>>> getRules() {
    return rules;
  }

  public T getStartingSymbol() {
    return startingSymbol;
  }

  public void setStartingSymbol(T startingSymbol) {
    this.startingSymbol = startingSymbol;
  }

  public <X> GridGrammar<X> map(Function<T, X> function) {
    GridGrammar<X> mapped = new GridGrammar<>();
    rules.forEach((nt, list) -> mapped.rules.put(
        function.apply(nt),
        list.stream()
            .map(rg -> new ReferencedGrid<>(
                rg.referenceKey(),
                rg.grid().map(function)
            )).toList()
    ));
    mapped.startingSymbol = function.apply(startingSymbol);
    return mapped;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<T, List<ReferencedGrid<T>>> rule : rules.entrySet()) {
      sb.append(rule.getKey()).append(" ").append(rule.getKey().equals(startingSymbol) ? "*" : "").append(
          RULE_ASSIGNMENT_STRING + " ");
      for (ReferencedGrid<T> option : rule.getValue()) {
        sb.append(option);
        sb.append(RULE_OPTION_SEPARATOR_STRING + " ");
      }
      sb.delete(sb.length() - 2 - RULE_OPTION_SEPARATOR_STRING.length(), sb.length());
      sb.append("\n");
    }
    return sb.toString();
  }

}
