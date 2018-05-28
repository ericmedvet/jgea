/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.mapper;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.genotype.BitString;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.grammarbased.ge.WeightedHierarchicalMapper;
import it.units.malelab.jgea.problem.mapper.element.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author eric
 */
public class RecursiveMapper<T> extends WeightedHierarchicalMapper<T> {

  private final Node<Element> optionChooser;
  private final Node<Element> genoAssigner;
  private final int maxMappingDepth;

  public RecursiveMapper(Node<Element> optionChooser, Node<Element> genoAssigner, int maxMappingDepth, int maxDepth, Grammar<T> grammar) {
    super(maxDepth, grammar);
    this.maxMappingDepth = maxMappingDepth;
    this.optionChooser = optionChooser;
    this.genoAssigner = genoAssigner;
  }

  @Override
  public Node<T> apply(BitString genotype, Listener listener) throws FunctionException {
    AtomicInteger mappingGlobalCounter = new AtomicInteger();
    AtomicInteger finalizationGlobalCounter = new AtomicInteger();
    Node<T> tree = mapRecursively(grammar.getStartingSymbol(), genotype, mappingGlobalCounter, finalizationGlobalCounter, 0);
    tree.propagateParentship();
    return tree;    
  }

  private Node<T> mapRecursively(
          T symbol,
          BitString genotype,
          AtomicInteger mappingGlobalCounter,
          AtomicInteger finalizationGlobalCounter,
          int depth
  ) {
    Node<T> node = new Node<>(symbol);
    if (!grammar.getRules().containsKey(symbol)) {
      return node;
    }
    if (depth >= maxMappingDepth) {
      List<Integer> shortestOptionIndexTies = shortestOptionIndexesMap.get(symbol);
      List<T> shortestOption = grammar.getRules().get(symbol).get(shortestOptionIndexTies.get(finalizationGlobalCounter.getAndIncrement()% shortestOptionIndexTies.size()));
      for (T optionSymbol : shortestOption) {
        node.getChildren().add(mapRecursively(optionSymbol, genotype, mappingGlobalCounter, finalizationGlobalCounter, depth + 1));
      }
      return node;
    }
    //choose option
    List<List<T>> options = grammar.getRules().get(symbol);
    List<Double> expressivenesses = new ArrayList<>(options.size());
    for (List<T> option : options) {
      double expressiveness = 1d;
      for (T optionSymbol : option) {
        expressiveness = expressiveness * (double) weightsMap.getOrDefault(optionSymbol, 1);
      }
      expressivenesses.add(expressiveness);
    }
    int optionIndex = ((Double) MapperUtils.compute(
            optionChooser, genotype, expressivenesses, depth, mappingGlobalCounter)).intValue();
    optionIndex = Math.min(optionIndex, options.size()-1);
    optionIndex = Math.max(0, optionIndex);
    //split genotype
    expressivenesses.clear();
    for (T optionSymbol : options.get(optionIndex)) {
      expressivenesses.add((double) weightsMap.getOrDefault(optionSymbol, 1));
    }
    List<BitString> pieces = ((List<BitString>) MapperUtils.compute(genoAssigner, genotype, expressivenesses, depth, mappingGlobalCounter));
    for (int i = 0; i < options.get(optionIndex).size(); i++) {
      BitString piece;
      if (pieces.size() > i) {
        piece = pieces.get(i);
      } else {
        piece = new BitString(0);
      }
      node.getChildren().add(mapRecursively(
              options.get(optionIndex).get(i), piece, mappingGlobalCounter, finalizationGlobalCounter, depth + 1));
    }
    return node;
  }

}
