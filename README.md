# JGEA

Java General Evolutionary Algorithm (jgea) is a modular Java framework for experimenting with [Evolutionary Computation](https://en.wikipedia.org/wiki/Evolutionary_computation).

JGEA aims at providing a general interface to potentially *all* evolutionary algorithms (EA).
To do so, it provides an interface to problems that can be solved with an EA and components of an EA (e.g., genetic operators, selection criteria, ...).

Moreover, a few EAs are actually implemented in JGEA: Map-Elites, NSGA-II, OpenAI-ES, CMA-ES, Differential Evolution, Graphea, GA, GP, and some variants of Grammar-guided GP. 

Several research papers have been published in which the experimental evaluation is based on JGEA or its previous version [evolved-ge](https://github.com/ericmedvet/evolved-ge).
See a partial [list](#research-papers-based-on-jgea) below.

If you use JGEA _please cite_ [our paper](https://medvet.inginf.units.it/publications/2022-c-mnm-jgea/):

```
@inproceedings{medvet2022jgea,
  title={JGEA: a Modular Java Framework for Experimenting with Evolutionary Computation},
  author={Medvet, Eric and Nadizar, Giorgia and Manzoni, Luca},
  booktitle={Proceedings of the genetic and evolutionary computation conference companion},
  year={2022}
}
```

## Usage

### JGEA inside your Java project

Add (at least) this to your `pom.xml`:
```xml

<dependency>
    <groupId>io.github.ericmedvet</groupId>
    <artifactId>jgea.core</artifactId>
    <version>2.7.1-SNAPSHOT</version>
</dependency>
```

### Standalone (through the Experimenter)

First, get the code:
```shell
git clone https://github.com/ericmedvet/jgea.git
cd jgea
mvn clean package
```

Then run an example experiment
```shell
java -jar io.github.ericmedvet.jgea.experimenter/target/jgea.experimenter-2.7.1-SNAPSHOT-jar-with-dependencies.jar -e sr-comparison
```

## Main components

Typical usage of JGEA consists in trying to solve a **problem** using an **EA**.

### Problem

The problem is described by a class implementing the `Problem` interface. A problem simply defines the *solution space*, by using a generics `S`, and a way to compare two solutions, by extending the `PartialComparator<S>` interface.

```java
public interface Problem<S> extends PartialComparator<S> {}
```

Most problems actually define also a space for the quality of the solutions and a way to compute the quality given a solution, on the assumption that solutions are compared by comparing the fitness. This kind of problems is described by classes implementing the `QualityBasedProblem` interface.

```java
public interface QualityBasedProblem<S, Q> extends Problem<S> {
  PartialComparator<Q> qualityComparator();

  Function<S, Q> qualityFunction();
}
```

Here, `Q` represents the *quality space* (or *fitness space*).

JGEA includes many realization of this abstract definition, i.e., other interfaces that extend `QualityBasedProblem` and implementations (possibly `abstract`) of these interfaces.

For example, there is a class representing the OneMax problem:

```java
public class OneMax implements ComparableQualityBasedProblem<BitString, Double> {
  /* ... */
}
```

where the solution space is the one of `BitString`s and the quality space is the one of `Double`s.

There are other interfaces extending `QualityBasedProblem` that model more specific classes of problems. For example, there is a class representing, in general, a classification problem:

```java
public class ClassificationProblem<O, L extends Enum<L>> implements ProblemWithValidation<Classifier<O, L>,
    List<Double>> {
  /* ... */
}
```

where `O` is the space of observations (or instances, or data points, in supervised learning jargon), `L` is the space of labels (or responses, or outputs), and `Classifier<O, L>` is the space of solutions. Here, the quality space is given by `List<Double>` because we model the quality of a classifier with potentially more than one indexes (i.e., not necessarily only the accuracy).

### Solver

A problem can be solved by a `Solver`:

```java
public interface Solver<P extends Problem<S>, S> {
  Collection<S> solve(
      P problem, RandomGenerator random, ExecutorService executor
  ) throws SolverException;
}
```

The unique ability of a `Solver` is to solve a problem `P`. The return value of `solve()` is a collection of solutions: depending on the solver and on the problem, this collection might be composed of a single solution, i.e., the best solution, or of multiple solutions.

In general, an implementation of a solver might be able to solve only a subset of the possible problems, i.e., only problems of a given type, here representated by `P`, e.g., `QualityBasedProblem<S, Double>`.

The `solve()` method takes, besides the problem, a `RandomGenerator` and an `ExecutorService`, because a solver can be, in general, non-deterministic and capable of exploiting concurrency.

- The `RandomGenerator` instance is used for all the random choices, hence allowing for repeatability of the experimentation.
  **Note**: reproducibility, i.e., obtaining the same results in the same conditions, is not a direct consequence of repeatability if some conditions are not satisfied. JGEA attempts to meet all of these conditions: however, executing an evolutionary run with some parallelism leads in general to not meeting one condition.
- The `ExecutorService` instance is used for distributing computation (usually, of the fitness of candidate solutions) across different workers of the executor.

Most of the solvers are iterative, i.e., the build solutions based on an iterative process.

```java
public interface IterativeSolver<T extends Copyable, P extends Problem<S>, S> extends Solver<P, S> {
  /* ... */
  default Collection<S> solve(
      P problem, RandomGenerator random, ExecutorService executor, Listener<? super T> listener
  ) throws SolverException {
    /* ... */
  }
}
```

Intuitively, an `IterativeSolver` evolves a state `T` across iterations starting from an initial value. In the practical case of **evolutionary algorithms** (EAs) the state usually contains also the *population of individuals*. The trajectory of the state during the solution of problems can be monitored by a `Listener`, that has to be passed to `solve()`.

#### Listeners

Listeners are a key component of JGEA. Their main use is to monitor the evolution by extracting some information and printing it somewhere. Typical information of interest is, at each iteration of the EA:

- the size of the population
- the diversity in the population
- the quality (a `Q`) of the best individual
- the best individual (a `S`)
- some function of the best individual

In the example below, it is shown how to use listener to print on the standard output how this kind of information changes during the evolution. JGEA contains classes for printing on stdout as well as on files, with proper formats.

#### Implemented EAs

JGEA contains a few significative EAs, i.e., classes implementing `IterativeSolver`.

One, that is at the same time pretty standard and a template that can be realized in many ways depending on the parameters, is `StanderdEvolver`, that corresponds to a *&mu; + &lambda;* (or *&mu;, &lambda;*, depending on the parameter `overlapping`) *generational model* (see [[1]](#references)).
`StandardEvolver` parameters are set using the only class constructor: names of the parameters indicate the corresponding meaning.

```java
public class StandardEvolver<G, S, Q>
    extends AbstractStandardEvolver<
    POCPopulationState<Individual<G, S, Q>, G, S, Q>,
    QualityBasedProblem<S, Q>,
    Individual<G, S, Q>,
    G,
    S,
    Q> {
  public StandardEvolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      int populationSize,
      Predicate<? super POCPopulationState<Individual<G, S, Q>, G, S, Q>> stopCondition,
      Map<GeneticOperator<G>, Double> operators,
      Selector<? super Individual<G, S, Q>> parentSelector,
      Selector<? super Individual<G, S, Q>> unsurvivalSelector,
      int offspringSize,
      boolean overlapping,
      boolean remap) {
    /* ... */
  }
}
```

`StandardEvolver` automatically exploits parallelism using the `ExecutorService` parameter of `solve()`.

`POCPopulationState` is a state that includes the population, in the form of a partially ordered set ([poset](https://en.wikipedia.org/wiki/Partially_ordered_set)).

```java
public interface POCPopulationState<I extends Individual<G, S, Q>, G, S, Q> extends State {
  long nOfBirths();
  long nOfFitnessEvaluations();
  PartiallyOrderedCollection<I> pocPopulation();
}
```

`Selector` represents a selection criterion: it is a functional interface with a method `select()` that takes a poset and returns, possibly stochastically, an element.

```java
public interface Selector<T> {
  <K extends T> K select(POCPopulationState<K> ks, RandomGenerator random);
}

```

JGEA uses `PartialComparator` and `PartiallyOrderedCollection` instead of the standard JDK interfaces `Comparator` and `Collection` because the latter represent a total ordering, whereas there are many cases where the EA does not assume a total ordering among candidate solutions.

## Example

In this example, JGEA is used for solving the *parity problem* with the standard EA and solution encoded as derivation trees of a provided grammar, that is, with a form of G3P. Here a solution is a `List<Tree<Element>>`, because the general form of a bits-to-bits set function is list of trees, each tree encoding a bits-to-bit function.

```java
public class Example {
  public static void main(String[] args) {
    ExecutorService executor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
    @SuppressWarnings({"unchecked", "rawtypes"})
    ListenerFactory<POCPopulationState<?, ?, NamedUnivariateRealFunction, Double>, Void>
        listenerFactory = new TabularPrinter<>(
        (List) List.of(
            nOfIterations(),
            elapsedSeconds(),
            nOfBirths(),
            all().then(size()),
            firsts().then(size()),
            lasts().then(size()),
            all().then(each(genotype())).then(uniqueness()),
            all().then(each(solution())).then(uniqueness()),
            all().then(each(quality())).then(uniqueness()),
            best().then(genotype()).then(size()),
            best().then(solution()).then(size()),
            best().then(fitnessMappingIteration()),
            NamedFunctions.<Individual<Object, Object, Double>, Object, Object, Double>best()
                .then(quality())
                .reformat("%5.3f"),
            NamedFunctions.<Individual<Object, Object, Double>, Object, Object, Double>all()
                .then(each(quality()))
                .then(hist(8)),
            best().then(solution()).reformat("%20.20s")
        ),
        List.of()
    );
    Random r = new Random(1);
    SyntheticUnivariateRegressionProblem p = new Nguyen7(UnivariateRegressionFitness.Metric.MSE, 1);
    List<Element.Variable> variables = p.qualityFunction()
        .getDataset()
        .xVarNames()
        .stream()
        .map(Element.Variable::new)
        .toList();
    List<Element.Constant> constantElements = Stream.of(0.1, 1d, 10d).map(Element.Constant::new).toList();
    IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
        IndependentFactory.picker(variables), IndependentFactory.picker(constantElements));
    IndependentFactory<Element> nonTerminalFactory = IndependentFactory.picker(List.of(
        Element.Operator.ADDITION,
        Element.Operator.SUBTRACTION,
        Element.Operator.MULTIPLICATION,
        Element.Operator.PROT_DIVISION
    ));
    TreeBuilder<Element> treeBuilder = new GrowTreeBuilder<>(x -> 2, nonTerminalFactory, terminalFactory);
    // operators
    Map<GeneticOperator<Tree<Element>>, Double> geneticOperators = Map.ofEntries(
        Map.entry(new SubtreeCrossover<>(10), 0.80),
        Map.entry(new SubtreeMutation<>(10, treeBuilder), 0.20)
    );
    StandardEvolver<Tree<Element>, NamedUnivariateRealFunction, Double> solver = new StandardEvolver<>(
        t -> new TreeBasedUnivariateRealFunction(
            t,
            p.qualityFunction().getDataset().xVarNames(),
            p.qualityFunction().getDataset().yVarNames().get(0)
        ),
        new RampedHalfAndHalf<>(4, 10, x -> 2, nonTerminalFactory, terminalFactory),
        100,
        StopConditions.nOfFitnessEvaluations(10000),
        geneticOperators,
        new Tournament(5),
        new Last(),
        100,
        true,
        false
    );
    Collection<NamedUnivariateRealFunction> solutions = solver.solve(p, r, executor, listenerFactory.build(null));
    System.out.printf("Found %d solutions%n", solutions.size());
  }
}
```

Methods inside the constructor of `TabularPrinter` are static methods of the class `NamedFunctions` that return functions that take an evolution `State` and return an object that will be printed as a table cell.

## Research papers based on JGEA

The list includes paper published from 2018 on.

- Nadizar, Medvet, Nichele, Pontes-Filho; [An Experimental Comparison of Evolved Neural Network Models for Controlling Simulated Modular Soft Robots](https://medvet.inginf.units.it/publications/2023-j-nmnp-experimental/); Applied Soft Computing (ASOC), Q1; 2023
- Medvet, Nadizar; [GP for Continuous Control: Teacher or Learner? The Case of Simulated Modular Soft Robots](https://medvet.inginf.units.it/publications/2023-c-mn-gp/); XX Genetic Programming Theory & Practice (GPTP); 2023
- Nadizar, Medvet, Walker, Risi; [Neural Cellular Automata Enable Self-Discovery of Physical Configuration in Modular Robots Driven by Collective Intelligence](https://medvet.inginf.units.it/publications/2023-c-nmwr-neural/); The Distributed Ghost Workshop (DistributedGhost@Alife); 2023
- Medvet, Pozzi, Manzoni; [A General Purpose Representation and Adaptive EA for Evolving Graphs](https://medvet.inginf.units.it/publications/2023-c-mpm-general/); ACM Genetic and Evolutionary Computation Conference (GECCO); 2023
- Nadizar, Medvet, Walker, Risi; [A Fully-distributed Shape-aware Neural Controller for Modular Robots](https://medvet.inginf.units.it/publications/2023-c-nmwr-fully/); ACM Genetic and Evolutionary Computation Conference (GECCO); 2023
- Pigozzi, Medvet, Bartoli, Rochelli; [Factors Impacting Diversity and Effectiveness of Evolved Modular Robots](https://medvet.inginf.units.it/publications/2023-j-pmbr-factors/); ACM Transactions on Evolutionary Learning and Optimization (TELO); 2023
- Nadizar, Medvet; [On the Effects of Collaborators Selection and Aggregation in Cooperative Coevolution: an Experimental Analysis](https://medvet.inginf.units.it/publications/2023-c-nm-effects/); 26th European Conference on Genetic Programming (EuroGP); 2023
- Ferigo, Iacca, Medvet, Pigozzi; [Evolving Hebbian Learning Rules in Voxel-based Soft Robot](https://medvet.inginf.units.it/publications/2022-j-fimp-evolving/); IEEE Transactions on Cognitive and Developmental Systems (TCDS); 2022
- Medvet, Rusin; [Impact of Morphology Variations on Evolved Neural Controllers for Modular Robots](https://medvet.inginf.units.it/publications/2022-c-mr-impact/); XVI International Workshop on Artificial Life and Evolutionary Computation (WIVACE); 2022
- Medvet, Nadizar, Manzoni; [JGEA: a Modular Java Framework for Experimenting with Evolutionary Computation](https://medvet.inginf.units.it/publications/2022-c-mnm-jgea/); Workshop Evolutionary Computation Software Systems (EvoSoft@GECCO); 2022
- Medvet, Nadizar, Pigozzi; [On the Impact of Body Material Properties on Neuroevolution for Embodied Agents: the Case of Voxel-based Soft Robots](https://medvet.inginf.units.it/publications/2022-c-mnp-impact/); Workshop on Neuroevolution at Work (NEWK@GECCO); 2022
- Ferigo, Soros, Medvet, Iacca; [On the Entanglement between Evolvability and Fitness: an Experimental Study on Voxel-based Soft Robots](https://medvet.inginf.units.it/publications/2022-c-fsmi-entanglement/); Annual Conference on Artificial Life (Alife); 2022
- Nadizar, Medvet, Nichele, Pontes-Filho; [Collective control of modular soft robots via embodied Spiking Neural Cellular Automata](https://medvet.inginf.units.it/publications/2022-c-nmnp-collective/); Workshop on From Cells to Societies: Collective Learning across Scales (Cells2Societies@ICLR); 2022
- Pigozzi, Tang, Medvet, Ha; [Evolving Modular Soft Robots without Explicit Inter-Module Communication using Local Self-Attention](https://medvet.inginf.units.it/publications/2022-c-ptmh-evolving/); ACM Genetic and Evolutionary Computation Conference (GECCO); 2022
- Pigozzi, Medvet; [Evolving Modularity in Soft Robots through an Embodied and Self-Organizing Neural Controller](https://medvet.inginf.units.it/publications/2022-j-pm-evolving/); Artificial Life, Q3; 2022
- Nadizar, Medvet, Miras; [On the Schedule for Morphological Development of Evolved Modular Soft Robots](https://medvet.inginf.units.it/publications/2022-c-nmm-schedule/); 25th European Conference on Genetic Programming (EuroGP); 2022
- Indri, Bartoli, Medvet, Nenzi; [One-Shot Learning of Ensembles of Temporal Logic Formulas for Anomaly Detection in Cyber-Physical Systems](https://medvet.inginf.units.it/publications/2022-c-ibmn-one/); 25th European Conference on Genetic Programming (EuroGP); 2022
- Ferigo, Iacca, Medvet, Pigozzi; [Evolving Hebbian Learning Rules in Voxel-based Soft Robots](https://medvet.inginf.units.it/publications/2021-p-fimp-evolving/); IEEE TechRxiv; 2021
- Ferigo, Medvet, Iacca; [Optimizing the Sensory Apparatus of Voxel-based Soft Robots through Evolution and Babbling](https://medvet.inginf.units.it/publications/2021-j-fmi-optimizing/); Springer Nature Computer Science; 2021
- Nadizar, Medvet, Nichele, Huse Ramstad, Pellegrino, Zullich; [Merging Pruning and Neuroevolution: towards Robust and Efficient Controllers for Modular Soft Robots](https://medvet.inginf.units.it/publications/2021-j-nmnhpz-merging/); Knowledge Engineering Review (KER); 2021
- Pigozzi, Medvet, Nenzi; [Mining Road Traffic Rules with Signal Temporal Logic and Grammar-based Genetic Programming](https://medvet.inginf.units.it/publications/2021-j-pmn-mining/); Applied Sciences; 2021
- Nadizar, Medvet, Pellegrino, Zullich, Nichele; [On the Effects of Pruning on Evolved Neural Controllers for Soft Robots](https://medvet.inginf.units.it/publications/2021-c-nmpzn-effects/); Workshop on Neuroevolution at Work (NEWK@GECCO); 2021
- Talamini, Medvet, Nichele; [Criticality-driven Evolution of Adaptable Morphologies of Voxel-Based Soft-Robots](https://medvet.inginf.units.it/publications/2021-j-tmn-criticality/); Frontiers in Robotics and AI; 2021
- Medvet, Bartoli, Pigozzi, Rochelli; [Biodiversity in Evolved Voxel-based Soft Robots](https://medvet.inginf.units.it/publications/2021-c-mbpr-biodiversity/); ACM Genetic and Evolutionary Computation Conference (GECCO); 2021
- Ferigo, Iacca, Medvet, [Beyond Body Shape and Brain: Evolving the Sensory Apparatus of Voxel-based Soft Robots](https://medvet.inginf.units.it/publications/2021-c-fim-beyond/); 24th European Conference on the Applications of Evolutionary Computation (EvoAPPS); 2021
- Medvet, Bartoli; [Evolutionary Optimization of Graphs with GraphEA](https://medvet.inginf.units.it/publications/2020-c-mb-evolutionary/); 19th International Conference of the Italian Association for Artificial Intelligence (AIxIA); 2020
- Medvet, Bartoli, [GraphEA: a Versatile Representation and Evolutionary Algorithm for Graphs](https://medvet.inginf.units.it/publications/2020-c-mb-graphea/), Workshop on Evolutionary and Population-based Optimization (WEPO@AIxIA), 2020
- Medvet, Bartoli, De Lorenzo, Seriani, [Design, Validation, and Case Studies of 2D-VSR-Sim, an Optimization-friendly Simulator of 2-D Voxel-based Soft Robots](https://medvet.inginf.units.it/publications/2020-p-mbds-design/), arXiv, 2020
- Medvet, Seriani, Bartoli, Gallina, [Evolutionary Optimization of Sliding Contact Positions in Powered Floor Systems for Mobile Robots](https://medvet.inginf.units.it/publications/2020-j-msbg-evolutionary/), at - Automatisierungstechnik, 2020
- Medvet, Bartoli, De Lorenzo, Fidel, [Evolution of Distributed Neural Controllers for Voxel-based Soft Robots](https://medvet.inginf.units.it/publications/2020-c-mbdf-evolution/), ACM Genetic and Evolutionary Computation Conference (GECCO), 2020, Cancun (Mexico)
- Bartoli, De Lorenzo, Medvet, Squillero, [Multi-level Diversity Promotion Strategies for Grammar-guided Genetic Programming](https://medvet.inginf.units.it/publications/2019-j-bdms-multi/), Applied Soft Computing, 2019
- Medvet, Seriani, Bartoli, Gallina, [Design of Powered Floor Systems for Mobile Robots with Differential Evolution](https://medvet.inginf.units.it/publications/2019-c-msbg-design/), 22nd European Conference on the Applications of Evolutionary Computation (EvoApplication), 2019, Leipzig (Germany)
- Bartoli, Castelli, Medvet, [Weighted Hierarchical Grammatical Evolution](https://medvet.inginf.units.it/publications/2018-j-bcm-weighted/), IEEE Transactions on Cybernetics, 2018
- Medvet, Virgolin, Castelli, Bosman, Gonçalves, Tušar, [Unveiling Evolutionary Algorithm Representation with DU Maps](https://medvet.inginf.units.it/publications/2018-j-mvcbgt-unveiling/), Genetic Programming and Evolvable Machines, 2018
- Medvet, Bartoli, De Lorenzo, Tarlao, [Designing Automatically a Representation for Grammatical Evolution](https://medvet.inginf.units.it/publications/2018-j-mbdt-designing/), Genetic Programming and Evolvable Machines, 2018
- Medvet, Bartoli, De Lorenzo, Tarlao, [GOMGE: Gene-pool Optimal Mixing on Grammatical Evolution](https://medvet.inginf.units.it/publications/2018-c-mbdt-gomge/), 15th International Conference on Parallel Problem Solving from Nature (PPSN), 2018, Coimbra (Portugal)
- Medvet, Bartoli, De Lorenzo, [Exploring the Application of GOMEA to Bit-string GE](https://medvet.inginf.units.it/publications/2018-c-mbd-exploring/), ACM Genetic and Evolutionary Computation Conference (GECCO), 2018, Kyoto (Japan)
- Medvet, Bartoli, [On the Automatic Design of a Representation for Grammar-based Genetic Programming](https://medvet.inginf.units.it/publications/2018-c-mb-automatic/), 21st European Conference on Genetic Programming (EuroGP), 2018, Parma (Italy)
- Medvet, Bartoli, Ansuini, Tarlao, [Observing the Population Dynamics in GE by means of the Intrinsic Dimension](https://medvet.inginf.units.it/publications/2018-c-mbat-observing/), Evolutionary Machine Learning workshop at International Conference on Parallel Problem Solving from Nature (EML@PPSN), 2018, Coimbra (Portugal)

## References

1. De Jong, Kenneth. "Evolutionary computation: a unified approach." Proceedings of the 2016 on Genetic and Evolutionary Computation Conference Companion. 2016.
