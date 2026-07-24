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

package io.github.ericmedvet.jgea.core.representation.grammar.string;

import io.github.ericmedvet.jgea.core.representation.grammar.Grammar;
import io.github.ericmedvet.jnb.datastructure.Utils;
import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

public interface StringGrammar<T> extends Grammar<T, List<T>> {

  String RULE_ASSIGNMENT_STRING = "::=";
  String RULE_OPTION_SEPARATOR_STRING = "|";

  static <T> StringGrammar<T> from(SequencedMap<T, List<List<T>>> rules) {
    return from(rules.firstEntry().getKey(), rules);
  }

  static <T> StringGrammar<T> from(T startingSymbol, Map<T, List<List<T>>> rules) {
    record HardStringGrammar<T>(T startingSymbol, Map<T, List<List<T>>> rules) implements StringGrammar<T> {

      @Override
      public String toString() {
        return StringGrammar.toString(this);
      }
    }
    return new HardStringGrammar<>(startingSymbol, Collections.unmodifiableMap(new LinkedHashMap<>(rules)));
  }

  static StringGrammar<String> load(InputStream inputStream) throws IOException {
    return load(inputStream, "UTF-8");
  }

  static StringGrammar<String> load(InputStream inputStream, String charset) throws IOException {
    SequencedMap<String, List<List<String>>> rules = new LinkedHashMap<>();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, charset))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] components = line.split(Pattern.quote(RULE_ASSIGNMENT_STRING));
        String toReplaceSymbol = components[0].trim();
        String[] optionStrings = components[1].split(Pattern.quote(RULE_OPTION_SEPARATOR_STRING));
        List<List<String>> options = new ArrayList<>();
        for (String optionString : optionStrings) {
          List<String> symbols = new ArrayList<>();
          for (String symbol : optionString.split("\\s+")) {
            if (!symbol.trim().isEmpty()) {
              symbols.add(symbol.trim());
            }
          }
          if (!symbols.isEmpty()) {
            options.add(symbols);
          }
        }
        rules.put(toReplaceSymbol, options);
      }
    }
    return from(rules);
  }

  static <T> String toString(StringGrammar<T> grammar) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<T, List<List<T>>> rule : grammar.rules().entrySet()) {
      sb.append(rule.getKey())
          .append(" ")
          .append(rule.getKey().equals(grammar.startingSymbol()) ? "*" : "")
          .append(RULE_ASSIGNMENT_STRING + " ");
      for (List<T> option : rule.getValue()) {
        for (T symbol : option) {
          sb.append(symbol).append(" ");
        }
        sb.append(RULE_OPTION_SEPARATOR_STRING + " ");
      }
      sb.delete(sb.length() - 2 - RULE_OPTION_SEPARATOR_STRING.length(), sb.length());
      sb.append("\n");
    }
    return sb.toString();
  }

  default <X> StringGrammar<X> map(Function<? super T, ? extends X> mapper) {
    return from(
        (X) mapper.apply(startingSymbol()),
        rules().entrySet()
            .stream()
            .collect(
                Utils.toSequencedMap(
                    e -> mapper.apply(e.getKey()),
                    e -> e.getValue()
                        .stream()
                        .map(o -> o.stream().map(t -> (X) mapper.apply(t)).toList())
                        .toList()
                )
            )
    );
  }

  @Override
  default Collection<T> usedSymbols(List<T> ts) {
    return ts;
  }
}