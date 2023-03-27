/**
 * @author "Eric Medvet" on 2022/09/02 for jgea
 */
module io.github.ericmedvet.jgea.tui {
  requires io.github.ericmedvet.jgea.core;
  requires java.logging;
  requires com.googlecode.lanterna;
  requires jdk.management;
  exports io.github.ericmedvet.jgea.tui;
  exports io.github.ericmedvet.jgea.tui.util;
  exports io.github.ericmedvet.jgea.tui.table;
}