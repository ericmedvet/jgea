package io.github.ericmedvet.jgea.problem.mapper;

import io.github.ericmedvet.jgea.core.order.ParetoDominance;
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.problem.ProblemWithValidation;
import io.github.ericmedvet.jgea.core.representation.grammar.string.GrammarBasedProblem;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import io.github.ericmedvet.jgea.core.util.Pair;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
@SuppressWarnings("rawtypes")
public class MapperGeneration implements GrammarBasedProblem<String, Pair<Tree<Element>, Tree<Element>>>,
    ProblemWithValidation<Pair<Tree<Element>, Tree<Element>>, List<Double>> {

  private final StringGrammar<String> grammar;
  private final FitnessFunction learningFitnessFunction;
  private final FitnessFunction validationFitnessFunction;
  private final int dimensionality;

  public MapperGeneration(
      List<EnhancedProblem> learningProblems,
      int learningGenotypeSize,
      int learningN,
      int learningMaxMappingDepth,
      List<FitnessFunction.Property> learningProperties,
      List<EnhancedProblem> validationProblems,
      int validationN,
      int validationGenotypeSize,
      int validationMaxMappingDepth,
      List<FitnessFunction.Property> validationProperties,
      long seed
  ) throws IOException {
    this.grammar = StringGrammar.load(StringGrammar.class.getResourceAsStream("/grammars/1d/mapper.bnf"));
    learningFitnessFunction = new FitnessFunction(
        learningProblems,
        learningGenotypeSize,
        learningN,
        learningMaxMappingDepth,
        learningProperties,
        seed
    );
    validationFitnessFunction = new FitnessFunction(
        validationProblems,
        validationGenotypeSize,
        validationN,
        validationMaxMappingDepth,
        validationProperties,
        seed
    );
    dimensionality = learningProperties.size();
  }

  @Override
  public StringGrammar<String> getGrammar() {
    return grammar;
  }

  @Override
  public Function<Tree<String>, Pair<Tree<Element>, Tree<Element>>> getSolutionMapper() {
    return (Tree<String> rawMappingTree) -> {
      Tree<Element> optionChooser = MapperUtils.transform(rawMappingTree.child(0));
      Tree<Element> genoAssigner = MapperUtils.transform(rawMappingTree.child(1));
      return Pair.of(optionChooser, genoAssigner);
    };
  }

  @Override
  public PartialComparator<List<Double>> qualityComparator() {
    return ParetoDominance.build(Double.class, dimensionality);
  }

  @Override
  public Function<Pair<Tree<Element>, Tree<Element>>, List<Double>> qualityFunction() {
    return learningFitnessFunction;
  }

  @Override
  public Function<Pair<Tree<Element>, Tree<Element>>, List<Double>> validationQualityFunction() {
    return validationFitnessFunction;
  }

}
