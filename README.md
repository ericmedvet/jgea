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
`Problem` is parametrized with two types that conceptually represent the solution space and the fitness space, i.e., the space of the quality of the solution:
```java
public interface Problem<S, F> extends Serializable {
  public NonDeterministicFunction<S, F> getFitnessFunction();
}
```
A `Problem` is hence defined by the solution space `S`, the fitness space `F` and a fitness function from `S` to `F`, that in JGEA can be non-deterministic.

JGEA includes many realization of this abstract definition, i.e., other interfaces that extends `Problem` and implementations (possibly `abstract`) of these interfaces.

For example, there is a class representing the OneMax problem:
```java
public class OneMax implements Problem<BitString, Double>, TunablePrecisionProblem<BitString, Double> { /* ... */ }
```
where the solution space is the one of `BitString`s and the fitness space is the one of `Double`s.

There is a `abstract` class representing, in general, a classification problem:
```java
public abstract class AbstractClassificationProblem<C, O, E extends Enum<E>> implements ProblemWithValidation<C, List<Double>>, BiFunction<C, O, E> { /* ... */ }
```
where `C` is the space of classifiers, `O` is the space of observations (or instances, or data points, in supervised learning jargon), `E` is the space of labels (or responses, or outputs).

## Evolutionary Algorithm (EA)
The EA is described by a class implementing the `Evolver` interface.
`Evolver` is parametrized with three types that potentially represent the genotype (`G`), solution (`S`), and fitness (`F`) spaces:
```java
public interface Evolver<G, S, F> extends Serializable { 
  public Collection<S> solve(
          Problem<S, F> problem,
          Random random,
          ExecutorService executor,
          Listener listener) throws InterruptedException, ExecutionException;  
}
```
In general, any `Evolver` can be used to solve a `Problem` with suitable `S`, `F`.
The genotype space `G` is the one in which the search is actually performed by applying the genetic operators: items in `G` are mapped to items in `S`, i.e., to solutions, with a mapper, that, when required, is one of the parameters of the EA, that are fields of the class implementing `Evolver` in JGEA.
Note that some EAs might support only the case in which `G` = `S`.

An `Evolver` solves a problem when the `solve()` method is invoked; that is, invoking `solve()` corresponds to performing an **evolutionary run** (also called evolutionary search or simply evolution).

Besides the `Problem` parameter, whose role is obvious, this methods takes as input also a `Random`, an `ExecutorService`, and a `Listener`.
The `Random` instance is used for all the random choices, hence allowing for repeatability of the experimentation.
**Note**: reproducibility, i.e., obtaining the same results in the same conditions, is not a direct consequence of repeatability if some conditions are not satisfied. JGEA attempts to meet all of these conditions: however, executing an evolutionary run with some parallelism leads in general to not meeting one condition.
The `ExecutorService` instance is used for distributing computation across different workers of the executor.
The `Listener` instance is used for notifying about important events during the evolution.
The return value of `solve()` is a collection of solutions: depending on the EA, this collection might be composed of a single solution, i.e., the solution found by the EA.

## Research papers based on JGEA
The list includes paper published from 2018 on.
- Medvet, Bartoli, De Lorenzo, Seriani, [Design, Validation, and Case Studies of 2D-VSR-Sim, an Optimization-friendly Simulator of 2-D Voxel-based Soft Robots](http://medvet.inginf.units.it/publications/designvalidationandcasestudiesofdvsrsimanoptimizationfriendlysimulatorofdvoxelbasedsoftrobots), arXiv, 2020
- Medvet, Seriani, Bartoli, Gallina, [Evolutionary Optimization of Sliding Contact Positions in Powered Floor Systems for Mobile Robots](http://medvet.inginf.units.it/publications/evolutionaryoptimizationofslidingcontactpositionsinpoweredfloorsystemsformobilerobots), at - Automatisierungstechnik, 2020
- Medvet, Bartoli, De Lorenzo, Fidel, [Evolution of Distributed Neural Controllers for Voxel-based Soft Robots](http://medvet.inginf.units.it/publications/evolutionofdistributedneuralcontrollersforvoxelbasedsoftrobots), ACM Genetic and Evolutionary Computation Conference (GECCO), 2020, Cancun (Mexico)
- Bartoli, De Lorenzo, Medvet, Squillero, [Multi-level Diversity Promotion Strategies for Grammar-guided Genetic Programming](http://medvet.inginf.units.it/publications/multileveldiversitypromotionstrategiesforgrammarguidedgeneticprogramming), Applied Soft Computing, 2019
- Medvet, Seriani, Bartoli, Gallina, [Design of Powered Floor Systems for Mobile Robots with Differential Evolution](http://medvet.inginf.units.it/publications/designofpoweredfloorsystemsformobilerobotswithdifferentialevolution), 22nd European Conference on the Applications of Evolutionary Computation (EvoApplication), 2019, Leipzig (Germany)
- Bartoli, Castelli, Medvet, [Weighted Hierarchical Grammatical Evolution](http://medvet.inginf.units.it/publications/weightedhierarchicalgrammaticalevolution), IEEE Transactions on Cybernetics, 2018
- Medvet, Virgolin, Castelli, Bosman, Gonçalves, Tušar, [Unveiling Evolutionary Algorithm Representation with DU Maps](http://medvet.inginf.units.it/publications/unveilingevolutionaryalgorithmrepresentationwithdumaps), Genetic Programming and Evolvable Machines, 2018
- Medvet, Bartoli, De Lorenzo, Tarlao, [Designing Automatically a Representation for Grammatical Evolution](http://medvet.inginf.units.it/publications/designingautomaticallyarepresentationforgrammaticalevolution), Genetic Programming and Evolvable Machines, 2018
- Medvet, Bartoli, De Lorenzo, Tarlao, [GOMGE: Gene-pool Optimal Mixing on Grammatical Evolution](http://medvet.inginf.units.it/publications/gomgegenepooloptimalmixingongrammaticalevolution), 15th International Conference on Parallel Problem Solving from Nature (PPSN), 2018, Coimbra (Portugal)
- Medvet, Bartoli, De Lorenzo, [Exploring the Application of GOMEA to Bit-string GE](http://medvet.inginf.units.it/publications/exploringtheapplicationofgomeatobitstringge), ACM Genetic and Evolutionary Computation Conference (GECCO), 2018, Kyoto (Japan)
- Medvet, Bartoli, [On the Automatic Design of a Representation for Grammar-based Genetic Programming](http://medvet.inginf.units.it/publications/ontheautomaticdesignofarepresentationforgrammarbasedgeneticprogramming), 21st European Conference on Genetic Programming (EuroGP), 2018, Parma (Italy)
- Medvet, Bartoli, Ansuini, Tarlao, [Observing the Population Dynamics in GE by means of the Intrinsic Dimension](http://medvet.inginf.units.it/publications/observingthepopulationdynamicsingebymeansoftheintrinsicdimension), Evolutionary Machine Learning workshop at International Conference on Parallel Problem Solving from Nature (EML@PPSN), 2018, Coimbra (Portugal)
