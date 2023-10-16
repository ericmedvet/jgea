
package io.github.ericmedvet.jgea.core.representation.grammar;

import io.github.ericmedvet.jgea.core.IndependentFactory;

import java.util.*;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GrammarOptionStringFactory<S, C> implements IndependentFactory<GrammarOptionString<S>> {

  private final Grammar<S, C> grammar;

  private final int l;
  private final Map<S, Integer> lengths;

  public GrammarOptionStringFactory(Grammar<S, C> grammar, int l, int level) {
    this.grammar = grammar;
    this.l = l;
    //expand
    List<List<S>> last = List.of(List.of(grammar.startingSymbol()));
    List<List<S>> all = new ArrayList<>(last);
    for (int i = 0; i < level; i++) {
      last = last.stream()
          .map(list -> expand(list, grammar))
          .flatMap(Collection::stream)
          .toList();
      all.addAll(last);
    }
    //count options
    List<S> allSymbols = all.stream().flatMap(Collection::stream).toList();
    Map<S, Long> rawCounts = grammar.rules().keySet().stream()
        .filter(s -> grammar.rules().get(s).size() > 1)
        .collect(Collectors.toMap(
                s -> s,
                s -> allSymbols.stream().filter(as -> as.equals(s)).count()
            )
        );
    //adjust
    long sum = rawCounts.values().stream().mapToLong(n -> n).sum();
    rawCounts = rawCounts.entrySet().stream().collect(Collectors.toMap(
        Map.Entry::getKey,
        e -> (long) Math.max(1, Math.ceil((double) e.getValue() / (double) sum * (double) l))
    ));
    while (rawCounts.values().stream().mapToLong(n -> n).sum() > l) {
      S mostFrequentS = rawCounts.entrySet()
          .stream()
          .max(Comparator.comparingLong(Map.Entry::getValue))
          .orElseThrow().getKey();
      rawCounts.put(mostFrequentS, rawCounts.get(mostFrequentS) - 1);
    }
    //set
    lengths = rawCounts.entrySet().stream().collect(Collectors.toMap(
        Map.Entry::getKey,
        e -> e.getValue().intValue()
    ));
  }

  private static <S, C> List<List<S>> expand(List<S> list, Grammar<S, C> grammar) {
    return IntStream.range(0, list.size())
        .mapToObj(i -> expand(list, grammar, i))
        .flatMap(Collection::stream)
        .toList();
  }

  private static <S, C> List<List<S>> expand(List<S> list, Grammar<S, C> grammar, int i) {
    if (!grammar.rules().containsKey(list.get(i))) {
      return List.of();
    }
    return grammar.rules().get(list.get(i)).stream()
        .map(c -> grammar.usedSymbols(c).stream()
            .filter(us -> grammar.rules().containsKey(us))
            .toList()
        )
        .filter(l -> !l.isEmpty())
        .map(l -> Stream.concat(
            Stream.concat(list.subList(0, i).stream(), l.stream()),
            list.subList(i + 1, list.size()).stream()
        ).toList())
        .toList();
  }

  @Override
  public GrammarOptionString<S> build(RandomGenerator random) {
    return new GrammarOptionString<>(
        lengths.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> IntStream.range(0, e.getValue())
                .map(i -> random.nextInt(0, grammar.rules().get(e.getKey()).size()))
                .boxed()
                .toList()
        )),
        grammar
    );
  }
}
