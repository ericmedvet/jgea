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

package it.units.malelab.jgea.lab;

import com.google.common.base.Stopwatch;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.consumer.*;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.problem.image.ImageReconstruction;
import it.units.malelab.jgea.problem.symbolicregression.MathUtils;
import it.units.malelab.jgea.problem.symbolicregression.RealFunction;
import it.units.malelab.jgea.representation.graph.*;
import it.units.malelab.jgea.representation.graph.numeric.Output;
import it.units.malelab.jgea.representation.graph.numeric.functiongraph.BaseFunction;
import it.units.malelab.jgea.representation.graph.numeric.functiongraph.FunctionGraph;
import it.units.malelab.jgea.representation.graph.numeric.functiongraph.FunctionNode;
import it.units.malelab.jgea.representation.graph.numeric.functiongraph.ShallowSparseFactory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static it.units.malelab.jgea.core.consumer.NamedFunctions.*;
import static it.units.malelab.jgea.core.util.Args.*;

/**
 * @author eric
 */

// /usr/lib/jvm/jdk-14.0.1/bin/java -cp ~/IdeaProjects/jgea/out/artifacts/jgea_jar/jgea.jar it.units.malelab.jgea.lab.SymbolicRegressionComparison seed=0:10 file=results-%s.txt
public class ImageExample extends Worker {

  public ImageExample(String[] args) throws FileNotFoundException {
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
    //BaseFunction[] baseFunctions = new BaseFunction[]{BaseFunction.STEP, BaseFunction.GAUSSIAN, BaseFunction.PROT_INVERSE, BaseFunction.SQ, BaseFunction.SAW, BaseFunction.SIN};
    BaseFunction[] baseFunctions = new BaseFunction[]{BaseFunction.GAUSSIAN, BaseFunction.SIN, BaseFunction.SQ};
    List<String> images = l(a("images", "/home/eric/experiments/2020-graphea/image/glasses-32x32.png"));
    //consumers
    Map<String, Object> keys = new HashMap<>();
    List<NamedFunction<Event<?, ? extends RealFunction, ? extends Double>, ?>> functions = List.of(
        constant("seed", "%2d", keys),
        constant("image", "%20.20s", keys),
        constant("evolver", "%20.20s", keys),
        iterations(),
        births(),
        elapsedSeconds(),
        size().of(all()),
        size().of(firsts()),
        size().of(lasts()),
        uniqueness().of(map(genotype())).of(all()),
        uniqueness().of(map(solution())).of(all()),
        uniqueness().of(map(fitness())).of(all()),
        size().of(genotype()).of(best()),
        size().of(solution()).of(best()),
        fitness().reformat("%5.3f").of(best()),
        birthIteration().of(best())
    );
    List<Consumer.Factory<Object, RealFunction, Double, Void>> factories = new ArrayList<>();
    factories.add(new TabularPrinter<>(functions, System.out, 10, true));
    if (a("file", null) != null) {
      factories.add(new CSVPrinter<>(functions, new File(a("file", null))));
    }
    Map<String, Evolver<?, RealFunction, Double>> evolvers = Map.ofEntries(
        Map.entry("graph-seq-ga", new StandardEvolver<Graph<Node, Double>, RealFunction, Double>(
            FunctionGraph.builder()
                .andThen(MathUtils.fromMultivariateBuilder()),
            new ShallowSparseFactory(0d, 0d, 1d, 2, 1),
            PartialComparator.from(Double.class).comparing(Individual::getFitness),
            nPop,
            Map.of(
                new NodeAddition<>(
                    FunctionNode.sequentialIndexFactory(baseFunctions),
                    (w, r) -> w,
                    (w, r) -> r.nextGaussian()
                ), 1d,
                new ArcModification<>((w, random) -> w + random.nextGaussian(), 1d), 5d,
                new ArcAddition<>(Random::nextGaussian, false), 1d,
                new ArcRemoval<>(node -> node instanceof Output), 0.1d,
                new AlignedCrossover<>(
                    (w1, w2, random) -> w1 + (w2 - w1) * (random.nextDouble() * 3d - 1d),
                    node -> node instanceof Output,
                    false
                ), 1d
            ),
            new Tournament(nTournament),
            new Worst(),
            nPop,
            true
        ))
    );
    //run
    for (int seed : seeds) {
      for (String image : images) {
        for (Map.Entry<String, Evolver<?, RealFunction, Double>> evolverEntry : evolvers.entrySet()) {
          keys.putAll(Map.of(
              "seed", Integer.toString(seed),
              "image", image.split(File.separator)[image.split(File.separator).length - 1],
              "evolver", evolverEntry.getKey()
          ));
          try {
            ImageReconstruction problem = new ImageReconstruction(ImageIO.read(new File(image)), true);
            Consumer<Object, RealFunction, Double, ?> consumer = Consumer.of(factories.stream()
                .map(Consumer.Factory::build)
                .collect(Collectors.toList()))
                .deferred(executorService);
            Stopwatch stopwatch = Stopwatch.createStarted();
            Evolver<?, RealFunction, Double> evolver = evolverEntry.getValue();
            L.info(String.format("Starting %s", keys));
            Collection<RealFunction> solutions = evolver.solve(
                Misc.cached(problem.getFitnessFunction(), 10000),
                new Iterations(nIterations),
                new Random(seed),
                executorService,
                consumer
            );
            consumer.consume(solutions);
            L.info(String.format("Done %s: %d solutions in %4.1fs",
                keys,
                solutions.size(),
                (double) stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000d
            ));
          } catch (InterruptedException | ExecutionException | IOException e) {
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
