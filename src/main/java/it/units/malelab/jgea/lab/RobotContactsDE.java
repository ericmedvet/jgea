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

import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.FitnessEvaluations;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.MultiFileListenerFactory;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.order.LexicoGraphical;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.problem.application.RobotPowerSupplyGeometry;
import it.units.malelab.jgea.representation.sequence.FixedLengthListFactory;
import it.units.malelab.jgea.representation.sequence.numeric.GaussianMutation;
import it.units.malelab.jgea.representation.sequence.numeric.GeometricCrossover;
import it.units.malelab.jgea.representation.sequence.numeric.UniformDoubleFactory;

import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static it.units.malelab.jgea.core.util.Args.*;

/**
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
    int[] runs = ri(a("runs", "0:1"));
    List<Double> ws = d(l(a("w", "25")));
    double v = d(a("v", "3"));
    int evaluations = i(a("nev", "10000"));
    int population = i(a("npop", "100"));
    double crossoverRate = d(a("cr", "0.8"));
    double differentialWeight = d(a("dw", "0.5"));
    List<Integer> nContacts = i(l(a("contacts", "10")));
    int nPoints = i(a("npoints", "100"));
    boolean symmetric = b(a("symmetry", "false"));
    List<String> fitnessNames = l(a("fitnesses", "madb"));
    Map<String, Pair<Function<double[], Boolean>, double[]>> robots = new LinkedHashMap<>();
    robots.put("Elisa-3", Pair.of((a) -> (a[0] >= 25d) && (a[0] < 30d), new double[]{25d, 30d}));
    robots.put("mBot", Pair.of((a) -> (a[0] >= 0d) && (Math.abs(a[0] * Math.cos(a[1])) <= 45d) && (Math.abs(a[0] * Math.sin(a[1])) <= 45d), new double[]{0d, 45d}));
    robots.put("Thymio-II", Pair.of((a) -> (a[0] >= 0d) && (((a[0] * Math.sin(a[1]) >= -50d) && (a[0] * Math.sin(a[1]) <= 0d) && (Math.abs(a[0] * Math.cos(a[1])) <= 75d)) || ((a[0] * Math.sin(a[1]) >= 0d) && (a[0] * Math.sin(a[1]) <= 30d) && (Math.abs(a[0] * Math.cos(a[1])) <= 110d))), new double[]{0d, 110d}));
    Map<String, RobotPowerSupplyGeometry.Objective[]> fitnesses = new LinkedHashMap<>();
    fitnesses.put("m", new RobotPowerSupplyGeometry.Objective[]{RobotPowerSupplyGeometry.Objective.CONTACT_MIN});
    fitnesses.put("md", new RobotPowerSupplyGeometry.Objective[]{RobotPowerSupplyGeometry.Objective.CONTACT_MIN, RobotPowerSupplyGeometry.Objective.DIST_AVG});
    fitnesses.put("mad", new RobotPowerSupplyGeometry.Objective[]{RobotPowerSupplyGeometry.Objective.CONTACT_MIN, RobotPowerSupplyGeometry.Objective.CONTACT_AVG, RobotPowerSupplyGeometry.Objective.DIST_AVG});
    fitnesses.put("mabd", new RobotPowerSupplyGeometry.Objective[]{RobotPowerSupplyGeometry.Objective.CONTACT_MIN, RobotPowerSupplyGeometry.Objective.CONTACT_AVG, RobotPowerSupplyGeometry.Objective.BALANCE, RobotPowerSupplyGeometry.Objective.DIST_AVG});
    fitnesses.put("mad", new RobotPowerSupplyGeometry.Objective[]{RobotPowerSupplyGeometry.Objective.CONTACT_MIN, RobotPowerSupplyGeometry.Objective.CONTACT_AVG, RobotPowerSupplyGeometry.Objective.BALANCE});
    fitnesses.put("mabd", new RobotPowerSupplyGeometry.Objective[]{RobotPowerSupplyGeometry.Objective.CONTACT_MIN, RobotPowerSupplyGeometry.Objective.CONTACT_AVG, RobotPowerSupplyGeometry.Objective.BALANCE, RobotPowerSupplyGeometry.Objective.DIST_AVG});
    fitnesses.put("mbad", new RobotPowerSupplyGeometry.Objective[]{RobotPowerSupplyGeometry.Objective.CONTACT_MIN, RobotPowerSupplyGeometry.Objective.BALANCE, RobotPowerSupplyGeometry.Objective.CONTACT_AVG, RobotPowerSupplyGeometry.Objective.DIST_AVG});
    fitnesses.keySet().retainAll(fitnessNames);
    //prepare things
    MultiFileListenerFactory<Object, Object, Object> listenerFactory = new MultiFileListenerFactory<>(a("dir", "."), a("file", null));
    //iterate
    for (int run : runs) {
      for (Map.Entry<String, Pair<Function<double[], Boolean>, double[]>> robot : robots.entrySet()) {
        for (Map.Entry<String, RobotPowerSupplyGeometry.Objective[]> fitness : fitnesses.entrySet()) {
          for (double w : ws) {
            for (int nContact : nContacts) {
              RobotPowerSupplyGeometry problem = new RobotPowerSupplyGeometry(
                  w, v,
                  robot.getValue().first(),
                  robot.getValue().second()[0],
                  robot.getValue().second()[1],
                  symmetric,
                  nPoints,
                  fitness.getValue()
              );
              StandardEvolver<List<Double>, List<Double>, List<Double>> evolver = new StandardEvolver<>(
                  Function.identity(),
                  new FixedLengthListFactory<>(nContact * 2, new UniformDoubleFactory(-1, 1)),
                  new LexicoGraphical(seq(fitness.getValue().length)).reversed().comparing(Individual::getFitness),
                  population,
                  Map.of(new GeometricCrossover().andThen(new GaussianMutation(0.1d)), 1d),
                  new Tournament(3),
                  new Worst(),
                  population,
                  true
              );
              Random random = new Random(run);
              Map<String, String> keys = new LinkedHashMap<>();
              keys.put("run", Integer.toString(run));
              keys.put("robot", robot.getKey());
              keys.put("fitness", fitness.getKey());
              keys.put("symmetry", Boolean.toString(symmetric));
              keys.put("w", Double.toString(w));
              keys.put("nc", Integer.toString(nContact));
              System.out.println(keys);
              Function<Individual<List<Double>, Object, Object>, double[]> toArray = (individual) -> {
                List<Double> s = individual.getGenotype();
                double[] values = new double[s.size()];
                for (int i = 0; i < s.size(); i++) {
                  values[i] = s.get(i);
                }
                return values;
              };
              Misc.cached(toArray.andThen(problem.getMinContactsFunction()), evaluations);
              try {
                evolver.solve(problem.getFitnessFunction(), new FitnessEvaluations(evaluations), random, executorService, Listener.onExecutor(listenerFactory.build(
                    new Static(keys),
                    new Basic(),
                    new Population(),
                    new BestInfo("%5.3f"),
                    //new Prefix("min.contacts", new FunctionOfOneBest<List<Double>, Object, Object>(Misc.cached(toArray.andThen(problem.getMinContactsFunction()), evaluations), "%%5.3f")),
                    //new FunctionOfBest("avg.contacts", problem.getAvgContactsFunction().cached(evaluations), "%%5.3f"),
                    //new FunctionOfBest("avg.dist", problem.getAvgDistFunction().cached(evaluations), "%%5.3f"),
                    //new FunctionOfBest("avg.balance", problem.getAvgBalanceFunction().cached(evaluations), "%%5.3f"),
                    //new FunctionOfBest("contacts", problem.getValidContactsFunction().cached(evaluations), "%2d"),
                    new BestPrinter(BestPrinter.Part.GENOTYPE)
                ), executorService));
              } catch (InterruptedException | ExecutionException ex) {
                L.log(Level.SEVERE, String.format("Cannot solve problem: %s", ex), ex);
              }
            }
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
