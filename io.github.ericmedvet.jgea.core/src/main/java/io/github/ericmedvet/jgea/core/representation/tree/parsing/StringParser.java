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
package io.github.ericmedvet.jgea.core.representation.tree.parsing;

import io.github.ericmedvet.jnb.datastructure.Tree;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringParser<L, NT extends L, T extends L> {

  public record Configuration(
      String childrenStartDelimiter,
      String childrenEndDelimiter,
      String childrenSeparator,
      boolean allowVoid
  ) {
    public static final Configuration DEFAULT = new Configuration(
        Pattern.quote(Tree.CHILDREN_START_DELIMITER),
        Pattern.quote(Tree.CHILDREN_END_DELIMITER),
        Pattern.quote(Tree.CHILDREN_SEPARATOR),
        true
    );
  }

  private static final String VOID_REGEX = "\\s*";

  public record Token<L>(L content, int start, int end) {}

  public interface NodeParser<L> {
    Optional<Token<L>> parse(String s, int i);

    static <L> NodeParser<? extends L> fromRegex(
        String regex,
        Function<String, ? extends L> builder,
        boolean allowVoid
    ) {
      return (s, i) -> findAt(s, i, regex, allowVoid)
          .map(
              t -> new Token<>(
                  builder.apply(t.content),
                  t.start,
                  t.end
              )
          );
    }

    static <E extends Enum<E>> List<? extends NodeParser<? extends E>> fromEnum(Class<E> enumClass, boolean allowVoid) {
      SequencedMap<String, E> namedValues = Arrays.stream(enumClass.getEnumConstants())
          .collect(
              Collectors.toMap(
                  Enum::toString,
                  e -> e,
                  (e1, e2) -> e1,
                  LinkedHashMap::new
              )
          );
      return namedValues.keySet()
          .stream()
          .map(n -> (NodeParser<? extends E>) fromRegex(Pattern.quote(n), namedValues::get, allowVoid))
          .toList();
    }
  }

  private final List<? extends NodeParser<? extends NT>> nonTerminalParsers;
  private final List<? extends NodeParser<? extends T>> terminalParsers;
  private final BiPredicate<NT, Integer> arityPredicate;
  private final Configuration configuration;

  public StringParser(
      List<? extends NodeParser<? extends NT>> nonTerminalParsers,
      List<? extends NodeParser<? extends T>> terminalParsers,
      BiPredicate<NT, Integer> arityPredicate,
      Configuration configuration
  ) {
    this.nonTerminalParsers = nonTerminalParsers;
    this.terminalParsers = terminalParsers;
    this.arityPredicate = arityPredicate;
    this.configuration = configuration;
  }

  public Tree<L> parse(String string) {
    return parseNode(string, 0).orElseThrow().content;
  }

  private static Optional<Token<String>> findAt(String string, int i, String regex, boolean allowVoid) {
    Matcher matcher = Pattern
        .compile(allowVoid ? (VOID_REGEX + regex + VOID_REGEX) : regex)
        .matcher(string);
    if (!matcher.find(i) || matcher.start() != i) {
      return Optional.empty();
    }
    return Optional.of(
        new Token<>(
            allowVoid ? string.substring(matcher.start(), matcher.end())
                .replaceAll("\\A" + VOID_REGEX, "")
                .replaceAll(VOID_REGEX + "\\z", "") : string.substring(matcher.start(), matcher.end()),
            i,
            matcher.end()
        )
    );
  }

  private Optional<Token<Tree<L>>> parseNode(String string, int i) {
    int finalI = i;
    Optional<? extends Token<? extends NT>> ntToken = nonTerminalParsers.stream()
        .map(np -> np.parse(string, finalI))
        .filter(Optional::isPresent)
        .findFirst()
        .map(Optional::orElseThrow);
    List<Tree<L>> children = new ArrayList<>();
    if (ntToken.isPresent()) {
      i = ntToken.get().end();
      i = findAt(string, i, configuration.childrenStartDelimiter, configuration.allowVoid).orElseThrow().end;
      while (true) {
        if (!children.isEmpty()) {
          Optional<Token<String>> separator = findAt(
              string,
              i,
              configuration.childrenSeparator,
              configuration.allowVoid
          );
          if (separator.isEmpty()) {
            break;
          }
          i = separator.get().end;
        }
        Optional<Token<Tree<L>>> child = parseNode(string, i);
        i = child.orElseThrow().end;
        children.add(child.orElseThrow().content);
      }
      i = findAt(string, i, configuration.childrenEndDelimiter, configuration.allowVoid).orElseThrow().end;
      if (!arityPredicate.test(ntToken.get().content(), children.size())) {
        throw new IllegalArgumentException(
            "Wrong arity %d for operator %s".formatted(
                children.size(),
                ntToken.get().content()
            )
        );
      }
      return Optional.of(
          new Token<>(
              new Tree<>(ntToken.get().content(), children),
              finalI,
              i
          )
      );
    }
    Optional<? extends Token<? extends T>> tToken = terminalParsers.stream()
        .map(np -> np.parse(string, finalI))
        .filter(Optional::isPresent)
        .findFirst()
        .map(Optional::orElseThrow);
    if (tToken.isPresent()) {
      return Optional.of(
          new Token<>(
              new Tree<>(tToken.get().content()),
              i,
              tToken.get().end()
          )
      );
    }
    return Optional.empty();
  }

}