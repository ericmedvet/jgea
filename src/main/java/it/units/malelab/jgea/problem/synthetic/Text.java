/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.synthetic;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.Sequence;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.distance.Distance;
import it.units.malelab.jgea.distance.Edit;
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.grammarbased.GrammarBasedProblem;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class Text implements GrammarBasedProblem<String, String, Integer> {
  
  private static class FitnessFunction implements Function<String, Integer>, Bounded<Integer> {
    
    private final Sequence<String> target;
    private final Distance<Sequence<String>> distance;

    public FitnessFunction(String targetString) {
      this.target = Sequence.from(targetString.split(""));
      this.distance = new Edit<>();
    }

    @Override
    public Integer apply(String string, Listener listener) throws FunctionException {
      Sequence<String> sequence = Sequence.from(string.split(""));
      int d = (int)Math.round(distance.d(target, sequence));
      return d;
    }

    @Override
    public Integer worstValue() {
      return Integer.MAX_VALUE;
    }

    @Override
    public Integer bestValue() {
      return 0;
    }
    
  }
  
  private final Grammar<String> grammar;
  private final Function<Node<String>, String> solutionMapper;
  private final Function<String, Integer> fitnessFunction;

  public Text(String targetString) throws IOException {
    grammar = Grammar.fromFile(new File("grammars/text.bnf"));
    solutionMapper = (Node<String> node, Listener listener) -> 
            node.leafNodes().stream()
                    .map(Node::getContent)
                    .collect(Collectors.joining()).replace("_", " ");
    fitnessFunction = new FitnessFunction(targetString);
  }

  @Override
  public Grammar<String> getGrammar() {
    return grammar;
  }

  @Override
  public Function<Node<String>, String> getSolutionMapper() {
    return solutionMapper;
  }

  @Override
  public Function<String, Integer> getFitnessFunction() {
    return fitnessFunction;
  }
  
}
