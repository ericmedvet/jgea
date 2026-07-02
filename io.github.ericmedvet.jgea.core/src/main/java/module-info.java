/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
module io.github.ericmedvet.jgea.core {
  requires java.logging;
  requires io.github.ericmedvet.jsdynsym.core;
  requires io.github.ericmedvet.jnb.datastructure;
  requires commons.math3;
  requires io.github.ericmedvet.jviz.core;
  requires jcodec;
  requires org.jspecify;

  exports io.github.ericmedvet.jgea.core;
  exports io.github.ericmedvet.jgea.core.distance;
  exports io.github.ericmedvet.jgea.core.fitness;
  exports io.github.ericmedvet.jgea.core.operator;
  exports io.github.ericmedvet.jgea.core.order;
  exports io.github.ericmedvet.jgea.core.representation.grammar;
  exports io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp;
  exports io.github.ericmedvet.jgea.core.representation.grammar.string.ge;
  exports io.github.ericmedvet.jgea.core.representation.graph;
  exports io.github.ericmedvet.jgea.core.representation.graph.finiteautomata;
  exports io.github.ericmedvet.jgea.core.representation.graph.numeric;
  exports io.github.ericmedvet.jgea.core.representation.graph.numeric.functiongraph;
  exports io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph;
  exports io.github.ericmedvet.jgea.core.representation.sequence;
  exports io.github.ericmedvet.jgea.core.representation.sequence.bit;
  exports io.github.ericmedvet.jgea.core.representation.sequence.numeric;
  exports io.github.ericmedvet.jgea.core.representation.tree;
  exports io.github.ericmedvet.jgea.core.representation.tree.numeric;
  exports io.github.ericmedvet.jgea.core.representation.tree.bool;
  exports io.github.ericmedvet.jgea.core.representation.programsynthesis;
  exports io.github.ericmedvet.jgea.core.representation.programsynthesis.type;
  exports io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn;
  exports io.github.ericmedvet.jgea.core.representation.grammar.grid;
  exports io.github.ericmedvet.jgea.core.selector;
  exports io.github.ericmedvet.jgea.core.solver;
  exports io.github.ericmedvet.jgea.core.solver.mapelites;
  exports io.github.ericmedvet.jgea.core.solver.speciation;
  exports io.github.ericmedvet.jgea.core.solver.cabea;
  exports io.github.ericmedvet.jgea.core.util;
  exports io.github.ericmedvet.jgea.core.representation.sequence.integer;
  exports io.github.ericmedvet.jgea.core.problem;
  exports io.github.ericmedvet.jgea.core.representation.grammar.string;
  exports io.github.ericmedvet.jgea.core.solver.pso;
  exports io.github.ericmedvet.jgea.core.solver.es;
  exports io.github.ericmedvet.jgea.core.solver.cooperative;
  exports io.github.ericmedvet.jgea.core.solver.mapelites.archive;
  exports io.github.ericmedvet.jgea.core.solver.mapelites.strategy;
  exports io.github.ericmedvet.jgea.core.solver.bi;
  exports io.github.ericmedvet.jgea.core.solver.bi.mapelites;
  exports io.github.ericmedvet.jgea.core.solver.multifidelity;
  exports io.github.ericmedvet.jgea.core.representation.tree.parsing;

}