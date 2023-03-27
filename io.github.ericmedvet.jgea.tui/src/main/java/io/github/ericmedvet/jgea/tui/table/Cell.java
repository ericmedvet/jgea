package io.github.ericmedvet.jgea.tui.table;

/**
 * @author "Eric Medvet" on 2023/03/27 for jgea
 */
public interface Cell {
  Object content();

  default int length() {
    return content().toString().length();
  }
}
