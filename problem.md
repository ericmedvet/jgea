---
layout: default
title: Problem
parent: Structure and components
nav_order: 1
---

# Problem
Any problem can be described by a class implementing the ``Problem`` interface, that defines the solution space ``S`` and a way of comparing two solutions,
by extending the ``PartialComparator<S>``.
```java
public interface Problem<S> extends PartialComparator<S> {}
```

In most cases, solutions are actually compared according to their _quality_, i.e., based on their fitness value.
We model this in the ``QualityBasedProblem`` interface, which adds two functionalities to the ``Problem`` interface: a function deputy to mapping a 
solution to a quality value, and a way of comparing qualities.
```java
public interface QualityBasedProblem<S, Q> extends Problem<S> {
  Function<S, Q> qualityFunction();
  PartialComparator<Q> qualityComparator();
}
```
Here, ``Q`` represents the _quality-_ or _fitness-space_.

We remark that ``qualityComparator()`` returns a ``PartialComparator<Q>`` and not a ``Comparator<Q>``, as we do not necessarily want to enforce 
total ordering between qualities of solutions.
For those cases in which we do want to enforce this, we extend ``QualityBasedProblem`` to a ``TotalOrderQualityBasedProblem``.
This interface adds a ``totalOrderComparator()``, and provides a default implementation for the ``qualityComparator()``, where a ``PartialComparator``
is obtained from the ``Comparator``.
```java
public interface TotalOrderQualityBasedProblem<S, Q> extends QualityBasedProblem<S, Q> {
  Comparator<Q> totalOrderComparator();
  @Override
  default PartialComparator<Q> qualityComparator() { /*...*/ }
}
```

In addition, since oftentimes the quality of a solution is "naturally comparable", i.e., ``Q extends Comparable``, 
we model this extending ``TotalOrderQualityBasedProblem`` with a ``ComparableQualityBasedProblem``. 
```java
public interface ComparableQualityBasedProblem<S, Q extends Comparable<Q>> extends TotalOrderQualityBasedProblem<S, Q> {
  @Override
  default Comparator<Q> totalOrderComparator() {
    return Comparable::compareTo;
  }
}
```

To model more specific classes of problems, it is sufficient to add interfaces extending or classes implementing ``Problem`` 
(or one of its subinterfaces).
Among them, we have already included classification problems, symbolic regression problems (including many synthetic functions recommended 
as benchmarks), multi-objective problems, and various benchmarks as the Ackley function, 
the Rastrigin function, or the K landscapes for GP.
