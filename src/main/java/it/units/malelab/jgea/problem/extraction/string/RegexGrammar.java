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

package it.units.malelab.jgea.problem.extraction.string;

import com.google.common.collect.Sets;
import it.units.malelab.jgea.problem.extraction.ExtractionFitness;
import it.units.malelab.jgea.representation.grammar.Grammar;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class RegexGrammar extends Grammar<String> {

  public enum Option {
    OR, QUANTIFIERS, NON_EMPTY_QUANTIFIER, BOUNDED_QUANTIFIERS, CHAR_CLASS, NEGATED_CHAR_CLASS, NON_CAPTURING_GROUP, ANY, ENHANCED_CONCATENATION
  }

  public static final String TO_BE_ESCAPED = "{}[]()?+*.\\^";

  public RegexGrammar(Collection<String> texts, Set<Option> options) {
    this(
        texts.stream()
            .map(s -> s.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toSet()))
            .reduce(Sets::union)
            .orElse(Set.of()),
        options
    );
  }

  public RegexGrammar(ExtractionFitness<Character> fitness, Set<Option> options) {
    this(
        fitness.getDesiredExtractions().stream()
            .map(r -> fitness.getSequence().subList(r.lowerEndpoint(), r.upperEndpoint()).stream()
                .collect(Collectors.toSet()))
            .reduce(Sets::union)
            .orElse(Set.of()),
        options
    );
  }

  public RegexGrammar(Set<Character> alphabet, Set<Option> options) {
    super();
    getRules().put("<regex>", l(l("<concat>")));
    if (options.contains(Option.OR)) {
      getRules().get("<regex>").add(l("<union>"));
      getRules().put("<union>", l(l("<regex>", "|", "<concat>")));
    }
    getRules().put("<concat>", l(l("<term>", "<concat>"), l("<term>")));
    if (options.contains(Option.ENHANCED_CONCATENATION)) {
      getRules().get("<concat>").add(l("<concat>", "<concat>"));
    }
    getRules().put("<term>", l(l("<element>")));
    if (options.contains(Option.QUANTIFIERS)) {
      getRules().get("<term>").add(l("<element>", "<quantifier>"));
      getRules().put("<quantifier>", l(l("?+"), l("++"), l("*+")));
    }
    getRules().put("<term>", l(l("<element>")));
    if (options.contains(Option.NON_EMPTY_QUANTIFIER)) {
      getRules().get("<term>").add(l("<element>", "<quantifier>"));
      getRules().put("<quantifier>", l(l("++")));
    }
    if (options.contains(Option.BOUNDED_QUANTIFIERS)) {
      getRules().get("<term>").add(l("<element>", "{", "<digit>", ",", "<digit>", "}"));
      getRules().put("<digit>", l(l("1"), l("2"), l("3"), l("4"), l("5"), l("6"), l("7"), l("8"), l("9")));
    }
    getRules().put("<element>", l(l("<char>")));
    if (options.contains(Option.CHAR_CLASS)) {
      getRules().get("<element>").add(l("[", "<constChars>", "]"));
      getRules().put("<constChars>", l(l("<constChar>"), l("<constChars>", "<constChar>")));
      if (options.contains(Option.ENHANCED_CONCATENATION)) {
        getRules().get("<constChars>").add(l("<constChars>", "<constChars>"));
      }
    }
    if (options.contains(Option.NEGATED_CHAR_CLASS)) {
      getRules().get("<element>").add(l("[^", "<constChars>", "]"));
      getRules().put("<constChars>", l(l("<constChar>"), l("<constChars>", "<constChar>")));
    }
    if (options.contains(Option.NON_CAPTURING_GROUP)) {
      getRules().get("<element>").add(l("(?:", "<regex>", ")"));
    }
    getRules().put("<char>", l(l("<constChar>")));
    if (options.contains(Option.ANY)) {
      getRules().get("<char>").add(l("."));
    }
    getRules().put("<constChar>", new ArrayList<>());
    for (Character character : alphabet) {
      getRules().get("<constChar>").add(l(escape(character.toString())));
    }
    setStartingSymbol("<regex>");
  }

  private String escape(String c) {
    if (TO_BE_ESCAPED.contains(c)) {
      return "\\" + c;
    }
    return c;
  }

  private <T> List<T> l(T... ts) {
    return new ArrayList<>(Arrays.asList(ts));
  }

}
