/*
 * Copyright 2022 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jgea.sample.lab;

import com.google.common.base.Stopwatch;
import io.github.ericmedvet.jgea.core.listener.CSVPrinter;
import io.github.ericmedvet.jgea.core.listener.ListenerFactory;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.listener.TabularPrinter;
import io.github.ericmedvet.jgea.core.representation.graph.*;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Output;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.functiongraph.BaseFunction;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.functiongraph.FunctionGraph;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.functiongraph.FunctionNode;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.functiongraph.ShallowSparseFactory;
import io.github.ericmedvet.jgea.core.selector.Last;
import io.github.ericmedvet.jgea.core.selector.Tournament;
import io.github.ericmedvet.jgea.core.solver.IterativeSolver;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jgea.core.solver.StandardEvolver;
import io.github.ericmedvet.jgea.core.solver.StopConditions;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jgea.problem.image.ImageReconstruction;
import io.github.ericmedvet.jgea.sample.Worker;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

import static io.github.ericmedvet.jgea.core.listener.NamedFunctions.*;
import static io.github.ericmedvet.jgea.sample.Args.*;

/**
 * @author eric
 */

public class ImageExample extends Worker {

  public ImageExample(String[] args) {
    super(args);
  }

  public static void main(String[] args) throws FileNotFoundException {
    new ImageExample(args);
  }

  @Override
  public void run() {
    int nPop = i(a("nPop", "200"));
    int nTournament = 3;
    int nIterations = i(a("nIterations", "250"));
    int[] seeds = ri(a("seed", "0:1"));
    //BaseFunction[] baseFunctions = new BaseFunction[]{BaseFunction.STEP, BaseFunction.GAUSSIAN, BaseFunction
    // .PROT_INVERSE, BaseFunction.SQ, BaseFunction.SAW, BaseFunction.SIN};
    BaseFunction[] baseFunctions = new BaseFunction[]{BaseFunction.GAUSSIAN, BaseFunction.SIN, BaseFunction.SQ};
    List<String> images = l(a("images", "/home/eric/experiments/2020-graphea/image/glasses-32x32.png"));
    //listeners
    List<NamedFunction<? super POSetPopulationState<?, ?, ? extends Double>, ?>> functions = List.of(
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
        fitness().reformat("%5.3f").of(best()),
        fitnessMappingIteration().of(best())
    );
    List<NamedFunction<? super Map<String, Object>, ?>> kFunctions = List.of(
        attribute("seed").reformat("%2d"),
        attribute("image").reformat("%20.20s"),
        attribute("evolver").reformat("%20.20s")
    );
    ListenerFactory<? super POSetPopulationState<?, ?, ? extends Double>, Map<String, Object>> listenerFactory =
        new TabularPrinter<>(functions, kFunctions);
    if (a("file", null) != null) {
      listenerFactory = ListenerFactory.all(List.of(
          listenerFactory,
          new CSVPrinter<>(functions, kFunctions, new File(a("file", null)), true)
      ));
    }
    Map<String, IterativeSolver<? extends POSetPopulationState<?, UnivariateRealFunction, Double>, ImageReconstruction,
        UnivariateRealFunction>> solvers = new TreeMap<>();
    solvers.put(
        "graph-seq-ga",
        new StandardEvolver<>(
            FunctionGraph.mapper(
                List.of("x", "y"),
                List.of("out")
            ).andThen(UnivariateRealFunction::from),
            new ShallowSparseFactory(0d, 0d, 1d, List.of("x", "y"), List.of("out")),
            nPop,
            StopConditions.nOfIterations(nIterations),
            Map.of(
                new NodeAddition<>(
                    FunctionNode.sequentialIndexFactory(baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ),
                1d,
                new ArcModification<>((w, random) -> w + random.nextGaussian(), 1d),
                5d,
                new ArcAddition<>(RandomGenerator::nextGaussian, false),
                1d,
                new ArcRemoval<>(node -> node instanceof Output),
                0.1d,
                new AlignedCrossover<>(
                    (w1, w2, random) -> w1 + (w2 - w1) * (random.nextDouble() * 3d - 1d),
                    node -> node instanceof Output,
                    false
                ),
                1d
            ),
            new Tournament(nTournament),
            new Last(),
            nPop,
            true,
            false,
            (srp, r) -> new POSetPopulationState<>()
        )
    );
    //run
    for (int seed : seeds) {
      for (String image : images) {
        for (Map.Entry<String, IterativeSolver<? extends POSetPopulationState<?, UnivariateRealFunction, Double>,
            ImageReconstruction, UnivariateRealFunction>> solverEntry : solvers.entrySet()) {
          Map<String, Object> keys = Map.ofEntries(
              Map.entry(
                  "seed",
                  Integer.toString(seed)
              ),
              Map.entry(
                  "image",
                  image.split(File.separator)[image.split(File.separator).length - 1]
              ),
              Map.entry(
                  "evolver",
                  solverEntry.getKey()
              )
          );
          try {
            ImageReconstruction problem = new ImageReconstruction(ImageIO.read(new File(image)), true);
            Stopwatch stopwatch = Stopwatch.createStarted();
            IterativeSolver<? extends POSetPopulationState<?, UnivariateRealFunction, Double>, ImageReconstruction,
                UnivariateRealFunction> solver = solverEntry.getValue();
            L.info(String.format("Starting %s", keys));
            Collection<UnivariateRealFunction> solutions = solver.solve(
                problem,
                new Random(seed),
                executorService,
                listenerFactory.build(keys).deferred(executorService)
            );
            L.info(String.format(
                "Done %s: %d solutions in %4.1fs",
                keys,
                solutions.size(),
                (double) stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000d
            ));
          } catch (SolverException | IOException e) {
            L.severe(String.format("Cannot complete %s due to %s", keys, e));
          }
        }
      }
    }
    listenerFactory.shutdown();
  }

}
