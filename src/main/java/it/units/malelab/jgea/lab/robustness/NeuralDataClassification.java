package it.units.malelab.jgea.lab.robustness;

import com.google.common.base.Stopwatch;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.TotalOrderQualityBasedProblem;
import it.units.malelab.jgea.core.listener.*;
import it.units.malelab.jgea.core.listener.telegram.TelegramProgressMonitor;
import it.units.malelab.jgea.core.solver.*;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.problem.classification.ClassificationFitness;
import it.units.malelab.jgea.problem.classification.Classifier;
import it.units.malelab.jgea.problem.classification.DatasetClassificationProblem;
import it.units.malelab.jgea.problem.classification.NeuralNetworkClassifier;
import it.units.malelab.jgea.representation.sequence.FixedLengthListFactory;
import it.units.malelab.jgea.representation.sequence.numeric.UniformDoubleFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.IntStream;

import static it.units.malelab.jgea.core.listener.NamedFunctions.*;
import static it.units.malelab.jgea.core.util.Args.i;
import static it.units.malelab.jgea.core.util.Args.ri;

public class NeuralDataClassification extends Worker {

  private record ValidationEvent(Classifier<double[], Integer> classifier, double fitness, double validationFitness) {
  }

  public static void main(String[] args) {
    new NeuralDataClassification(args);
  }

  public NeuralDataClassification(String[] args) {
    super(args);
  }

  static class TotallyOrderedClassificationProblem extends DatasetClassificationProblem
      implements TotalOrderQualityBasedProblem<Classifier<double[], Integer>, List<Double>> {

    public TotallyOrderedClassificationProblem(String filename, int numberOfClasses, String yColumnName, int folds, int i,
                                               ClassificationFitness.Metric learningErrorMetric,
                                               ClassificationFitness.Metric validationErrorMetric) throws IOException {
      super(filename, numberOfClasses, yColumnName, folds, i, learningErrorMetric, validationErrorMetric);
    }

    @Override
    public Comparator<List<Double>> totalOrderComparator() {
      return Comparator.comparingDouble(l -> l.get(0));
    }

  }

  @Override
  public void run() {
    String telegramBotId = a("telegramBotId", null);
    long telegramChatId = Long.parseLong(a("telegramChatId", "0"));

    String dataset = a("dataset", "D:\\Research\\Cooperative_coevolution\\datasets\\german_one_hot.csv");
    String classificationColumn = a("column", "Risk");
    int numberOfClasses = i(a("nClasses", "2"));
    int nFolds = i(a("folds", "5"));
    int nHiddenLayers = i(a("nLayers", "3"));
    int nPop = i(a("nPop", "10"));
    int nEvals = i(a("nEvals", "100"));
    int[] seeds = ri(a("seed", "0:10"));

    boolean output = a("output", "true").startsWith("t");
    String bestFile = a("bestFile", "best_nn.txt");
    String lastFile = a("lastFile", "last_nn.txt");
    String validationFile = a("validationFile", "validation_nn.txt");

    ClassificationFitness.Metric metric = ClassificationFitness.Metric.ERROR_RATE;

    Map<String, TotallyOrderedClassificationProblem> problemMap = buildProblems(dataset, numberOfClasses, classificationColumn, nFolds, metric);

    List<NamedFunction<? super POSetPopulationState<?, ?, List<Double>>, ?>> basicFunctions = List.of(
        iterations(), births(), fitnessEvaluations(), elapsedSeconds()
    );
    List<NamedFunction<? super Individual<?, ?, List<Double>>, ?>> basicIndividualFunctions = List.of(
        size().of(genotype()),
        size().of(solution()),
        fitnessMappingIteration(),
        f("fitness", "%.3f", (Function<List<Double>, Double>) l -> l.get(0)).of(fitness())
    );
    NamedFunction<POSetPopulationState<?, ?, List<Double>>, Individual<?, ?, List<Double>>> bestFunction =
        ((NamedFunction<POSetPopulationState<?, ?, List<Double>>, Individual<?, ?, List<Double>>>) state -> Misc.first(
            state.getPopulation().firsts())).rename("best");
    bestFunction.then(basicIndividualFunctions);
    List<NamedFunction<? super POSetPopulationState<?, ?, List<Double>>, ?>> populationFunctions = List.of(
        size().of(all()),
        size().of(firsts()),
        size().of(lasts()),
        uniqueness().of(each(genotype())).of(all()),
        uniqueness().of(each(solution())).of(all()),
        uniqueness().of(each(fitness())).of(all())
    );
    List<NamedFunction<? super POSetPopulationState<?, ?, List<Double>>, ?>> functions = Misc.concat(List.of(
        basicFunctions,
        bestFunction.then(basicIndividualFunctions),
        populationFunctions
    ));
    List<NamedFunction<? super Map<String, Object>, ?>> kFunctions = List.of(
        attribute("seed").reformat("%2d"),
        attribute("problem").reformat(NamedFunction.formatOfLongest(problemMap.keySet().stream().toList())),
        attribute("evolver").reformat("%20.20s")
    );
    List<NamedFunction<? super Map<String, Object>, ?>> validationKFunctions = List.of(
        attribute("seed").reformat("%2d"),
        attribute("problem").reformat(NamedFunction.formatOfLongest(problemMap.keySet().stream().toList())),
        attribute("evolver").reformat("%20.20s"),
        attribute("validator").reformat("%20.20s")
    );
    List<NamedFunction<? super ValidationEvent, ?>> validationFunctions = List.of(
        NamedFunction.build("event.fitness", "%5.3f", ValidationEvent::fitness),
        NamedFunction.build("validation.fitness", "%5.3f", ValidationEvent::validationFitness)
    );

    List<ListenerFactory<POSetPopulationState<?, ?, List<Double>>, Map<String, Object>>> listenerFactories =
        new ArrayList<>();
    if (bestFile == null || output) {
      listenerFactories.add(new TabularPrinter<>(functions, kFunctions));
    }
    if (bestFile != null) {
      listenerFactories.add(new CSVPrinter<>(functions, kFunctions, new File(bestFile)));
    }
    if (lastFile != null) {
      listenerFactories.add(new CSVPrinter<>(functions, kFunctions, new File(lastFile)).onLast());
    }
    ListenerFactory<POSetPopulationState<?, ?, List<Double>>, Map<String, Object>> listenerFactory =
        ListenerFactory.all(listenerFactories);
    ListenerFactory<ValidationEvent, Map<String, Object>>
        validationListenerFactory = validationFile == null ? ListenerFactory.deaf() :
        new CSVPrinter<>(validationFunctions, validationKFunctions, new File(validationFile));

    ProgressMonitor progressMonitor = new ScreenProgressMonitor(System.out);
    if (telegramBotId != null && telegramChatId != 0) {
      progressMonitor = progressMonitor.and(new TelegramProgressMonitor(telegramBotId, telegramChatId));
    }

    // evolvers
    Map<String, Function<DatasetClassificationProblem, IterativeSolver<? extends POSetPopulationState<?, Classifier<double[], Integer>, List<Double>>,
        TotalOrderQualityBasedProblem<Classifier<double[], Integer>, List<Double>>, Classifier<double[], Integer>>>> solvers = new TreeMap<>();

    solvers.put("es", p -> {
      int[] layers = new int[nHiddenLayers + 2];
      layers[0] = p.getNumberOfFeatures();
      layers[layers.length - 1] = p.getNumberOfClasses();
      IntStream.range(1, layers.length - 1).forEach(i -> layers[i] = layers[0]);
      Function<List<Double>, NeuralNetworkClassifier> classifierBuilder = list ->
          new NeuralNetworkClassifier(list.stream().mapToDouble(d -> d).toArray(), layers);
      int weightsSize = NeuralNetworkClassifier.countWeights(layers);

      return new SimpleEvolutionaryStrategy<>(
          classifierBuilder,
          new FixedLengthListFactory<>(weightsSize, new UniformDoubleFactory(-1, 1)),
          nPop,
          StopConditions.nOfFitnessEvaluations(nEvals),
          nPop / 4,
          1,
          0.35,
          false
      );
    });

    // validation
    Map<String, Function<TotallyOrderedClassificationProblem, Function<Classifier<double[], Integer>, Double>>> validators = new TreeMap<>();
    validators.put("default", p -> p.validationQualityFunction().andThen(l -> l.get(0)));

    L.info(String.format("Going to test with %d evolvers: %s%n", solvers.size(), solvers.keySet()));
    int nOfRuns = seeds.length * problemMap.size() * solvers.size();
    int counter = 0;
    // run
    for (int seed : seeds) {
      for (Map.Entry<String, TotallyOrderedClassificationProblem> problemEntry : problemMap.entrySet()) {
        for (Map.Entry<String, Function<DatasetClassificationProblem,
            IterativeSolver<? extends POSetPopulationState<?, Classifier<double[], Integer>, List<Double>>,
                TotalOrderQualityBasedProblem<Classifier<double[], Integer>, List<Double>>, Classifier<double[], Integer>>>> solverEntry : solvers.entrySet()) {
          Map<String, Object> keys = Map.ofEntries(
              Map.entry("seed", seed),
              Map.entry("problem", problemEntry.getKey()),
              Map.entry("evolver", solverEntry.getKey())
          );
          try {
            TotallyOrderedClassificationProblem problem = problemEntry.getValue();
            counter = counter + 1;
            Stopwatch stopwatch = Stopwatch.createStarted();
            progressMonitor.notify(
                ((float) counter - 1) / nOfRuns,
                String.format("(%d/%d); Starting %s", counter, nOfRuns, keys)
            );
            IterativeSolver<? extends POSetPopulationState<?, Classifier<double[], Integer>, List<Double>>,
                TotalOrderQualityBasedProblem<Classifier<double[], Integer>, List<Double>>, Classifier<double[], Integer>> solver =
                solverEntry.getValue().apply(problem);
            Collection<Classifier<double[], Integer>> solutions = solver.solve(
                problem,
                new Random(seed),
                executorService,
                listenerFactory.build(keys).deferred(executorService)
            );
            progressMonitor.notify((float) counter / nOfRuns, String.format(
                "(%d/%d); Done %s: %d solutions in %4ds",
                counter,
                nOfRuns,
                keys,
                solutions.size(),
                stopwatch.elapsed(TimeUnit.SECONDS)
            ));

            for (Map.Entry<String, Function<TotallyOrderedClassificationProblem,
                Function<Classifier<double[], Integer>, Double>>> validator : validators.entrySet()) {
              Map<String, Object> validationKeys = Map.ofEntries(
                  Map.entry("seed", seed),
                  Map.entry("problem", problemEntry.getKey()),
                  Map.entry("evolver", solverEntry.getKey()),
                  Map.entry("validator", validator.getKey())
              );
              Listener<ValidationEvent> validationListener = validationListenerFactory.build(validationKeys);
              Function<Classifier<double[], Integer>, Double> fitnessFunction = problem.qualityFunction().andThen(l -> l.get(0));
              Function<Classifier<double[], Integer>, Double> validationFunction = validator.getValue().apply(problem);
              solutions.stream().map(
                  s -> new ValidationEvent(
                      s, fitnessFunction.apply(s), validationFunction.apply(s))
              ).forEach(validationListener::listen);
              L.info(String.format(
                  "Validation done %s",
                  validationKeys
              ));

            }

          } catch (SolverException e) {
            L.severe(String.format("Cannot complete %s due to %s", keys, e));
            e.printStackTrace();
          }

        }
      }
    }
    listenerFactory.shutdown();

  }

  private Map<String, TotallyOrderedClassificationProblem> buildProblems(String filename, int nClasses, String yColumnName,
                                                                         int folds, ClassificationFitness.Metric metric) {
    Map<String, TotallyOrderedClassificationProblem> problems = new HashMap<>();
    for (int i = 0; i < folds; i++) {
      try {
        problems.put(
            String.format("%s~%d~%d", simplify(filename), folds, i),
            new TotallyOrderedClassificationProblem(filename, nClasses, yColumnName, folds, i, metric, metric)
        );
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return problems;
  }

  private static String simplify(String filename) {
    return filename.substring(filename.lastIndexOf("\\") + 1).replaceFirst("[.][^.]+$", "");
  }


}
