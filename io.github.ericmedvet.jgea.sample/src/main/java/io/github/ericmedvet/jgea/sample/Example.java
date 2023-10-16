
package io.github.ericmedvet.jgea.sample;

import com.google.common.collect.Range;
import io.github.ericmedvet.jgea.core.listener.ListenerFactory;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.listener.TabularPrinter;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.representation.NamedUnivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp.GrammarBasedSubtreeMutation;
import io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp.GrammarRampedHalfAndHalf;
import io.github.ericmedvet.jgea.core.representation.sequence.FixedLengthListFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitStringFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitStringFlipMutation;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitStringUniformCrossover;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.GaussianMutation;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.HypercubeGeometricCrossover;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.UniformDoubleFactory;
import io.github.ericmedvet.jgea.core.representation.tree.SameRootSubtreeCrossover;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.TreeBasedUnivariateRealFunction;
import io.github.ericmedvet.jgea.core.selector.Last;
import io.github.ericmedvet.jgea.core.selector.Tournament;
import io.github.ericmedvet.jgea.core.solver.*;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.problem.regression.FormulaMapper;
import io.github.ericmedvet.jgea.problem.regression.MathUtils;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jgea.problem.regression.univariate.synthetic.Nguyen7;
import io.github.ericmedvet.jgea.problem.regression.univariate.synthetic.SyntheticUnivariateRegressionProblem;
import io.github.ericmedvet.jgea.problem.synthetic.Ackley;
import io.github.ericmedvet.jgea.problem.synthetic.OneMax;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static io.github.ericmedvet.jgea.core.listener.NamedFunctions.*;

public class Example extends Worker {

  public final static List<NamedFunction<? super POSetPopulationState<?, ?, ?>, ?>> BASIC_FUNCTIONS =
      List.of(
          iterations(),
          births(),
          elapsedSeconds(),
          size().of(all()),
          size().of(firsts()),
          size().of(lasts()),
          uniqueness().of(each(genotype())).of(all()),
          uniqueness().of(each(solution())).of(all()),
          uniqueness().of(each(fitness())).of(all()),
          size().of(genotype()).of(best()),
          size().of(solution()).of(best()),
          fitnessMappingIteration().of(best())
      );

  public final static List<NamedFunction<? super POSetPopulationState<?, ?, ? extends Double>, ?>> DOUBLE_FUNCTIONS =
      List.of(
          fitness().reformat("%5.3f").of(best()),
          hist(8).of(each(fitness())).of(all()),
          max(Double::compare).reformat("%5.3f").of(each(fitness())).of(all())
      );

  public Example(String[] args) {
    super(args);
  }

  public static void main(String[] args) {
    new Example(args);
  }

  @Override
  public void run() {
    String problem = a("problem", "oneMax");
    if (problem.equals("oneMax")) {
      runOneMax();
    }
    if (problem.equals("symbolicRegression")) {
      runSymbolicRegression();
    }
    if (problem.equals("ackley")) {
      runAckley();
    }
  }

  public void runAckley() {
    ListenerFactory<POSetPopulationState<?, ?, ? extends Double>, Map<String, Object>> listenerFactory =
        new TabularPrinter<>(
            Misc.concat(List.of(BASIC_FUNCTIONS, DOUBLE_FUNCTIONS)),
            List.of()
        );
    Random r = new Random(1);
    TotalOrderQualityBasedProblem<List<Double>, Double> p = new Ackley(10);
    List<IterativeSolver<? extends POSetPopulationState<List<Double>, List<Double>, Double>,
        TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>>> solvers = new ArrayList<>();
    solvers.add(new RandomSearch<>(
        Function.identity(),
        new FixedLengthListFactory<>(10, new UniformDoubleFactory(0, 1)),
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100))
    ));
    solvers.add(new RandomWalk<>(
        Function.identity(),
        new FixedLengthListFactory<>(10, new UniformDoubleFactory(0, 1)),
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100)),
        new GaussianMutation(0.01d)
    ));
    solvers.add(new StandardEvolver<POSetPopulationState<List<Double>, List<Double>, Double>,
        TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>, List<Double>, Double>(
        Function.identity(),
        new FixedLengthListFactory<>(10, new UniformDoubleFactory(0, 1)),
        100,
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100)),
        Map.of(new HypercubeGeometricCrossover(Range.open(-1d, 2d)).andThen(new GaussianMutation(0.01)), 1d),
        new Tournament(5),
        new Last(),
        100,
        true,
        false,
        (problem, random) -> new POSetPopulationState<>()
    ));
    solvers.add(new SimpleEvolutionaryStrategy<>(
        Function.identity(),
        new FixedLengthListFactory<>(10, new UniformDoubleFactory(0, 1)),
        100,
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100)),
        25,
        1,
        0.1,
        false
    ));
    for (IterativeSolver<? extends POSetPopulationState<List<Double>, List<Double>, Double>,
        TotalOrderQualityBasedProblem<List<Double>, Double>, List<Double>> solver : solvers) {
      System.out.println(solver.getClass().getSimpleName());
      try {
        Collection<List<Double>> solutions = solver.solve(
            p,
            r,
            executorService,
            listenerFactory.build(Map.of()).deferred(executorService)
        );
        System.out.printf("Found %d solutions with " + "%s.%n", solutions.size(), solver.getClass().getSimpleName());
      } catch (SolverException e) {
        e.printStackTrace();
      }
    }
  }

  public void runOneMax() {
    int size = 1000;
    Random r = new Random(1);
    QualityBasedProblem<BitString, Double> p = new OneMax(size);
    List<NamedFunction<? super POSetPopulationState<?, ?, ?>, ?>> keysFunctions = List.of();
    ListenerFactory<POSetPopulationState<?, ?, ? extends Double>, Map<String, Object>> listenerFactory =
        ListenerFactory.all(
            List.of(new TabularPrinter<>(
                Misc.concat(List.of(keysFunctions, BASIC_FUNCTIONS, DOUBLE_FUNCTIONS)),
                List.of(attribute("solver"))
            )));
    List<IterativeSolver<? extends POSetPopulationState<?, BitString, Double>, QualityBasedProblem<BitString, Double>
        , BitString>> solvers = new ArrayList<>();
    solvers.add(new RandomSearch<>(
        Function.identity(),
        new BitStringFactory(size),
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100))
    ));
    solvers.add(new RandomWalk<>(
        Function.identity(),
        new BitStringFactory(size),
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100)),
        new BitStringFlipMutation(0.01d)
    ));
    solvers.add(new StandardEvolver<POSetPopulationState<BitString, BitString, Double>, QualityBasedProblem<BitString
        , Double>, BitString, BitString, Double>(
        Function.identity(),
        new BitStringFactory(size),
        100,
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100)),
        Map.of(new BitStringUniformCrossover(), 0.8d, new BitStringFlipMutation(0.01d), 0.2d),
        new Tournament(5),
        new Last(),
        100,
        true,
        false,
        (problem, random) -> new POSetPopulationState<>()
    ));
    solvers.add(new StandardWithEnforcedDiversityEvolver<POSetPopulationState<BitString, BitString, Double>,
        QualityBasedProblem<BitString, Double>, BitString, BitString, Double>(
        Function.identity(),
        new BitStringFactory(size),
        100,
        StopConditions.targetFitness(0d).or(StopConditions.nOfIterations(100)),
        Map.of(new BitStringUniformCrossover(), 0.8d, new BitStringFlipMutation(0.01d), 0.2d),
        new Tournament(5),
        new Last(),
        100,
        true,
        false,
        (problem, random) -> new POSetPopulationState<>(),
        100
    ));
    for (IterativeSolver<? extends POSetPopulationState<?, BitString, Double>, QualityBasedProblem<BitString, Double>
        , BitString> evolver : solvers) {
      try {
        Collection<BitString> solutions = evolver.solve(
            p,
            r,
            executorService,
            listenerFactory.build(Map.of("solver", evolver.getClass().getSimpleName())).deferred(executorService)
        );
        System.out.printf("Found %d solutions with " + "%s.%n", solutions.size(), evolver.getClass().getSimpleName());
      } catch (SolverException e) {
        e.printStackTrace();
      }
    }
    listenerFactory.shutdown();
  }

  public void runSymbolicRegression() {
    ListenerFactory<? super POSetPopulationState<?, ?, ? extends Double>, Void> listenerFactory = new TabularPrinter<>(
        Misc.concat(List.of(BASIC_FUNCTIONS, DOUBLE_FUNCTIONS)),
        List.of()
    );
    Random r = new Random(1);
    SyntheticUnivariateRegressionProblem p = new Nguyen7(UnivariateRegressionFitness.Metric.MSE, 1);
    StringGrammar<String> srGrammar;
    try {
      srGrammar = StringGrammar.load(StringGrammar.class.getResourceAsStream("/grammars/1d/symbolic-regression-nguyen7.bnf"));
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    List<IterativeSolver<? extends POSetPopulationState<?, NamedUnivariateRealFunction, Double>,
        SyntheticUnivariateRegressionProblem,
        NamedUnivariateRealFunction>> solvers = new ArrayList<>();
    solvers.add(new StandardEvolver<>(
        new FormulaMapper()
            .andThen(n -> new TreeBasedUnivariateRealFunction(n,
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames().get(0)
            ))
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new GrammarRampedHalfAndHalf<>(3, 12, srGrammar),
        100,
        StopConditions.nOfIterations(100),
        Map.of(new SameRootSubtreeCrossover<>(12), 0.8d, new GrammarBasedSubtreeMutation<>(12, srGrammar), 0.2d),
        new Tournament(5),
        new Last(),
        100,
        true,
        false,
        (srp, rnd) -> new POSetPopulationState<>()
    ));
    solvers.add(new StandardWithEnforcedDiversityEvolver<>(
        new FormulaMapper()
            .andThen(n -> new TreeBasedUnivariateRealFunction(n,
                p.qualityFunction().getDataset().xVarNames(),
                p.qualityFunction().getDataset().yVarNames().get(0)
            ))
            .andThen(MathUtils.linearScaler(p.qualityFunction())),
        new GrammarRampedHalfAndHalf<>(3, 12, srGrammar),
        100,
        StopConditions.nOfIterations(100),
        Map.of(new SameRootSubtreeCrossover<>(12), 0.8d, new GrammarBasedSubtreeMutation<>(12, srGrammar), 0.2d),
        new Tournament(5),
        new Last(),
        100,
        true,
        false,
        (srp, rnd) -> new POSetPopulationState<>(),
        100
    ));
    for (IterativeSolver<? extends POSetPopulationState<?, NamedUnivariateRealFunction, Double>,
        SyntheticUnivariateRegressionProblem,
        NamedUnivariateRealFunction> solver : solvers) {
      System.out.println(solver.getClass().getSimpleName());
      try {
        Collection<NamedUnivariateRealFunction> solutions = solver.solve(
            p,
            r,
            executorService,
            listenerFactory.build(null).deferred(executorService)
        );
        System.out.printf("Found %d solutions with %s.%n", solutions.size(), solver.getClass().getSimpleName());
      } catch (SolverException e) {
        e.printStackTrace();
      }
    }
  }

}
