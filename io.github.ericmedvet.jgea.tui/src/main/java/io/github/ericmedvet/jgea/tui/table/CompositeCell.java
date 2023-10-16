
package io.github.ericmedvet.jgea.tui.table;

import java.util.List;
import java.util.stream.Collectors;

public record CompositeCell(List<Cell> cells) implements Cell {
  @Override
  public String content() {
    return cells.stream().map(c -> c.content().toString()).collect(Collectors.joining(""));
  }
}
