package it.units.malelab.jgea.lab.coevolution;

import com.google.common.collect.Lists;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.evolver.stopcondition.PerfectFitness;
import it.units.malelab.jgea.core.function.BiFunction;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.MultiFileListenerFactory;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.ranker.ComparableRanker;
import it.units.malelab.jgea.core.ranker.FitnessComparator;
import it.units.malelab.jgea.core.ranker.selector.Tournament;
import it.units.malelab.jgea.core.ranker.selector.Worst;
import it.units.malelab.jgea.problem.synthetic.OneMax;
import it.units.malelab.jgea.representation.sequence.UniformCrossover;
import it.units.malelab.jgea.representation.sequence.bit.BitFlipMutation;
import it.units.malelab.jgea.representation.sequence.bit.BitString;
import it.units.malelab.jgea.representation.sequence.bit.BitStringFactory;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class Starter extends Worker {

  public Starter(String[] args) throws FileNotFoundException {
    super(args);
  }

  @Override
  public void run() {
    runOneMax();
  }

  private void runSymbolixRegression() {
    int[] seeds = new int[]{0, 1, 2, 3, 4};
    int[] nPop1s = new int[]{10, 20, 30};
    int[] nPop2s = new int[]{10, 20, 30};
    int nGen = 200;

  }

  private void runOneMax() {
    int[] seeds = new int[]{0, 1, 2, 3, 4};
    int[] nPop1s = new int[]{10};
    int[] nPop2s = new int[]{10};
    int[] ls = new int[]{256, 512, 1024};
    String[] evolverTypes = new String[]{
        "standard",
        "coevo-or-min",
        "coevo-or-max",
        "coevo-and-min",
        "coevo-and-max"
    };
    int nGen = 200;
    Map<GeneticOperator<BitString>, Double> operators = new LinkedHashMap<>();
    operators.put(new BitFlipMutation(0.01d), 0.2d);
    operators.put(new UniformCrossover<>(), 0.8d);
    MultiFileListenerFactory listenerFactory = new MultiFileListenerFactory("/home/eric/experiments/coop-coevo/", "results-%s.txt");
    for (int l : ls) {
      for (int seed : seeds) {
        for (int nPop1 : nPop1s) {
          for (int nPop2 : nPop2s) {
            for (String evolverType : evolverTypes) {
              Random random = new Random(seed);
              OneMax p = new OneMax();
              Evolver evolver;
              if (evolverType.equals("standard")) {
                //evolution
                evolver = new StandardEvolver<>(
                    nPop1 * nPop2,
                    new BitStringFactory(l),
                    new ComparableRanker(new FitnessComparator(Function.identity())),
                    Function.identity(),
                    operators,
                    new Tournament<>(Math.max(nPop1 * nPop2 / 10, 2)),
                    new Worst<>(),
                    nPop1 * nPop2,
                    true,
                    Lists.newArrayList(new Iterations(nGen), new PerfectFitness<>(0d)),
                    1000,
                    false
                );
              } else if (evolverType.startsWith("coevo")) {
                //co-evolution
                StandardEvolver<BitString, BitString, Double> evolver1 = new StandardEvolver<>(
                    nPop1,
                    new BitStringFactory(l),
                    new ComparableRanker(new FitnessComparator(Function.identity())),
                    Function.identity(),
                    operators,
                    new Tournament<>(Math.max(nPop1 / 10, 2)),
                    new Worst<>(),
                    nPop1,
                    true,
                    Lists.newArrayList(new Iterations(nGen)),
                    0,
                    false
                );
                StandardEvolver<BitString, BitString, Double> evolver2 = new StandardEvolver<>(
                    nPop2,
                    new BitStringFactory(l),
                    new ComparableRanker(new FitnessComparator(Function.identity())),
                    Function.identity(),
                    operators,
                    new Tournament<>(Math.max(nPop1 / 5, 2)),
                    new Worst<>(),
                    nPop2,
                    false,
                    Lists.newArrayList(new Iterations(nGen)),
                    0,
                    false
                );
                BiFunction<BitString, BitString, BitString> composer;
                Function<Collection<Double>, Double> fitnessAggregator;
                if (evolverType.split("-")[1].equals("or")) {
                  composer = (BitString b1, BitString b2, Listener listener) -> b1.or(b2);
                } else {
                  composer = (BitString b1, BitString b2, Listener listener) -> b1.and(b2);
                }
                if (evolverType.split("-")[2].equals("min")) {
                  fitnessAggregator = (Collection<Double> fs, Listener listener) -> fs.stream().mapToDouble(Double::doubleValue).min().orElse(1d);
                } else if (evolverType.split("-")[2].equals("avg")) {
                  fitnessAggregator = (Collection<Double> fs, Listener listener) -> fs.stream().mapToDouble(Double::doubleValue).average().orElse(1d);
                } else {
                  fitnessAggregator = (Collection<Double> fs, Listener listener) -> fs.stream().mapToDouble(Double::doubleValue).max().orElse(1d);
                }
                evolver = new CooperativeCoevolver<>(
                    fitnessAggregator,
                    composer,
                    evolver1,
                    evolver2,
                    new BitStringFactory(l),
                    new BitStringFactory(l),
                    new ComparableRanker(new FitnessComparator(Function.identity())),
                    1,
                    1000
                );
              } else {
                continue;
              }
              try {
                Map<String, Object> staticKeys = new LinkedHashMap<>();
                staticKeys.put("l", l);
                staticKeys.put("nPop1", nPop1);
                staticKeys.put("nPop2", nPop2);
                staticKeys.put("seed", seed);
                staticKeys.put("evolver", evolverType);
                L.info("Doing: " + staticKeys);
                Collection solutions = evolver.solve(p, random, executorService, Listener.onExecutor(listenerFactory.build(
                    new Static(staticKeys),
                    new Basic(),
                    new Population(),
                    new Diversity(),
                    new BestInfo<>("%8.6f"),
                    new BestPrinter(BestPrinter.Part.SOLUTION),
                    new BestPrinter(BestPrinter.Part.GENOTYPE)
                ), executorService));
              } catch (InterruptedException | ExecutionException ex) {
                L.log(Level.SEVERE, "Some exception!", ex);
              }
            }
          }
        }
      }
    }
  }

  public final static void main(String[] args) throws FileNotFoundException {
    new Starter(args);
  }

}
