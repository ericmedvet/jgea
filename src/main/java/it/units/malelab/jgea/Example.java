/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.evolver.RandomSearch;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.listener.collector.Basic;
import it.units.malelab.jgea.core.listener.collector.BestInfo;
import it.units.malelab.jgea.core.listener.collector.BestPrinter;
import it.units.malelab.jgea.core.listener.collector.Population;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.problem.synthetic.OneMax;
import it.units.malelab.jgea.representation.sequence.UniformCrossover;
import it.units.malelab.jgea.representation.sequence.bit.BitFlipMutation;
import it.units.malelab.jgea.representation.sequence.bit.BitString;
import it.units.malelab.jgea.representation.sequence.bit.BitStringFactory;
import it.units.malelab.jgea.representation.sequence.numeric.GaussianMutation;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * @author eric
 */
public class Example extends Worker {

  public Example(String[] args) throws FileNotFoundException {
    super(args);
  }

  public final static void main(String[] args) throws FileNotFoundException {
    new Example(args);
  }

  @Override
  public void run() {
    Random r = new Random(1);
    Problem<BitString, Double> p = new OneMax();
    Evolver<BitString, BitString, Double> evolver = new RandomSearch<>(
        Function.identity(),
        new BitStringFactory(100),
        PartialComparator.from(Double.class).on(Individual::getFitness)
    );
    evolver = new StandardEvolver<>(
        Function.identity(),
        new BitStringFactory(100),
        PartialComparator.from(Double.class).on(Individual::getFitness),
        100,
        Map.of(
            new UniformCrossover<>(Boolean.class), 0.8d,
            new BitFlipMutation(0.01d), 0.2d
        ),
        new Tournament(5),
        new Worst(),
        100,
        true
    );
    try {
      Collection<BitString> solutions = evolver.solve(p, new Iterations(100), r, executorService, listener(
          new Basic(),
          new Population(),
          new BestInfo("%5.3f"),
          new BestPrinter(BestPrinter.Part.GENOTYPE)
      ));
      System.out.printf("Found %d solutions:%n%s%n", solutions.size(), solutions);
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }
}
