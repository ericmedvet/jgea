package io.github.ericmedvet.jgea.core.distance;

import io.github.ericmedvet.jgea.core.representation.grammar.GrammarOptionString;
public class GrammarOptionStringDistance<S> implements Distance<GrammarOptionString<S>> {
  @Override
  public Double apply(GrammarOptionString<S> gos1, GrammarOptionString<S> gos2) {
    for (S s : gos1.options().keySet()) {
      if (gos1.options().get(s).size() != gos2.options().get(s).size()) {
        throw new IllegalArgumentException(String.format(
            "Sequences size should be the same for symbol %d (%d vs. %d)",
            s,
            gos1.options().get(s).size(),
            gos2.options().get(s).size()
        ));
      }
    }
    int sum = 0;
    for (S s : gos1.options().keySet()) {
      for (int i = 0; i < gos1.options().get(s).size(); i = i + 1) {
        sum = sum + Math.abs(gos1.options().get(s).get(i) - gos2.options().get(s).get(i));
      }
    }
    return (double) sum;
  }
}
