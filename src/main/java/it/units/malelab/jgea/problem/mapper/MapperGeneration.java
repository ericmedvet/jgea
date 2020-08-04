/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.mapper;

import it.units.malelab.jgea.representation.tree.Tree;
import it.units.malelab.jgea.core.ProblemWithValidation;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
import it.units.malelab.jgea.problem.mapper.element.Element;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * @author eric
 */
public class MapperGeneration implements
    GrammarBasedProblem<String, Pair<Tree<Element>, Tree<Element>>, List<Double>>,
    ProblemWithValidation<Pair<Tree<Element>, Tree<Element>>, List<Double>> {

  private final Grammar<String> grammar;
  private final FitnessFunction learningFitnessFunction;
  private final FitnessFunction validationFitnessFunction;

  public MapperGeneration(
      List<EnhancedProblem> learningProblems, int learningGenotypeSize, int learningN, int learningMaxMappingDepth, List<FitnessFunction.Property> learningProperties,
      List<EnhancedProblem> validationProblems, int validationN, int validationGenotypeSize, int validationMaxMappingDepth, List<FitnessFunction.Property> validationProperties,
      long seed) throws IOException {
    this.grammar = Grammar.fromFile(new File("grammars/mapper.bnf"));
    learningFitnessFunction = new FitnessFunction(learningProblems, learningGenotypeSize, learningN, learningMaxMappingDepth, learningProperties, seed);
    validationFitnessFunction = new FitnessFunction(validationProblems, validationGenotypeSize, validationN, validationMaxMappingDepth, validationProperties, seed);
  }

  @Override
  public Grammar<String> getGrammar() {
    return grammar;
  }

  @Override
  public Function<Tree<String>, Pair<Tree<Element>, Tree<Element>>> getSolutionMapper() {
    return (Tree<String> rawMappingTree) -> {
      Tree<Element> optionChooser = MapperUtils.transform(rawMappingTree.getChildren().get(0));
      Tree<Element> genoAssigner = MapperUtils.transform(rawMappingTree.getChildren().get(1));
      optionChooser.propagateParentship();
      genoAssigner.propagateParentship();
      return Pair.of(optionChooser, genoAssigner);
    };
  }

  @Override
  public Function<Pair<Tree<Element>, Tree<Element>>, List<Double>> getFitnessFunction() {
    return learningFitnessFunction;
  }

  @Override
  public Function<Pair<Tree<Element>, Tree<Element>>, List<Double>> getValidationFunction() {
    return validationFitnessFunction;
  }

}
