---
layout: default
title: Individual
parent: Structure and components
nav_order: 3
---

# Individual
{: .no_toc }
We use the notion of _individual_, modeled in the ``Individual`` record, to capture the genotype-phenotype representation.
To this extent, we employ two generics parameters, ``G`` and ``S``, to define the genotype and the phenotype spaces, respectively.
In addition, since we also store the quality (or fitness) of the solution, i.e., of the phenotype encoded by the genotype, within the individual, we also add a generics parameter ``Q``.
```java
public record Individual<G, S, Q>(
    G genotype,
    S solution,
    Q fitness,
    long fitnessMappingIteration,
    long genotypeBirthIteration
)
```

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Creation

To create an instance of ``Individual``, we need to 
1. obtain a genotype,
2. map it to the corresponding phenotype, and
3. evaluate the fitness of the candidate solution.

Note that an ``Individual`` also stores the iteration at which the fitness is evaluated (``fitnessMappingIteration``), and the iteration at which the genotype is obtained (``genotypeBirthIteration``): these values model the "evolutionary age" for the individual in the evolutionary optimization run it belongs to.

A genotype can either be created from scratch or it can be the result of the application of genetic operators on pre-existing genotypes.
In the first case, we employ the factory design pattern to _build_ random genotypes.
```java
@FunctionalInterface
public interface Factory<T> {
  List<T> build(int n, RandomGenerator random);
}
```
We provide JGEA with some default implementations of factories for the most common genotypes, such as numeric genotypes, bit-strings, or trees.

Concerning genetic operators, we translate the concept into a general interface, extended by two more specific ones, accounting for the mutation and crossover operators.
```java
public interface GeneticOperator<G> {
  List<? extends G> apply(
    List<? extends G> parents,
    RandomGenerator random
  );
  int arity();
}
```
We remark that both methods deputy to computing the new genotype, ``build()`` and ``apply()``, take an instance of ``RandomGenerator`` to ensure reproducibility.

After obtaining a genotype, we map it to the corresponding phenotype with a simple ``Function<? super G, ? extends S>``, which can easily be defined on-the-fly, using Java lambda expressions.
Last, we compute the fitness of the solution by invoking a ``Function<? super S, ? extends Q>``, such as the ``qualityFunction()`` of a ``QualityBasedProblem``.


## Selection

Several EAs require to select individuals for reproduction or survival.
To model the selection process in JGEA we resort to the ``Selector`` interface.
```java
@FunctionalInterface
public interface Selector<T> {
  <K extends T> K select(
    PartiallyOrderedCollection<K> ks,
    RandomGenerator random
  );
}
```
We provide a few concrete selector implementations, such as the ``Tournament``, replicating the tournament selection, or the ``First`` and ``Last``, returning the best or worst individual (or a random one among them, in case of fitness ties).
Again, we remark the importance of passing an instance of ``RandomGenerator`` to the ``select()`` method for reproducibility concerns.
