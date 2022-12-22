/**
 * @author "Eric Medvet" on 2022/08/29 for jgea
 */
module it.units.malelab.jgea.problem {
  exports it.units.malelab.jgea.problem.application;
  exports it.units.malelab.jgea.problem.booleanfunction;
  exports it.units.malelab.jgea.problem.classification;
  exports it.units.malelab.jgea.problem.extraction;
  exports it.units.malelab.jgea.problem.extraction.string;
  exports it.units.malelab.jgea.problem.image;
  exports it.units.malelab.jgea.problem.mapper;
  exports it.units.malelab.jgea.problem.symbolicregression;
  exports it.units.malelab.jgea.problem.synthetic;
  requires it.units.malelab.jgea.core;
  requires commons.math3;
  requires java.desktop;
  requires com.google.common;
}