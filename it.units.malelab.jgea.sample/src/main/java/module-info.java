/**
 * @author "Eric Medvet" on 2022/08/29 for jgea
 */
module it.units.malelab.jgea.sample {
  opens it.units.malelab.jgea.sample.experimenter to it.units.malelab.jnb.core;
  requires it.units.malelab.jgea.core;
  requires it.units.malelab.jgea.problem;
  requires it.units.malelab.jgea.tui;
  requires it.units.malelab.jgea.experimenter;
  requires it.units.malelab.jnb.core;
  requires com.google.common;
  requires java.logging;
  requires java.desktop;
}