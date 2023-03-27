package io.github.ericmedvet.jgea.tui.table;

import com.googlecode.lanterna.TextColor;

/**
 * @author "Eric Medvet" on 2023/03/27 for jgea
 */
public record ColoredStringCell(String content, TextColor color) implements Cell {
}
