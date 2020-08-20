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
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.MultiFileListenerFactory;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.problem.image.ImageReconstruction;
import it.units.malelab.jgea.problem.image.ImageUtils;
import it.units.malelab.jgea.problem.symbolicregression.MathUtils;
import it.units.malelab.jgea.problem.symbolicregression.RealFunction;
import it.units.malelab.jgea.representation.graph.*;
import it.units.malelab.jgea.representation.graph.numeric.Node;
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

import static it.units.malelab.jgea.core.util.Args.*;

/**
 * @author eric
 * @created 2020/08/05
 * @project jgea
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
    MultiFileListenerFactory<Object, RealFunction, Double> listenerFactory = new MultiFileListenerFactory<>(
        a("dir", "."),
        a("file", null)
    );
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
          Map<String, String> keys = new TreeMap<>(Map.of(
              "seed", Integer.toString(seed),
              "image", image.split(File.separator)[image.split(File.separator).length - 1],
              "evolver", evolverEntry.getKey()
          ));
          try {
            ImageReconstruction problem = new ImageReconstruction(ImageIO.read(new File(image)), true);
            Stopwatch stopwatch = Stopwatch.createStarted();
            Evolver<?, RealFunction, Double> evolver = evolverEntry.getValue();
            L.info(String.format("Starting %s", keys));
            Collection<RealFunction> solutions = evolver.solve(
                Misc.cached(problem.getFitnessFunction(), 10000),
                new Iterations(nIterations),
                new Random(seed),
                executorService,
                Listener.onExecutor(((Listener<Object, RealFunction, Double>) event -> {
                  RealFunction best = Misc.first(event.getOrderedPopulation().firsts()).getSolution();
                  try {
                    ImageIO.write(
                        ImageUtils.render(best, 100, 100, true),
                        "png",
                        new File(image.replaceFirst("\\.[a-z]+",
                            ".e" + evolverEntry.getKey() + ".g" + event.getState().getIterations() + ".png"))
                    );
                  } catch (IOException e) {
                    L.severe(String.format("Cannot write file due to %s", e));
                  }
                }).then(listenerFactory.build(
                    new Static(keys),
                    new Basic(),
                    new Population(),
                    new Diversity(),
                    new BestInfo("%7.5f")
                )), executorService)
            );
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
