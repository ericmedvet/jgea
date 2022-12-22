/**
 * @author "Eric Medvet" on 2022/11/24 for jgea
 */
module it.units.malelab.jgea.experimenter {
  opens io.github.ericmedvet.jgea.experimenter.builders to io.github.ericmedvet.jnb.core;
  exports io.github.ericmedvet.jgea.experimenter;
  requires io.github.ericmedvet.jnb.core;
  requires it.units.malelab.jgea.core;
  requires it.units.malelab.jgea.tui;
  requires java.logging;
  requires java.desktop;
  requires it.units.malelab.jgea.telegram;
}