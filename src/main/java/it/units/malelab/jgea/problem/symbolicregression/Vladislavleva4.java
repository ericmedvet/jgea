/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.ProblemWithValidation;
import it.units.malelab.jgea.core.fitness.SymbolicRegressionFitness;
import it.units.malelab.jgea.core.mapper.BoundMapper;
import it.units.malelab.jgea.core.mapper.DeterministicMapper;
import it.units.malelab.jgea.core.mapper.Mapper;
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.grammarbased.GrammarBasedProblem;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author eric
 */
public class Vladislavleva4 implements GrammarBasedProblem<String, Node<Element>, Double>, ProblemWithValidation<Node<Element>, Double> {
  
  //aka: UBall5D, https://www.researchgate.net/profile/Ekaterina_Katya_Vladislavleva/publication/224330345_Order_of_Nonlinearity_as_a_Complexity_Measure_for_Models_Generated_by_Symbolic_Regression_via_Pareto_Genetic_Programming/links/00b7d5306967756b1d000000.pdf

  private final static SymbolicRegressionFitness.TargetFunction TARGET_FUNCTION = new SymbolicRegressionFitness.TargetFunction() {
    @Override
    public double compute(double... v) {
      double s = 0;
      for (int i = 0; i < 5; i++) {
        s = s + (v[i] - 3) * (v[i] - 3);
      }
      return 10 / (5 + s);
    }

    @Override
    public String[] varNames() {
      return new String[]{"x1", "x2", "x3", "x4", "x5"};
    }
  };

  private final Grammar<String> grammar;
  private final DeterministicMapper<Node<String>, Node<Element>> solutionMapper;
  private final BoundMapper<Node<Element>, Double> fitnessMapper;
  private final Mapper<Node<Element>, Double> validationMapper;

  public Vladislavleva4(long seed) throws IOException {
    grammar = Grammar.fromFile(new File("grammars/symbolic-regression-vladislavleva4.bnf"));
    solutionMapper = new FormulaMapper();
    fitnessMapper = new SymbolicRegressionFitness(
            TARGET_FUNCTION,
            MathUtils.asObservations(
                    buildCases(0.05, 6.05, 1024, new Random(seed)),
                    TARGET_FUNCTION.varNames()
            ),
            true
    );
    validationMapper = new SymbolicRegressionFitness(
            TARGET_FUNCTION,
            MathUtils.asObservations(
                    buildCases(-0.25, 6.35, 5000, new Random(seed)),
                    TARGET_FUNCTION.varNames()
            ),
            true
    );
  }
  
  private static Map<String, double[]> buildCases(double min, double max, int count, Random random) {
    Map<String, double[]> map = new LinkedHashMap<>();
    for (String varName : TARGET_FUNCTION.varNames()) {
      map.put(varName, MathUtils.uniformSample(min, max, count, random));
    }
    return map;
  }

  @Override
  public Grammar<String> getGrammar() {
    return grammar;
  }

  @Override
  public DeterministicMapper<Node<String>, Node<Element>> getSolutionMapper() {
    return solutionMapper;
  }

  @Override
  public BoundMapper<Node<Element>, Double> getFitnessMapper() {
    return fitnessMapper;
  }

  @Override
  public Mapper<Node<Element>, Double> getValidationMapper() {
    return validationMapper;
  }

}
