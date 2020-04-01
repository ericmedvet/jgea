/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.representation.grammar;

import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.core.util.Triplet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author eric
 */
public class GrammarUtil {

  public static <T> Map<T, Pair<Double, Double>> computeSymbolsMinMaxDepths(Grammar<T> g) {
    Map<T, Pair<Integer, Boolean>> minDepths = computeSymbolsMinDepths(g);
    Map<T, Triplet<Double, Boolean, Set<T>>> maxDepths = computeSymbolsMaxDepths(g);
    Map<T, Pair<Double, Double>> map = new HashMap<>();
    for (T t : minDepths.keySet()) {
      map.put(t, Pair.build((double) minDepths.get(t).first(), maxDepths.get(t).first()));
    }
    return map;
  }

  private static <T> Map<T, Pair<Integer, Boolean>> computeSymbolsMinDepths(Grammar<T> g) {
    Map<T, Pair<Integer, Boolean>> map = new HashMap<>();
    map.put(g.getStartingSymbol(), Pair.build(Integer.MAX_VALUE, false));
    for (List<List<T>> options : g.getRules().values()) {
      for (List<T> option : options) {
        for (T symbol : option) {
          if (!g.getRules().containsKey(symbol)) {
            map.put(symbol, Pair.build(1, true));
          } else {
            map.put(symbol, Pair.build(Integer.MAX_VALUE, false));
          }
        }
      }
    }
    //compute mins
    while (true) {
      boolean changed = false;
      for (T nonTerminal : g.getRules().keySet()) {
        Pair<Integer, Boolean> pair = map.get(nonTerminal);
        if (pair.second()) {
          //this non-terminal is definitely resolved
          continue;
        }
        boolean allResolved = true;
        int minDepth = Integer.MAX_VALUE;
        for (List<T> option : g.getRules().get(nonTerminal)) {
          boolean optionAllResolved = true;
          int optionMaxDepth = 0;
          for (T optionSymbol : option) {
            Pair<Integer, Boolean> optionSymbolPair = map.get(optionSymbol);
            optionAllResolved = optionAllResolved && optionSymbolPair.second();
            optionMaxDepth = Math.max(optionMaxDepth, optionSymbolPair.first());
          }
          allResolved = allResolved && optionAllResolved;
          minDepth = Math.min(minDepth, optionMaxDepth + 1);
        }
        Pair<Integer, Boolean> newPair = Pair.build(minDepth, allResolved);
        if (!newPair.equals(pair)) {
          map.put(nonTerminal, newPair);
          changed = true;
        }
      }
      if (!changed) {
        break;
      }
    }
    return map;
  }

  private static <T> Map<T, Triplet<Double, Boolean, Set<T>>> computeSymbolsMaxDepths(Grammar<T> g) {
    Map<T, Triplet<Double, Boolean, Set<T>>> map = new HashMap<>();
    map.put(g.getStartingSymbol(), Triplet.build(0d, false, (Set<T>) new HashSet<T>()));
    for (List<List<T>> options : g.getRules().values()) {
      for (List<T> option : options) {
        for (T symbol : option) {
          if (!g.getRules().containsKey(symbol)) {
            map.put(symbol, Triplet.build(1d, true, (Set<T>) Collections.EMPTY_SET));
          } else {
            map.put(symbol, Triplet.build(0d, false, (Set<T>) new HashSet<T>()));
          }
        }
      }
    }
    //compute maxs
    while (true) {
      boolean changed = false;
      for (T nonTerminal : g.getRules().keySet()) {
        Triplet<Double, Boolean, Set<T>> triplet = map.get(nonTerminal);
        Set<T> dependencies = new HashSet<>(triplet.getThird());
        if (triplet.second()) {
          //this non-terminal is definitely resolved
          continue;
        }
        boolean allResolved = true;
        double maxDepth = 0;
        for (List<T> option : g.getRules().get(nonTerminal)) {
          boolean optionAllResolved = true;
          double optionMaxDepth = 0;
          for (T optionSymbol : option) {
            Triplet<Double, Boolean, Set<T>> optionSymbolTriplet = map.get(optionSymbol);
            optionAllResolved = optionAllResolved && optionSymbolTriplet.second();
            optionMaxDepth = Math.max(optionMaxDepth, optionSymbolTriplet.first());
            dependencies.add(optionSymbol);
            dependencies.addAll(optionSymbolTriplet.getThird());
          }
          allResolved = allResolved && optionAllResolved;
          maxDepth = Math.max(maxDepth, optionMaxDepth + 1);
        }
        if (dependencies.contains(nonTerminal)) {
          allResolved = true;
          maxDepth = Double.POSITIVE_INFINITY;
        }
        Triplet<Double, Boolean, Set<T>> newTriplet = Triplet.build(maxDepth, allResolved, dependencies);
        if (!newTriplet.equals(triplet)) {
          map.put(nonTerminal, newTriplet);
          changed = true;
        }
      }
      if (!changed) {
        break;
      }
    }
    return map;
  }
  
  public static <T> Map<T, List<Integer>> computeShortestOptionIndexesMap(Grammar<T> grammar) {
    Map<T, List<Integer>> optionJumpsToTerminalMap = new LinkedHashMap<>();
    for (Map.Entry<T, List<List<T>>> rule : grammar.getRules().entrySet()) {
      List<Integer> optionsJumps = new ArrayList<>();
      for (List<T> option : rule.getValue()) {
        optionsJumps.add(Integer.MAX_VALUE);
      }
      optionJumpsToTerminalMap.put(rule.getKey(), optionsJumps);
    }
    while (true) {
      boolean completed = true;
      for (Map.Entry<T, List<Integer>> entry : optionJumpsToTerminalMap.entrySet()) {
        for (int i = 0; i < entry.getValue().size(); i++) {
          List<T> option = grammar.getRules().get(entry.getKey()).get(i);
          if (Collections.disjoint(option, grammar.getRules().keySet())) {
            entry.getValue().set(i, 1);
          } else {
            int maxJumps = Integer.MIN_VALUE;
            for (T optionSymbol : option) {
              List<Integer> optionSymbolJumps = optionJumpsToTerminalMap.get(optionSymbol);
              if (optionSymbolJumps == null) {
                maxJumps = Math.max(0, maxJumps);
              } else {
                int minJumps = Integer.MAX_VALUE;
                for (int jumps : optionSymbolJumps) {
                  minJumps = Math.min(minJumps, jumps);
                }
                minJumps = (minJumps == Integer.MAX_VALUE) ? minJumps : (minJumps + 1);
                maxJumps = Math.max(minJumps, maxJumps);
              }
            }
            entry.getValue().set(i, maxJumps);
            if (maxJumps == Integer.MAX_VALUE) {
              completed = false;
            }
          }
        }
      }
      if (completed) {
        break;
      }
    }
    //build shortestOptionIndexMap
    Map<T, List<Integer>> shortestOptionIndexesMap = new LinkedHashMap<>();
    for (Map.Entry<T, List<List<T>>> rule : grammar.getRules().entrySet()) {
      int minJumps = Integer.MAX_VALUE;
      for (int i = 0; i < optionJumpsToTerminalMap.get(rule.getKey()).size(); i++) {
        int localJumps = optionJumpsToTerminalMap.get(rule.getKey()).get(i);
        if (localJumps < minJumps) {
          minJumps = localJumps;
        }
      }
      List<Integer> indexes = new ArrayList<>();
      for (int i = 0; i < optionJumpsToTerminalMap.get(rule.getKey()).size(); i++) {
        if (optionJumpsToTerminalMap.get(rule.getKey()).get(i) == minJumps) {
          indexes.add(i);
        }
      }
      shortestOptionIndexesMap.put(rule.getKey(), indexes);
    }
    return shortestOptionIndexesMap;
  }

}
