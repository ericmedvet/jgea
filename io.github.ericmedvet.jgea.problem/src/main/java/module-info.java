/**
 * @author "Eric Medvet" on 2022/08/29 for jgea
 */
module io.github.ericmedvet.jgea.problem {
  exports io.github.ericmedvet.jgea.problem.booleanfunction;
  exports io.github.ericmedvet.jgea.problem.classification;
  exports io.github.ericmedvet.jgea.problem.extraction;
  exports io.github.ericmedvet.jgea.problem.extraction.string;
  exports io.github.ericmedvet.jgea.problem.image;
  exports io.github.ericmedvet.jgea.problem.mapper;
  exports io.github.ericmedvet.jgea.problem.regression;
  exports io.github.ericmedvet.jgea.problem.synthetic;
  exports io.github.ericmedvet.jgea.problem.regression.univariate;
  exports io.github.ericmedvet.jgea.problem.regression.multivariate;
  exports io.github.ericmedvet.jgea.problem.regression.univariate.synthetic;
  exports io.github.ericmedvet.jgea.problem;
  requires io.github.ericmedvet.jgea.core;
  requires io.github.ericmedvet.jsdynsym.core;
  requires commons.math3;
  requires java.desktop;
  requires com.google.common;
  requires org.apache.commons.csv;
  requires java.logging;
}