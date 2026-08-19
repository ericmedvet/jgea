/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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
package io.github.ericmedvet.jgea.experimenter;

import io.github.ericmedvet.jgea.core.IndependentFactory;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp.FullGrammarTreeBuilder;
import io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp.GrowGrammarTreeBuilder;
import io.github.ericmedvet.jgea.core.representation.tree.GrowTreeBuilder;
import io.github.ericmedvet.jgea.experimenter.drawer.TreeDrawer;
import io.github.ericmedvet.jgea.experimenter.drawer.TreeDrawer.Configuration;
import io.github.ericmedvet.jnb.datastructure.Tree;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class GrammarTreeTest {

  static void main() throws IOException {
    StringGrammar<String> g = StringGrammar.load(
        new FileInputStream(
            "/home/eric/IdeaProjects/shield/src/main/resources/grammars/geolife/geolife.mobility.04.bnf"
            //"/home/eric/IdeaProjects/jgea/io.github.ericmedvet.jgea.core/src/main/resources/grammars/1d/symbolic-regression.bnf"
        )
    );
    //    StringGrammar<String> g = StringGrammars.simpleFixedArity(3).apply(null);
    System.out.println(g);
    GrowGrammarTreeBuilder<String> growFactory = new GrowGrammarTreeBuilder<>(g);
    FullGrammarTreeBuilder<String> fullFactory = new FullGrammarTreeBuilder<>(g);
    RandomGenerator rg = new Random(2);

    TreeDrawer drawer = new TreeDrawer(Configuration.DEFAULT);
    //    drawer.show(growFactory.apply(g.startingSymbol(), 5).build(rg));
    //    drawer.show(fullFactory.apply(g.startingSymbol(), 7).build(rg));

    //    g.rules()
    //        .keySet()
    //        .forEach(
    //            s -> System.out.printf(
    //                "%20.20s [%3.0f,%3.0f] %d -> %s%n",
    //                s,
    //                growFactory.nonTerminalHeights.get(s).min(),
    //                growFactory.nonTerminalHeights.get(s).max(),
    //                g.rules().get(s).size(),
    //                IntStream.range(0, 20)
    //                    .mapToObj(
    //                        h -> "%02d:%d".formatted(h, growFactory.getMatchingOptions(s, h).size()))
    //                    .collect(Collectors.joining(","))
    //            )
    //        );

    record TreeStats(int h, int size, int n) {

      public TreeStats sum(TreeStats other) {
        return new TreeStats(h + other.h, size + other.size, n + other.n);
      }

      public static TreeStats from(Tree<?> tree) {
        return new TreeStats(tree.height(), tree.size(), 1);
      }
    }
    int nTrees = 10;
    IntStream.range(0, 16).forEach(h -> {
      TreeStats growTS = IntStream.range(0, nTrees)
          .mapToObj(_ -> TreeStats.from(growFactory.apply(g.startingSymbol(), h).build(rg)))
          .reduce(TreeStats::sum)
          .orElseThrow();
      TreeStats fullTS = IntStream.range(0, nTrees)
          .mapToObj(_ -> TreeStats.from(fullFactory.apply(g.startingSymbol(), h).build(rg)))
          .reduce(TreeStats::sum)
          .orElseThrow();
      System.out.printf(
          "%d ->\tgrow: h=%.1f size=%.1f n=%d\tfull: h=%.1f size=%.1f n=%d%n",
          h,
          (float) growTS.h / growTS.n,
          (float) growTS.size / growTS.n,
          growTS.n,
          (float) fullTS.h / fullTS.n,
          (float) fullTS.size / fullTS.n,
          fullTS.n
      );
    });

    Function<Integer, IndependentFactory<Tree<String>>> tb = new GrowTreeBuilder<>(
        _ -> 3,
        _ -> "N",
        _ -> "t"
    );
    drawer.show(tb.apply(5).build(rg));

  }

}
