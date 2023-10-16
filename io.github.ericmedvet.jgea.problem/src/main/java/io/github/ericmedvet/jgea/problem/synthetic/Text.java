
package io.github.ericmedvet.jgea.problem.synthetic;

import io.github.ericmedvet.jgea.core.distance.Distance;
import io.github.ericmedvet.jgea.core.distance.Edit;
import io.github.ericmedvet.jgea.core.problem.ComparableQualityBasedProblem;
import io.github.ericmedvet.jgea.core.representation.grammar.string.GrammarBasedProblem;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
public class Text implements GrammarBasedProblem<String, String>, ComparableQualityBasedProblem<String, Double> {

  private final StringGrammar<String> grammar;
  private final Function<Tree<String>, String> solutionMapper;
  private final Function<String, Double> fitnessFunction;
  private final List<Character> target;
  private final Distance<List<Character>> distance;

  public Text(String targetString) throws IOException {
    grammar = StringGrammar.load(StringGrammar.class.getResourceAsStream("/grammars/1d/text.bnf"));
    solutionMapper = (Tree<String> tree) -> tree.leaves()
        .stream()
        .map(Tree::content)
        .collect(Collectors.joining())
        .replace("_", " ");
    target = targetString.chars().mapToObj(c -> (char) c).toList();
    this.distance = new Edit<>();
    fitnessFunction = string -> distance.apply(
        target,
        string.chars().mapToObj(c -> (char) c).toList()
    ) / (double) target.size();
  }

  @Override
  public StringGrammar<String> getGrammar() {
    return grammar;
  }

  @Override
  public Function<Tree<String>, String> getSolutionMapper() {
    return solutionMapper;
  }

  @Override
  public Function<String, Double> qualityFunction() {
    return fitnessFunction;
  }

}
