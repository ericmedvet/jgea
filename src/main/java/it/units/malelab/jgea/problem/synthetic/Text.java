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
public class Text implements GrammarBasedProblem<String, String, Double> {
  
  private static class FitnessFunction implements Function<String, Double>, Bounded<Double> {
    
    private final Sequence<Character> target;
    private final Distance<Sequence<Character>> distance;

    public FitnessFunction(String targetString) {
      this.target = Sequence.from(targetString.chars().mapToObj(c -> (char) c).toArray(Character[]::new));
      this.distance = new Edit<>();
    }

    @Override
    public Double apply(String string, Listener listener) throws FunctionException {
      double d = (double)distance.apply(
              target,
              Sequence.from(string.chars().mapToObj(c -> (char) c).toArray(Character[]::new))
      )/(double)target.size();
      return d;
    }

    @Override
    public Double worstValue() {
      return Double.POSITIVE_INFINITY;
    }

    @Override
    public Double bestValue() {
      return 0d;
    }
    
  }
  
  private final Grammar<String> grammar;
  private final Function<Node<String>, String> solutionMapper;
  private final Function<String, Double> fitnessFunction;

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
  public Function<String, Double> getFitnessFunction() {
    return fitnessFunction;
  }
  
}
