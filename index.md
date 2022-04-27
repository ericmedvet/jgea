---
layout: default
title: Overview
nav_order: 1
has_children: false
permalink: index
---

# Overview
{: .no_toc }

We present the Java General Evolutionary Algorithm (JGEA) framework, a modular Java framework for experimenting with Evolutionary Computation (EC).
We designed JGEA to be 
1. aimed at providing a general interface to potentially all Evolutionary Algorithms (EAs), yet
2. solid and easy to use for people who rely on EC as a tool. 

To this extent, we developed JGEA including a range of ready-to-use EAs, backed by a modular architecture, comprising diverse layers of abstraction, which simplifies the implementation of new EAs and the addition of new features.
Here, we detail the general structure of JGEA, focusing on its high-level components, and present the use case of the introduction of a new EA in the framework.
To complete the picture, we illustrate the application of JGEA for solving a real world problem, from its formal definition in the framework to the saving and processing of results.
The source code of JGEA is available at [https://github.com/ericmedvet/jgea](https://github.com/ericmedvet/jgea).

If you use JGEA please cite our paper:
```
@inproceedings{medvet2022jgea,
  title={JGEA: a Modular Java Framework for Experimenting with Evolutionary Computation},
  author={Medvet, Eric and Nadizar, Giorgia and Manzoni, Luca},
  booktitle={Proceedings of the genetic and evolutionary computation conference companion},
  year={2022}
}
```
