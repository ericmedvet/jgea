/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.lab;

import com.google.common.base.Stopwatch;
import com.google.common.graph.ValueGraph;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.StandardWithEnforcedDiversity;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.evolver.stopcondition.TargetFitness;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.MultiFileListenerFactory;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.problem.symbolicregression.*;
import it.units.malelab.jgea.problem.symbolicregression.element.Constant;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import it.units.malelab.jgea.problem.symbolicregression.element.Operator;
import it.units.malelab.jgea.problem.symbolicregression.element.Variable;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarBasedSubtreeMutation;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarRampedHalfAndHalf;
import it.units.malelab.jgea.representation.graph.*;
import it.units.malelab.jgea.representation.graph.multivariatefunction.*;
import it.units.malelab.jgea.representation.tree.*;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static it.units.malelab.jgea.core.util.Args.i;
import static it.units.malelab.jgea.core.util.Args.ri;

/**
 * @author eric
 * @created 2020/08/05
 * @project jgea
 */

// /usr/lib/jvm/jdk-14.0.1/bin/java -cp ~/IdeaProjects/jgea/out/artifacts/jgea_jar/jgea.jar it.units.malelab.jgea.lab.SymbolicRegressionComparison seed=0:10 file=results-%s.txt
public class SymbolicRegressionComparison extends Worker {

  public SymbolicRegressionComparison(String[] args) throws FileNotFoundException {
    super(args);
  }

  public static void main(String[] args) throws FileNotFoundException {
    new SymbolicRegressionComparison(args);
  }

  @Override
  public void run() {
    int nPop = i(a("nPop", "100"));
    int maxNodes = i(a("maxNodes", "10"));
    int nTournament = 5;
    int nIterations = i(a("nIterations", "3"));
    int[] seeds = ri(a("seed", "0:1"));
    boolean[] enforceDiversities = new boolean[]{false, true};
    Operator[] operators = new Operator[]{Operator.ADDITION, Operator.SUBTRACTION, Operator.MULTIPLICATION, Operator.PROT_DIVISION};
    Double[] constants = new Double[]{0.1, 1d, 10d};
    List<SymbolicRegressionProblem> problems = List.of(
        new Nguyen7(1),
        new Keijzer6(),
        new Polynomial4(),
        new Vladislavleva4(1),
        new Pagie1()
    );
    MultiFileListenerFactory<Object, RealFunction, Double> listenerFactory = new MultiFileListenerFactory<>(
        a("dir", "."),
        a("file", null)
    );
    Map<String, Function<SymbolicRegressionProblem, Evolver<?, RealFunction, Double>>> evolvers = Map.of(
        "treegp", p -> {
          IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
              IndependentFactory.picker(Arrays.stream(vars(p.arity())).sequential().map(Variable::new).toArray(Variable[]::new)),
              IndependentFactory.picker(Arrays.stream(constants).map(Constant::new).toArray(Constant[]::new))
          );
          return new StandardEvolver<Tree<Element>, RealFunction, Double>(
              ((Function<Tree<Element>, RealFunction>) t -> new TreeBasedRealFunction(t, vars(p.arity())))
                  .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
              new RampedHalfAndHalf<>(
                  4, maxNodes,
                  Operator.arityFunction(),
                  IndependentFactory.picker(operators),
                  terminalFactory
              ),
              PartialComparator.from(Double.class).on(Individual::getFitness),
              nPop,
              Map.of(
                  new SubtreeCrossover<>(maxNodes), 0.8d,
                  new SubtreeMutation<>(maxNodes, new GrowTreeBuilder<>(
                      Operator.arityFunction(),
                      IndependentFactory.picker(operators),
                      terminalFactory
                  )), 0.2d
              ),
              new Tournament(nTournament),
              new Worst(),
              nPop,
              true
          );
        },
        "cfggp", p -> {
          SymbolicRegressionGrammar g = new SymbolicRegressionGrammar(
              List.of(operators),
              List.of(vars(p.arity())),
              List.of(constants)
          );
          return new StandardEvolver<Tree<String>, RealFunction, Double>(
              new FormulaMapper()
                  .andThen(n -> TreeBasedRealFunction.from(n, vars(p.arity())))
                  .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
              new GrammarRampedHalfAndHalf<>(6, maxNodes + 4, g),
              PartialComparator.from(Double.class).on(Individual::getFitness),
              nPop,
              Map.of(
                  new SameRootSubtreeCrossover<>(maxNodes + 4), 0.8d,
                  new GrammarBasedSubtreeMutation<>(maxNodes + 4, g), 0.2d
              ),
              new Tournament(nTournament),
              new Worst(),
              nPop,
              true
          );
        },
        "graphea", p -> new StandardEvolver<ValueGraph<Node, Double>, RealFunction, Double>(
            MultivariateRealFunctionGraph.builder()
                .andThen(GraphBasedRealFunction.builder())
                .andThen(MathUtils.linearScaler((SymbolicRegressionFitness) p.getFitnessFunction())),
            new ShallowGraphFactory(0.5d, 0d, 1d, p.arity(), 1),
            PartialComparator.from(Double.class).on(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<>(
                    FunctionNode.factory(maxNodes, BaseFunction.RE_LU, BaseFunction.GAUSSIAN, BaseFunction.PROT_INVERSE),
                    Random::nextGaussian
                ), 2d,
                new EdgeModification<>((w, random) -> w + random.nextGaussian()), 1d,
                new EdgeAddition<>(Random::nextGaussian, false), 1d,
                new EdgeRemoval<>(node -> node instanceof OutputNode), 0.1d,
                new AlignedCrossover<>(
                    (w1, w2, random) -> w1 + (w2 - w1) - (random.nextDouble() * 3d - 1d),
                    node -> node instanceof OutputNode,
                    false
                ), 1d
            ),
            new Tournament(nTournament),
            new Worst(),
            nPop,
            true
        )
    );
    //run
    for (int seed : seeds) {
      for (SymbolicRegressionProblem problem : problems) {
        for (Map.Entry<String, Function<SymbolicRegressionProblem, Evolver<?, RealFunction, Double>>> evolverEntry : evolvers.entrySet()) {
          for (boolean enforceDiversity : enforceDiversities) {
            Map<String, String> keys = new TreeMap<>(Map.of(
                "seed", Integer.toString(seed),
                "problem", problem.getClass().getSimpleName().toLowerCase(),
                "evolver", evolverEntry.getKey(),
                "diversity", Boolean.toString(enforceDiversity)
            ));
            L.info(String.format("Starting %s", keys));
            try {
              Stopwatch stopwatch = Stopwatch.createStarted();
              Evolver<?, RealFunction, Double> evolver = evolverEntry.getValue().apply(problem);
              if (enforceDiversity && evolver instanceof StandardEvolver) {
                evolver = StandardWithEnforcedDiversity.from((StandardEvolver<?, RealFunction, Double>) evolver, 100);
              }
              Collection<RealFunction> solutions = evolver.solve(
                  Misc.cached(problem.getFitnessFunction(), 10000),
                  new Iterations(nIterations),
                  new Random(seed),
                  executorService,
                  Listener.onExecutor(listenerFactory.build(
                      new Static(keys),
                      new Basic(),
                      new Population(),
                      new Diversity(),
                      new BestInfo("%7.5f"),
                      new FunctionOfOneBest<>(i -> List.of(new Item(
                          "validation.fitness",
                          problem.getValidationFunction().apply(i.getSolution()),
                          "%7.5f"
                      )))
                  ), executorService));
              L.info(String.format("Done %s: %d solutions in %4.1fs",
                  keys,
                  solutions.size(),
                  (double) stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000d
              ));
            } catch (InterruptedException | ExecutionException e) {
              L.severe(String.format("Cannot complete %s due to %s",
                  keys,
                  e
              ));
            }
          }
        }
      }
    }
  }

  private static String[] vars(int n) {
    if (n == 1) {
      return new String[]{"x"};
    }
    String[] vars = new String[n];
    for (int i = 0; i < n; i++) {
      vars[i] = "x" + i;
    }
    return vars;
  }
}
