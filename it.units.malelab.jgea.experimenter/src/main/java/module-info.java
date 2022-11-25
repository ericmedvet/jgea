/**
 * @author "Eric Medvet" on 2022/11/24 for jgea
 */
module it.units.malelab.jgea.experimenter {
  opens it.units.malelab.jgea.experimenter.builders to it.units.malelab.jnb.core;
  exports it.units.malelab.jgea.experimenter;
  requires it.units.malelab.jnb.core;
  requires it.units.malelab.jgea.core;
  requires it.units.malelab.jgea.tui;
  requires java.logging;
  requires java.desktop;
  requires it.units.malelab.jgea.telegram;
}