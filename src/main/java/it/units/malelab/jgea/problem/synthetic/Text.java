/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.synthetic;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.Sequence;
import it.units.malelab.jgea.core.mapper.BoundMapper;
import it.units.malelab.jgea.core.mapper.DeterministicMapper;
import it.units.malelab.jgea.core.mapper.MappingException;
import it.units.malelab.jgea.core.mapper.MuteDeterministicMapper;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.distance.Distance;
import it.units.malelab.jgea.distance.Edit;
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.grammarbased.GrammarBasedProblem;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author eric
 */
public class Text implements GrammarBasedProblem<String, String, Integer> {

  private static class SolutionMapper extends MuteDeterministicMapper<Node<String>, String> {

    @Override
    public String map(Node<String> tree) throws MappingException {
      StringBuilder sb = new StringBuilder();
      if (tree!=null) {
        for (Node<String> leafNode : tree.leafNodes()) {
          sb.append(leafNode.getContent());
        }
      }
      return sb.toString().replace("_", " ");
    }
    
  }
  
  private static class FitnessMapper extends MuteDeterministicMapper<String, Integer> implements BoundMapper<String, Integer> {
    
    private final Sequence<String> target;
    private final Distance<Sequence<String>> distance;

    public FitnessMapper(String targetString) {
      target = Misc.fromList(Arrays.asList(targetString.split("")));
      distance = new Edit<>();
    }

    @Override
    public Integer map(String string) throws MappingException {
      Sequence<String> sequence = Misc.fromList(Arrays.asList(string.split("")));
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
  private final DeterministicMapper<Node<String>, String> solutionMapper;
  private final BoundMapper<String, Integer> fitnessMapper;

  public Text(String targetString) throws IOException {
    grammar = Grammar.fromFile(new File("grammars/text.bnf"));
    solutionMapper = new SolutionMapper();
    fitnessMapper = new FitnessMapper(targetString);
  }

  @Override
  public Grammar<String> getGrammar() {
    return grammar;
  }

  @Override
  public DeterministicMapper<Node<String>, String> getSolutionMapper() {
    return solutionMapper;
  }

  @Override
  public BoundMapper<String, Integer> getFitnessMapper() {
    return fitnessMapper;
  }
  
}
