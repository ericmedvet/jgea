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

package io.github.ericmedvet.jgea.problem.extraction.string;

import com.google.common.collect.Sets;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.problem.extraction.ExtractionFitness;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class RegexGrammar extends StringGrammar<String> {

  public static final String TO_BE_ESCAPED = "{}[]()?+*.\\^";

  public RegexGrammar(Collection<String> texts, Set<Option> options) {
    this(texts.stream()
        .map(s -> s.chars().mapToObj(c -> (char) c).collect(Collectors.toSet()))
        .reduce(Sets::union)
        .orElse(Set.of()), options);
  }

  public RegexGrammar(ExtractionFitness<Character> fitness, Set<Option> options) {
    this(fitness.getDesiredExtractions().stream().map(r -> fitness.getSequence().subList(
        r.lowerEndpoint(),
        r.upperEndpoint()
    ).stream().collect(Collectors.toSet())).reduce(Sets::union).orElse(Set.of()), options);
  }

  public RegexGrammar(Set<Character> alphabet, Set<Option> options) {
    super();
    rules().put("<regex>", l(l("<concat>")));
    if (options.contains(Option.OR)) {
      rules().get("<regex>").add(l("<union>"));
      rules().put("<union>", l(l("<regex>", "|", "<concat>")));
    }
    rules().put("<concat>", l(l("<term>", "<concat>"), l("<term>")));
    if (options.contains(Option.ENHANCED_CONCATENATION)) {
      rules().get("<concat>").add(l("<concat>", "<concat>"));
    }
    rules().put("<term>", l(l("<element>")));
    if (options.contains(Option.QUANTIFIERS)) {
      rules().get("<term>").add(l("<element>", "<quantifier>"));
      rules().put("<quantifier>", l(l("?+"), l("++"), l("*+")));
    }
    rules().put("<term>", l(l("<element>")));
    if (options.contains(Option.NON_EMPTY_QUANTIFIER)) {
      rules().get("<term>").add(l("<element>", "<quantifier>"));
      rules().put("<quantifier>", l(l("++")));
    }
    if (options.contains(Option.BOUNDED_QUANTIFIERS)) {
      rules().get("<term>").add(l("<element>", "{", "<digit>", ",", "<digit>", "}"));
      rules().put("<digit>", l(l("1"), l("2"), l("3"), l("4"), l("5"), l("6"), l("7"), l("8"), l("9")));
    }
    rules().put("<element>", l(l("<char>")));
    if (options.contains(Option.CHAR_CLASS)) {
      rules().get("<element>").add(l("[", "<constChars>", "]"));
      rules().put("<constChars>", l(l("<constChar>"), l("<constChars>", "<constChar>")));
      if (options.contains(Option.ENHANCED_CONCATENATION)) {
        rules().get("<constChars>").add(l("<constChars>", "<constChars>"));
      }
    }
    if (options.contains(Option.NEGATED_CHAR_CLASS)) {
      rules().get("<element>").add(l("[^", "<constChars>", "]"));
      rules().put("<constChars>", l(l("<constChar>"), l("<constChars>", "<constChar>")));
    }
    if (options.contains(Option.NON_CAPTURING_GROUP)) {
      rules().get("<element>").add(l("(?:", "<regex>", ")"));
    }
    rules().put("<char>", l(l("<constChar>")));
    if (options.contains(Option.ANY)) {
      rules().get("<char>").add(l("."));
    }
    rules().put("<constChar>", new ArrayList<>());
    for (Character character : alphabet) {
      rules().get("<constChar>").add(l(escape(character.toString())));
    }
    setStartingSymbol("<regex>");
  }

  public enum Option {
    OR, QUANTIFIERS, NON_EMPTY_QUANTIFIER, BOUNDED_QUANTIFIERS, CHAR_CLASS, NEGATED_CHAR_CLASS, NON_CAPTURING_GROUP,
    ANY, ENHANCED_CONCATENATION
  }

  private String escape(String c) {
    if (TO_BE_ESCAPED.contains(c)) {
      return "\\" + c;
    }
    return c;
  }

  @SafeVarargs
  private <T> List<T> l(T... ts) {
    return new ArrayList<>(Arrays.asList(ts));
  }

}
