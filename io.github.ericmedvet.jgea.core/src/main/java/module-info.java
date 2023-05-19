/**
 * @author "Eric Medvet" on 2022/08/29 for jgea
 */
module io.github.ericmedvet.jgea.core {
  exports io.github.ericmedvet.jgea.core;
  exports io.github.ericmedvet.jgea.core.distance;
  exports io.github.ericmedvet.jgea.core.fitness;
  exports io.github.ericmedvet.jgea.core.listener;
  exports io.github.ericmedvet.jgea.core.operator;
  exports io.github.ericmedvet.jgea.core.order;
  exports io.github.ericmedvet.jgea.core.representation.grammar;
  exports io.github.ericmedvet.jgea.core.representation.grammar.cfggp;
  exports io.github.ericmedvet.jgea.core.representation.grammar.ge;
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
  exports io.github.ericmedvet.jgea.core.representation.tree.booleanfunction;
  exports io.github.ericmedvet.jgea.core.representation;
  exports io.github.ericmedvet.jgea.core.selector;
  exports io.github.ericmedvet.jgea.core.solver;
  exports io.github.ericmedvet.jgea.core.solver.mapelites;
  exports io.github.ericmedvet.jgea.core.solver.speciation;
  exports io.github.ericmedvet.jgea.core.solver.state;
  exports io.github.ericmedvet.jgea.core.util;
  exports io.github.ericmedvet.jgea.core.representation.sequence.integer;
  requires io.github.ericmedvet.jsdynsym.core;
  requires com.google.common;
  requires org.knowm.xchart;
  requires java.desktop;
  requires java.logging;
  requires commons.math3;
  requires org.apache.commons.csv;
}