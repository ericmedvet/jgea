/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
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

package it.units.malelab.jgea.sample.lab;

import com.google.common.base.Stopwatch;
import it.units.malelab.jgea.core.listener.CSVPrinter;
import it.units.malelab.jgea.core.listener.ListenerFactory;
import it.units.malelab.jgea.core.listener.NamedFunction;
import it.units.malelab.jgea.core.listener.TabularPrinter;
import it.units.malelab.jgea.core.representation.graph.*;
import it.units.malelab.jgea.core.representation.graph.numeric.Output;
import it.units.malelab.jgea.core.representation.graph.numeric.RealFunction;
import it.units.malelab.jgea.core.representation.graph.numeric.functiongraph.BaseFunction;
import it.units.malelab.jgea.core.representation.graph.numeric.functiongraph.FunctionGraph;
import it.units.malelab.jgea.core.representation.graph.numeric.functiongraph.FunctionNode;
import it.units.malelab.jgea.core.representation.graph.numeric.functiongraph.ShallowSparseFactory;
import it.units.malelab.jgea.core.selector.Last;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.solver.IterativeSolver;
import it.units.malelab.jgea.core.solver.SolverException;
import it.units.malelab.jgea.core.solver.StandardEvolver;
import it.units.malelab.jgea.core.solver.StopConditions;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jgea.problem.image.ImageReconstruction;
import it.units.malelab.jgea.problem.symbolicregression.MathUtils;
import it.units.malelab.jgea.sample.Worker;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

import static it.units.malelab.jgea.core.listener.NamedFunctions.*;
import static it.units.malelab.jgea.core.util.Args.*;

/**
 * @author eric
 */

// /usr/lib/jvm/jdk-14.0.1/bin/java -cp ~/IdeaProjects/jgea/out/artifacts/jgea_jar/jgea.jar it.units.malelab.jgea
// .sample.lab
// .SymbolicRegressionComparison seed=0:10 file=results-%s.txt
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
          new CSVPrinter<>(functions, kFunctions, new File(a("file", null)))
      ));
    }
    Map<String, IterativeSolver<? extends POSetPopulationState<?, RealFunction, Double>, ImageReconstruction,
        RealFunction>> solvers = new TreeMap<>();
    solvers.put(
        "graph-seq-ga",
        new StandardEvolver<>(
            FunctionGraph.builder().andThen(MathUtils.fromMultivariateBuilder()),
            new ShallowSparseFactory(0d, 0d, 1d, 2, 1),
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
        for (Map.Entry<String, IterativeSolver<? extends POSetPopulationState<?, RealFunction, Double>,
            ImageReconstruction, RealFunction>> solverEntry : solvers.entrySet()) {
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
            IterativeSolver<? extends POSetPopulationState<?, RealFunction, Double>, ImageReconstruction,
                RealFunction> solver = solverEntry.getValue();
            L.info(String.format("Starting %s", keys));
            Collection<RealFunction> solutions = solver.solve(
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
