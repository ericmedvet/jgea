/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.genotype.BitString;
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.grammarbased.GrammarBasedMapper;
import it.units.malelab.jgea.grammarbased.ge.HierarchicalMapper;
import it.units.malelab.jgea.grammarbased.ge.StandardGEMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author eric
 */
public class TheoreticalDegeneracy {

  public static void main(String[] args) {
    int maxLength = 10;
    Map<String, GrammarBasedMapper<BitString, String>> mappers = new TreeMap<>();
    //toy grammar 1
    Grammar<String> grammar = new Grammar<>();
    grammar.getRules().put("A", l(l("A", "a"), l("a")));
    grammar.setStartingSymbol("A");
    System.out.println(grammar);
    mappers.put("GE", new StandardGEMapper<>(1, 1, grammar));
    mappers.put("HGE", new HierarchicalMapper<>(grammar));
    for (int l = 1; l < maxLength; l++) {
      System.out.printf("Length: %d%n", l);
      Set<BitString> genotypes = new HashSet<>();
      for (long i = 0; i < Math.pow(2, l); i++) {
        genotypes.add(new BitString(l, BitSet.valueOf(new long[]{i})));
      }
      mappers.forEach((k, mapper) -> {
        Set<Node<String>> phenotypes = new HashSet<>();
        int invalidCount = 0;
        for (BitString genotype : genotypes) {
          try {
            phenotypes.add(mapper.apply(genotype));
          } catch (FunctionException ex) {
            invalidCount = invalidCount + 1;
          };
        }
        System.out.printf("%5.5s phenos=%6d degeneracy=%5.3f invalids=%6d invalidity=%5.3f%n",
                k,
                phenotypes.size(),
                1d - (double) phenotypes.size() / (double) genotypes.size(),
                invalidCount,
                (double) invalidCount / (double) genotypes.size()
        );
      });
    }
  }

  private static <T> List<T> l(T... ts) {
    return Arrays.asList(ts);
  }

}
