---
layout: default
title: Structure and components
nav_order: 3
has_children: true
permalink: structure
---

# Structure and components
EAs are a class of population-based optimization algorithms.
As such, given a _problem_, usually an optimization problem, an EA acts as a _solver_, whose goal is to find one or more solutions to the problem, possibly the best ones.
To capture these high level concepts in JGEA, we translate them into two core interfaces: ``Problem`` and ``Solver``.
Starting from this dichotomy, we render more specific instances by extending and implementing the interfaces to suit different case studies.
