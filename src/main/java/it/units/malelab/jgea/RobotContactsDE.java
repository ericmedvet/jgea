/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea;

import com.google.common.collect.Lists;
import it.units.malelab.jgea.core.evolver.DifferentialEvolution;
import it.units.malelab.jgea.core.evolver.stopcondition.FitnessEvaluations;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.MultiFileListenerFactory;
import it.units.malelab.jgea.core.listener.collector.Basic;
import it.units.malelab.jgea.core.listener.collector.BestInfo;
import it.units.malelab.jgea.core.listener.collector.BestPrinter;
import it.units.malelab.jgea.core.listener.collector.DoubleArrayPrinter;
import it.units.malelab.jgea.core.listener.collector.FunctionOfBest;
import it.units.malelab.jgea.core.listener.collector.Population;
import it.units.malelab.jgea.core.listener.collector.Static;
import it.units.malelab.jgea.core.ranker.ComparableRanker;
import it.units.malelab.jgea.core.ranker.LexicoGraphicalMOComparator;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.problem.application.RobotPowerSupplyGeometry;
import it.units.malelab.jgea.problem.mapper.FitnessFunction;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class RobotContactsDE extends Worker {

  private final static Logger L = Logger.getLogger(RobotContactsDE.class.getName());

  public final static void main(String[] args) throws FileNotFoundException {
    new RobotContactsDE(args);
  }

  public RobotContactsDE(String[] args) throws FileNotFoundException {
    super(args);
  }

  @Override
  public void run() {
    //prepare parameters
    List<Integer> runs = i(l(a("runs", "0")));
    double w = d(a("w", "25"));
    double v = d(a("v", "3"));
    int evaluations = i(a("nev", "10000"));
    int population = i(a("npop", "100"));
    double crossoverRate = d(a("cr", "0.8"));
    double differentialWeight = d(a("dw", "0.5"));
    int contacts = i(a("contacts", "10"));
    int nPoints = i(a("npoints", "100"));
    Map<String, Pair<Function<double[], Boolean>, double[]>> robots = new LinkedHashMap<>();
    robots.put("Elisa-3", Pair.build((a, l) -> (a[0] >= 25d) && (a[0] < 30d), new double[]{25d, 30d}));
    robots.put("mBot", Pair.build((a, l) -> (a[0] >= 0d) && (Math.abs(a[0] * Math.cos(a[1])) <= 45d) && (Math.abs(a[0] * Math.sin(a[1])) <= 45d), new double[]{0d, 45d}));
    robots.put("Thymio-II", Pair.build((a, l) -> (a[0] >= 0d) && (((a[0] * Math.sin(a[1]) >= -50d) && (a[0] * Math.sin(a[1]) <= 0d) && (Math.abs(a[0] * Math.cos(a[1])) <= 75d)) || ((a[0] * Math.sin(a[1]) >= 0d) && (a[0] * Math.sin(a[1]) <= 30d) && (Math.abs(a[0] * Math.cos(a[1])) <= 110d))), new double[]{0d, 110d}));
    Map<String, RobotPowerSupplyGeometry.Objective[]> fitnesses = new LinkedHashMap<>();
    fitnesses.put("m", new RobotPowerSupplyGeometry.Objective[]{RobotPowerSupplyGeometry.Objective.CONTACT_MIN});
    fitnesses.put("md", new RobotPowerSupplyGeometry.Objective[]{RobotPowerSupplyGeometry.Objective.CONTACT_MIN, RobotPowerSupplyGeometry.Objective.DIST_AVG});
    fitnesses.put("mad", new RobotPowerSupplyGeometry.Objective[]{RobotPowerSupplyGeometry.Objective.CONTACT_MIN, RobotPowerSupplyGeometry.Objective.CONTACT_AVG, RobotPowerSupplyGeometry.Objective.DIST_AVG});
    //prepare things
    MultiFileListenerFactory listenerFactory = new MultiFileListenerFactory(a("dir", "."), a("file", null));
    //iterate
    for (int run : runs) {
      for (Map.Entry<String, Pair<Function<double[], Boolean>, double[]>> robot : robots.entrySet()) {
        for (Map.Entry<String, RobotPowerSupplyGeometry.Objective[]> fitness : fitnesses.entrySet()) {
          RobotPowerSupplyGeometry problem = new RobotPowerSupplyGeometry(
                  w, v,
                  robot.getValue().first(),
                  robot.getValue().second()[0],
                  robot.getValue().second()[1],
                  nPoints,
                  fitness.getValue()
          );
          Comparator<List<Double>> lgComparator = (Comparator) (new LexicoGraphicalMOComparator<>(seq(fitness.getValue().length)).reversed());
          DifferentialEvolution<List<Double>> de = new DifferentialEvolution<>(
                  population, i(a("threads", Integer.toString(Runtime.getRuntime().availableProcessors()))),
                  crossoverRate, differentialWeight,
                  contacts * 2,
                  0.5d, 1d,
                  new ComparableRanker<>(
                          (i1, i2) -> lgComparator.compare(i1.getFitness(), i2.getFitness())
                  ),
                  Lists.newArrayList(new FitnessEvaluations(evaluations)), evaluations);
          Random random = new Random(1);
          Map<String, String> keys = new LinkedHashMap<>();
          keys.put("run", Integer.toString(run));
          keys.put("robot", robot.getKey());
          keys.put("fitness", fitness.getKey());
          System.out.println(keys);
          try {
            de.solve(problem, random, executorService, Listener.onExecutor(listenerFactory.build(
                    new Static(keys),
                    new Basic(),
                    new Population(),
                    new BestInfo<>((Function) problem.getFitnessFunction(), "%5.3f"),
                    new FunctionOfBest("contacts", problem.getValidContactsFunction(), evaluations, "%2d"),
                    new BestPrinter(new DoubleArrayPrinter("%+5.3f"), "%s")
            ), executorService));
          } catch (InterruptedException | ExecutionException ex) {
            L.log(Level.SEVERE, String.format("Cannot solve problem: %s", ex), ex);
          }
        }
      }
    }
  }

  private int[] seq(int n) {
    int[] s = new int[n];
    for (int i = 0; i < n; i++) {
      s[i] = i;
    }
    return s;
  }

}
