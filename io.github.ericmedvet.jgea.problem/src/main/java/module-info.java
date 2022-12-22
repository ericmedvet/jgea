/**
 * @author "Eric Medvet" on 2022/08/29 for jgea
 */
module it.units.malelab.jgea.problem {
  exports io.github.ericmedvet.jgea.problem.application;
  exports io.github.ericmedvet.jgea.problem.booleanfunction;
  exports io.github.ericmedvet.jgea.problem.classification;
  exports io.github.ericmedvet.jgea.problem.extraction;
  exports io.github.ericmedvet.jgea.problem.extraction.string;
  exports io.github.ericmedvet.jgea.problem.image;
  exports io.github.ericmedvet.jgea.problem.mapper;
  exports io.github.ericmedvet.jgea.problem.symbolicregression;
  exports io.github.ericmedvet.jgea.problem.synthetic;
  requires it.units.malelab.jgea.core;
  requires commons.math3;
  requires java.desktop;
  requires com.google.common;
}