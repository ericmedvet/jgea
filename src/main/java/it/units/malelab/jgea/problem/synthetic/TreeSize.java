/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.synthetic;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.grammarbased.GrammarBasedProblem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class TreeSize implements GrammarBasedProblem<Boolean, Node<Boolean>, Double> {

  private final Grammar<Boolean> grammar;
  private final NonDeterministicFunction<Node<Boolean>, Double> fitnessFunction;

  public TreeSize(int nonTerminals, int terminals) {
    this.grammar = new Grammar<>();
    grammar.setStartingSymbol(false);
    grammar.getRules().put(false, l(r(nonTerminals, false), r(terminals, true)));
    fitnessFunction = (Node<Boolean> tree, Random r, Listener l) -> 1d / (double) tree.size();
  }

  @Override
  public Grammar<Boolean> getGrammar() {
    return grammar;
  }

  @Override
  public Function<Node<Boolean>, Node<Boolean>> getSolutionMapper() {
    return Function.identity();
  }

  @Override
  public NonDeterministicFunction<Node<Boolean>, Double> getFitnessFunction() {
    return fitnessFunction;
  }

  private static <T> List<T> l(T... ts) {
    return Arrays.asList(ts);
  }

  private static <T> List<T> r(int n, T... ts) {
    List<T> list = new ArrayList<>(n * ts.length);
    for (int i = 0; i < n; i++) {
      list.addAll(l(ts));
    }
    return list;
  }

}
