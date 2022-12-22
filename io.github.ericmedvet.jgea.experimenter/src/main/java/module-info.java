/**
 * @author "Eric Medvet" on 2022/11/24 for jgea
 */
module io.github.ericmedvet.jgea.experimenter {
  opens io.github.ericmedvet.jgea.experimenter.builders to io.github.ericmedvet.jnb.core;
  exports io.github.ericmedvet.jgea.experimenter;
  requires io.github.ericmedvet.jnb.core;
  requires io.github.ericmedvet.jgea.core;
  requires io.github.ericmedvet.jgea.tui;
  requires java.logging;
  requires java.desktop;
  requires io.github.ericmedvet.jgea.telegram;
}