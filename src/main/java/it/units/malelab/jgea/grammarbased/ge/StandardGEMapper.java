/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.grammarbased.ge;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.genotype.BitString;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.event.FunctionEvent;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.grammarbased.GrammarBasedMapper;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author eric
 */
public class StandardGEMapper<T> extends GrammarBasedMapper<BitString, T> {
  
  private final int codonLenght;
  private final int maxWraps;
  
  public static final String BIT_USAGES_INDEX_NAME = "bitUsages";

  public StandardGEMapper(int codonLenght, int maxWraps, Grammar<T> grammar) {
    super(grammar);
    this.codonLenght = codonLenght;
    this.maxWraps = maxWraps;
  }    

  @Override
  public Node<T> apply(BitString genotype, Listener listener) throws FunctionException {
    int[] bitUsages = new int[genotype.size()];
    if (genotype.size()<codonLenght) {
      throw new FunctionException(String.format("Short genotype (%d<%d)", genotype.size(), codonLenght));
    }
    Node<T> tree = new Node<>(grammar.getStartingSymbol());
    int currentCodonIndex = 0;
    int wraps = 0;
    while (true) {
      Node<T> nodeToBeReplaced = null;
      for (Node<T> node : tree.leafNodes()) {
        if (grammar.getRules().keySet().contains(node.getContent())) {
          nodeToBeReplaced = node;
          break;
        }
      }
      if (nodeToBeReplaced==null) {
        break;
      }
      //get codon index and option
      if ((currentCodonIndex+1)*codonLenght>genotype.size()) {
        wraps = wraps+1;
        currentCodonIndex = 0;
        if (wraps>maxWraps) {
          throw new FunctionException(String.format("Too many wraps (%d>%d)", wraps, maxWraps));
        }
      }      
      List<List<T>> options = grammar.getRules().get(nodeToBeReplaced.getContent());
      int optionIndex = genotype.slice(currentCodonIndex*codonLenght, (currentCodonIndex+1)*codonLenght).toInt()%options.size();
      /*
      System.out.printf("i=%3d g_i=%3d |r_s|=%2d j=%2d w=%2d %s %s%n",
              currentCodonIndex,
              genotype.slice(currentCodonIndex*codonLenght, (currentCodonIndex+1)*codonLenght).toInt(),
              options.size(),
              optionIndex,
              wraps,
              genotype.slice(currentCodonIndex*codonLenght, (currentCodonIndex+1)*codonLenght),
              tree.leaves()
              );
      */
      //update usages
      for (int i = currentCodonIndex*codonLenght; i<(currentCodonIndex+1)*codonLenght; i++) {
        bitUsages[i] = bitUsages[i]+1;
      }
      //add children
      for (T t : options.get(optionIndex)) {
        Node<T> newChild = new Node<>(t);
        nodeToBeReplaced.getChildren().add(newChild);
      }
      currentCodonIndex = currentCodonIndex+1;
    }
    listener.listen(new FunctionEvent(genotype, tree, Collections.singletonMap(BIT_USAGES_INDEX_NAME, bitUsages)));
    return tree;
  }

  @Override
  public String toString() {
    return "StandardGEMapper{" + "codonLenght=" + codonLenght + ", maxWraps=" + maxWraps + '}';
  }    
     
}
