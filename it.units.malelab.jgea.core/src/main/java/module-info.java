/**
 * @author "Eric Medvet" on 2022/08/29 for jgea
 */
module it.units.malelab.jgea.core {
  exports it.units.malelab.jgea.core;
  exports it.units.malelab.jgea.core.distance;
  exports it.units.malelab.jgea.core.fitness;
  exports it.units.malelab.jgea.core.listener;
  exports it.units.malelab.jgea.core.operator;
  exports it.units.malelab.jgea.core.order;
  exports it.units.malelab.jgea.core.representation.grammar;
  exports it.units.malelab.jgea.core.representation.grammar.cfggp;
  exports it.units.malelab.jgea.core.representation.grammar.ge;
  exports it.units.malelab.jgea.core.representation.graph;
  exports it.units.malelab.jgea.core.representation.graph.finiteautomata;
  exports it.units.malelab.jgea.core.representation.graph.numeric;
  exports it.units.malelab.jgea.core.representation.graph.numeric.functiongraph;
  exports it.units.malelab.jgea.core.representation.graph.numeric.operatorgraph;
  exports it.units.malelab.jgea.core.representation.sequence;
  exports it.units.malelab.jgea.core.representation.sequence.bit;
  exports it.units.malelab.jgea.core.representation.sequence.numeric;
  exports it.units.malelab.jgea.core.representation.tree;
  exports it.units.malelab.jgea.core.selector;
  exports it.units.malelab.jgea.core.solver;
  exports it.units.malelab.jgea.core.solver.mapelites;
  exports it.units.malelab.jgea.core.solver.speciation;
  exports it.units.malelab.jgea.core.solver.state;
  exports it.units.malelab.jgea.core.util;
  requires com.google.common;
  requires org.knowm.xchart;
  requires java.desktop;
  requires commons.csv;
  requires java.logging;
  requires commons.math3;
}