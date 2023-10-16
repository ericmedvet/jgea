package io.github.ericmedvet.jgea.tui.table;
public interface Cell {
  Object content();

  default int length() {
    return content().toString().length();
  }
}
