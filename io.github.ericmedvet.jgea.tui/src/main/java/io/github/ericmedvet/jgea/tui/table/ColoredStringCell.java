package io.github.ericmedvet.jgea.tui.table;

import com.googlecode.lanterna.TextColor;
public record ColoredStringCell(String content, TextColor color) implements Cell {
}
