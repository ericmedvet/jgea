# JGEA

Java General Evolutionary Algorithm (jgea) is a modular Java framework for experimenting with [Evolutionary Computation](https://en.wikipedia.org/wiki/Evolutionary_computation).

JGEA aims at providing a general interface to potentially *all* evolutionary algorithms (EA).
To do so, it provides an interface to problems that can be solved with an EA and components of an EA (e.g., genetic operators, selection criteria, ...).

Moreover, a few EA are actually implemented in JGEA, most of them being related to Genetic Programming (GP) and its Grammar-guided variants (G3P).

Several research papers have been published in which the experimental evaluation is based on JGEA or its previous version [evolved-ge](https://github.com/ericmedvet/evolved-ge).
See a partial [list](#research-papers-based-on-jgea) below

## Main components

Typical usage of JGEA consists in try to solve a **problem** using an **EA**.

### Problem

The problem is described by a class implementing the `Problem` interface.
`Problem` is parametrized with two types that conceptually represent the solution space and the fitness space, i.e., the space of the quality of the solutions:
```java
public interface Problem<S, F> {
  Function<S, F> getFitnessFunction();
}
```
A `Problem` is hence defined by the solution space `S`, the fitness space `F` and a fitness function from `S` to `F`.

JGEA includes many realization of this abstract definition, i.e., other interfaces that extends `Problem` and implementations (possibly `abstract`) of these interfaces.

For example, there is a class representing the OneMax problem:
```java
public class OneMax implements Problem<BitString, Double> { /* ... */ }
```
where the solution space is the one of `BitString`s and the fitness space is the one of `Double`s.

There are other interfaces extending `Problem` that model more specific classes of problems.
For example, there is an `abstract` class representing, in general, a classification problem:
```java
public abstract class AbstractClassificationProblem<C, O, E extends Enum<E>> implements ProblemWithValidation<C, List<Double>>, BiFunction<C, O, E> { /* ... */ }
```
where `C` is the space of classifiers, `O` is the space of observations (or instances, or data points, in supervised learning jargon), `E` is the space of labels (or responses, or outputs).

### Evolutionary Algorithm (EA)

The EA is described by a class implementing the `Evolver` interface.
`Evolver` is parametrized with three types that potentially represent the genotype (`G`), solution (`S`), and fitness (`F`) spaces:
```java

public interface Evolver<G, S, F> {
  Collection<S> solve(
      Function<S, F> fitnessFunction,
      Predicate<? super Event<G, S, F>> stopCondition,
      Random random,
      ExecutorService executor,
      Listener<? super Event<G, S, F>> listener) throws InterruptedException, ExecutionException;
}
```
In general, any `Evolver` can be used to solve any `Problem` with suitable `S`, `F` by invoking `solve()` on the problem fitness function.
The genotype space `G` is the one in which the search is actually performed by applying the genetic operators: items in `G` are mapped to items in `S`, i.e., to solutions, with a mapper, that, when required, is one of the parameters of the EA, that are fields of the class implementing `Evolver` in JGEA.
Note that some EAs might support only the case in which `G` = `S`; other my constraint `G` or `S` or both to be a given type (e.g., `List<Double>`).

An `Evolver` solves a problem when the `solve()` method is invoked; that is, invoking `solve()` corresponds to performing an **evolutionary run** (also called evolutionary search or simply evolution).

Besides the `Problem` parameter, whose role is obvious, this methods takes as input also a `Predicate`, a `Random`, an `ExecutorService`, and a `Listener`.
- The `Predicate` instance represents a termination criterion (possibly the conjuction or disjunction of other predicates).
Many EAs are iterative and support, in general, various termination conditions.
However, some EAs might not use, or need, a termination criterion. 
- The `Random` instance is used for all the random choices, hence allowing for repeatability of the experimentation.
**Note**: reproducibility, i.e., obtaining the same results in the same conditions, is not a direct consequence of repeatability if some conditions are not satisfied. JGEA attempts to meet all of these conditions: however, executing an evolutionary run with some parallelism leads in general to not meeting one condition.
- The `ExecutorService` instance is used for distributing computation (usually, of the fitness of candidate solutions) across different workers of the executor.
- The `Listener` instance is used for notifying about important events during the evolution.

The return value of `solve()` is a collection of solutions: depending on the EA and on the problem, this collection might be composed of a single solution, i.e., the best solution found by the EA, or a more than one solutions.

#### Listeners

Listeners are a key component of JGEA.
They main use is to monitor the evolution by extracting some information and printing it somewhere.
Typical information of interest is, at each iteration of the EA:
- the size of the population
- the diversity in the population
- the fitness (a `F`) of the best individual
- the best individual (a `S`)
- some function of the best individual

In the example below, it is shown how to use listener to print on the standard output how this kind of information changes during the evolution.
JGEA contains classes for printing on stdout as well as on files, with proper formats.

#### Implemented EAs

JGEA contains a few significatives EAs, i.e., classes implementing `Evolver`.

One, that is at the same time pretty standard and a template that can be realized in many ways depending on the parameters, is `StanderdEvolver`, that corresponds to a *mu + lamda* (or *mu, lambda*, depending on the parameter `overlapping`) *generational model* (see [[1]](#references)).
`StandardEvolver` parameters are set using the only class constructor: names of the parameters indicate the corresponding meaning.
```java
public class StandardEvolver<G, S, F> extends AbstractIterativeEvolver<G, S, F> {
  public StandardEvolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      PartialComparator<? super Individual<G, S, F>> individualComparator,
      int populationSize,
      Map<GeneticOperator<G>, Double> operators,
      Selector<? super Individual<? super G, ? super S, ? super F>> parentSelector,
      Selector<? super Individual<? super G, ? super S, ? super F>> unsurvivalSelector,
      int offspringSize,
      boolean overlapping,
      boolean remap) { /* ... */}
}
```
`StandardEvolver` automatically exploits parallelism using the `ExecutorService` parameter of `solve()`.

`Selector` represents a selection criterion: it is a functional interface with a method `select()` that takes a partially ordered set ([poset](https://en.wikipedia.org/wiki/Partially_ordered_set)) and returns, possibly stochastically, an element.
```java
public interface Selector<T> {  
  <K extends T> K select(PartiallyOrderedCollection<K> ks, Random random);  
}
```
JGEA uses `PartialComparator` and `PartiallyOrderedCollection` instead of the standard JDK interfaces `Comparator` and `Collection` because the latter represent a total ordering, whereas there are many cases where the EA does not assume a total ordering among candidate solutions.
Note that the `PartialComparator` is a parameter of an EA, rather than part of the definition of a `Problem` because different ranking criteria can be used on individuals rather than just quality.
For example, one may want to solve a *symbolic regression* problem by ranking solutions according to Pareto dominance based on mean absolute error *and* complexity of the expression, both to be minimized.

## Example

In this example, JGEA is used for solving the *parity problem* with the standard EA and solution encoded as derivation trees of a provided grammar, that is, with a form of G3P.
Here a solution is a `List<Tree<Element>>`, because the general form of a bits-to-bits set function is list of trees, each tree encoding a bits-to-bit function.
```java
public class Example {
  public static void main(String[] args) {
    ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    Random r = new Random(1);
    GrammarBasedProblem<String, List<Tree<Element>>, Double> p = new EvenParity(8);
    Evolver<Tree<String>, List<Tree<Element>>, Double> evolver = new StandardEvolver<>(
        new FormulaMapper(),
        new RampedHalfAndHalf<>(3, 12, p.getGrammar()),
        PartialComparator.from(Double.class).on(Individual::getFitness),
        100,
        Map.of(
            new StandardTreeCrossover<>(12), 0.8d,
            new StandardTreeMutation<>(12, p.getGrammar()), 0.2d
        ),
        new Tournament(3),
        new Worst(),
        100,
        true,
        false
    );
    Listener.Factory<Event<?, ?, ? extends Double>> listenerFactory = new TabularPrinter<>(List.of(
        iterations(),
        births(),
        elapsedSeconds(),
        uniqueness().of(each(genotype())).of(all()),
        size().of(solution()).of(best()),
        birthIteration().of(best()),
        fitness().reformat("%5.3f").of(best()),
        hist(8).of(each(fitness())).of(all())
    ));
    Collection<List<Tree<Element>>> solutions = evolver.solve(
        Misc.cached(p.getFitnessFunction(), 10000),
        new Iterations(100),
        r,
        executorService,
        listenerFactory.build().deferred(executorService)
    );
    System.out.printf("Found %d solutions with %s.%n", solutions.size(), evolver.getClass().getSimpleName());
    listenerFactory.shutdown();
  }
}
```
Methods inside the constructor of `TabularPrinter` are static methods of the class `NamedFunctions` that returns functions that take an evolution `Event` and returns an object that will be printed as a table cell.

## Research papers based on JGEA
The list includes paper published from 2018 on.
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
