/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.grammarbased;

import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.core.util.Triplet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author eric
 */
public class Util {

  public static <T> Map<T, Pair<Double, Double>> computeSymbolsMinMaxDepths(Grammar<T> g) {
    Map<T, Pair<Integer, Boolean>> minDepths = computeSymbolsMinDepths(g);
    Map<T, Triplet<Double, Boolean, Set<T>>> maxDepths = computeSymbolsMaxDepths(g);
    Map<T, Pair<Double, Double>> map = new HashMap<>();
    for (T t : minDepths.keySet()) {
      map.put(t, new Pair<>((double) minDepths.get(t).getFirst(), maxDepths.get(t).getFirst()));
    }
    return map;
  }

  private static <T> Map<T, Pair<Integer, Boolean>> computeSymbolsMinDepths(Grammar<T> g) {
    Map<T, Pair<Integer, Boolean>> map = new HashMap<>();
    map.put(g.getStartingSymbol(), new Pair<>(Integer.MAX_VALUE, false));
    for (List<List<T>> options : g.getRules().values()) {
      for (List<T> option : options) {
        for (T symbol : option) {
          if (!g.getRules().containsKey(symbol)) {
            map.put(symbol, new Pair<>(1, true));
          } else {
            map.put(symbol, new Pair<>(Integer.MAX_VALUE, false));
          }
        }
      }
    }
    //compute mins
    while (true) {
      boolean changed = false;
      for (T nonTerminal : g.getRules().keySet()) {
        Pair<Integer, Boolean> pair = map.get(nonTerminal);
        if (pair.getSecond()) {
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
            optionAllResolved = optionAllResolved && optionSymbolPair.getSecond();
            optionMaxDepth = Math.max(optionMaxDepth, optionSymbolPair.getFirst());
          }
          allResolved = allResolved && optionAllResolved;
          minDepth = Math.min(minDepth, optionMaxDepth + 1);
        }
        Pair<Integer, Boolean> newPair = new Pair<>(minDepth, allResolved);
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
    map.put(g.getStartingSymbol(), new Triplet<>(0d, false, (Set<T>) new HashSet<T>()));
    for (List<List<T>> options : g.getRules().values()) {
      for (List<T> option : options) {
        for (T symbol : option) {
          if (!g.getRules().containsKey(symbol)) {
            map.put(symbol, new Triplet<>(1d, true, (Set<T>) Collections.EMPTY_SET));
          } else {
            map.put(symbol, new Triplet<>(0d, false, (Set<T>) new HashSet<T>()));
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
        if (triplet.getSecond()) {
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
            optionAllResolved = optionAllResolved && optionSymbolTriplet.getSecond();
            optionMaxDepth = Math.max(optionMaxDepth, optionSymbolTriplet.getFirst());
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
        Triplet<Double, Boolean, Set<T>> newTriplet = new Triplet<>(maxDepth, allResolved, dependencies);
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

}
