/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.representation.sequence.bit.BitString;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedMapper;
import it.units.malelab.jgea.representation.grammar.ge.HierarchicalMapper;
import it.units.malelab.jgea.representation.grammar.ge.StandardGEMapper;
import it.units.malelab.jgea.representation.grammar.ge.WeightedHierarchicalMapper;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.math3.stat.StatUtils;

/**
 *
 * @author eric
 */
public class TheoreticalDegeneracy {

  public static void main(String[] args) throws FileNotFoundException {
    final PrintStream ps = new PrintStream("/home/eric/experiments/tcyb-whge/properties.txt");
    //final PrintStream ps = System.out;

    int[] lengths = new int[]{2, 4, 6, 8, 10, 12, 14, 16};
    int[] ts = new int[]{1, 2, 4, 6, 8};
    int[] ns = new int[]{1, 2, 4, 6, 8};
    ps.printf("%s %s %s %s %s %s%n", "n", "t", "l", "mapper", "property", "value");
    for (int n : ns) {
      for (int t : ts) {
        //build grammar
        Grammar<String> grammar = new Grammar<>();
        for (int i = 0; i < n; i++) {
          grammar.getRules().put("N_" + i, new ArrayList<>());
          List<String> nonTerminalsOption = new ArrayList<>();
          for (int j = i; j < n; j++) {
            nonTerminalsOption.add("N_" + j);
          }
          grammar.getRules().get("N_" + i).add(nonTerminalsOption);
          for (int j = 0; j < t; j++) {
            grammar.getRules().get("N_" + i).add(l("t_" + i + "_" + j));
          }
        }
        grammar.setStartingSymbol("N_0");
        //build mappers
        int geCodonSize = (int)Math.ceil(Math.log(t+1)/Math.log(2));
        
        System.out.println(grammar);
        System.out.printf("n=%d t=%d codonSize=%d%n", n, t, geCodonSize);
        
        Map<String, GrammarBasedMapper<BitString, String>> mappers = new TreeMap<>();
        mappers.put("GE-opt-1", new StandardGEMapper<>(geCodonSize, 1, grammar));
        mappers.put("GE-opt-2", new StandardGEMapper<>(geCodonSize, 2, grammar));
        mappers.put("GE-opt-4", new StandardGEMapper<>(geCodonSize, 4, grammar));
        mappers.put("HGE", new HierarchicalMapper<>(grammar));
        mappers.put("WHGE-2", new WeightedHierarchicalMapper<>(2, grammar));
        mappers.put("WHGE-3", new WeightedHierarchicalMapper<>(3, grammar));
        mappers.put("WHGE-4", new WeightedHierarchicalMapper<>(4, grammar));
        //compute
        for (int l : lengths) {
          Set<BitString> genotypes = new HashSet<>();
          for (long i = 0; i < Math.pow(2, l); i++) {
            genotypes.add(new BitString(l, BitSet.valueOf(new long[]{i})));
          }
          mappers.forEach((k, mapper) -> {
            Multiset<List<String>> phenotypes = HashMultiset.create();
            phenotypes.addAll(genotypes.parallelStream()
                    .map(g -> {
                      try {
                        return mapper.apply(g).leafNodes().stream().map(Node::getContent).collect(Collectors.toList());
                      } catch (FunctionException e) {
                        return null;
                      }
                    })
                    .collect(Collectors.toList()));
            double invalidity = (double) phenotypes.count(null) / (double) phenotypes.size();
            double phenoSize = (double) phenotypes.elementSet().size() - (phenotypes.contains(null) ? 1d : 0d);
            double degeneracy = 1d - phenoSize / (double) genotypes.size();
            double maxLength = phenotypes.elementSet().stream().filter(s -> (s != null)).mapToInt(List::size).max().orElse(0);
            double avgLength = phenotypes.stream().filter(s -> (s != null)).mapToInt(List::size).average().orElse(0d);
            double[] sizes = phenotypes.entrySet().stream().filter(e -> (e.getElement() != null)).mapToDouble(Multiset.Entry::getCount).toArray();
            double nonUniformity = Math.sqrt(StatUtils.variance(sizes))/StatUtils.mean(sizes);
            ps.printf("%d %d %d %s %s %f%n", n, t, l, k, "degeneracy", degeneracy);
            ps.printf("%d %d %d %s %s %f%n", n, t, l, k, "nonUniformity", nonUniformity);
            ps.printf("%d %d %d %s %s %f%n", n, t, l, k, "invalidity", invalidity);
            ps.printf("%d %d %d %s %s %f%n", n, t, l, k, "phenoSize", phenoSize);
            ps.printf("%d %d %d %s %s %f%n", n, t, l, k, "maxLength", maxLength);
            ps.printf("%d %d %d %s %s %f%n", n, t, l, k, "avgLength", avgLength);
          });
        }

      }
    }
  }

  private static <T> List<T> l(T... ts) {
    return Arrays.asList(ts);
  }

}
