package io.github.ericmedvet.jgea.experimenter.listener.tui.util;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import io.github.ericmedvet.jgea.core.util.Table;
import io.github.ericmedvet.jgea.experimenter.listener.tui.table.Cell;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2023/11/07 for jgea
 */
public class TuiDrawer {

  private static final String VERTICAL_PART_FILLER = " ▁▂▃▄▅▆▇";
  private static final char FILLER = '█';
  private static final Configuration DEFAULT_CONFIGURATION = new Configuration(
      TextColor.Factory.fromString("#10A010"),
      TextColor.Factory.fromString("#10A010"),
      TextColor.Factory.fromString("#A01010"),
      TextColor.Factory.fromString("#404040"),
      TextColor.Factory.fromString("#A0A0A0"),
      TextColor.Factory.fromString("#F0F0F0")
  );
  private final Configuration configuration;
  private final TextGraphics textGraphics;
  private final Rectangle viewport;
  public TuiDrawer(Configuration configuration, TextGraphics textGraphics, Rectangle viewport) {
    this.configuration = configuration;
    this.textGraphics = textGraphics;
    this.viewport = viewport;
  }

  public TuiDrawer(TextGraphics textGraphics, Rectangle viewport) {
    this(DEFAULT_CONFIGURATION, textGraphics, viewport);
  }

  public TuiDrawer(TextGraphics textGraphics) {
    this(
        DEFAULT_CONFIGURATION,
        textGraphics,
        new Rectangle(new Point(0, 0), new Point(textGraphics.getSize().getColumns(), textGraphics.getSize().getRows()))
    );
  }

  public record Configuration(
      TextColor frameColor,
      TextColor frameLabelColor,
      TextColor dataLabelColor,
      TextColor missingDataColor,
      TextColor dataColor,
      TextColor mainDataColor
  ) {}

  public TuiDrawer clear() {
    textGraphics.fillRectangle(viewport.ne().tp(), new TerminalSize(viewport.w(), viewport.h()), ' ');
    return this;
  }

  public TuiDrawer drawFrame(
      String label
  ) {
    textGraphics.setForegroundColor(configuration.frameColor);
    textGraphics.drawLine(
        viewport.ne().delta(1, 0).tp(),
        viewport.nw().delta(-1, 0).tp(),
        Symbols.SINGLE_LINE_HORIZONTAL
    );
    textGraphics.drawLine(
        viewport.se().delta(1, 0).tp(),
        viewport.sw().delta(-1, 0).tp(),
        Symbols.SINGLE_LINE_HORIZONTAL
    );
    textGraphics.drawLine(
        viewport.ne().delta(0, 1).tp(),
        viewport.se().delta(0, -1).tp(),
        Symbols.SINGLE_LINE_VERTICAL
    );
    textGraphics.drawLine(
        viewport.nw().delta(0, 1).tp(),
        viewport.sw().delta(0, -1).tp(),
        Symbols.SINGLE_LINE_VERTICAL
    );
    textGraphics.setCharacter(viewport.ne().tp(), Symbols.SINGLE_LINE_TOP_LEFT_CORNER);
    textGraphics.setCharacter(viewport.se().tp(), Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER);
    textGraphics.setCharacter(viewport.nw().tp(), Symbols.SINGLE_LINE_TOP_RIGHT_CORNER);
    textGraphics.setCharacter(viewport.sw().tp(), Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER);
    textGraphics.setForegroundColor(configuration.frameLabelColor);
    if (!label.isEmpty()) {
      drawString(2, 0, "[" + label + "]");
    }
    return this;
  }

  public TuiDrawer drawString(Point p, String s, TextColor textColor, SGR... sgrs) {
    // multiline
    if (s.lines().count() > 1) {
      List<String> lines = s.lines().toList();
      for (int i = 0; i < lines.size(); i++) {
        drawString(p.delta(0, i), lines.get(i), textColor, sgrs);
      }
      return this;
    }
    if (p.y() >= viewport.h() || p.y() < 0) {
      return this;
    }
    int headD = Math.max(0, -p.x());
    int tailD = Math.max(0, p.x() + s.length() - viewport.w());
    if (s.length() - headD - tailD <= 0) {
      return this;
    }
    s = s.substring(headD, s.length() - tailD);
    textGraphics.setForegroundColor(textColor);
    if (sgrs.length == 0) {
      textGraphics.putString(p.delta(headD + viewport.min().x(), viewport.min().y()).tp(), s);
    } else if (sgrs.length == 1) {
      textGraphics.putString(p.delta(headD + viewport.min().x(), viewport.min().y()).tp(), s, sgrs[0]);
    } else {
      textGraphics.putString(p.delta(headD + viewport.min().x(), viewport.min().y()).tp(), s, sgrs[0], sgrs);
    }
    return this;
  }

  public TuiDrawer drawString(int x, int y, String s, TextColor textColor, SGR... sgrs) {
    return drawString(new Point(x, y), s, textColor, sgrs);
  }

  public TuiDrawer drawString(int x, int y, String s, SGR... sgrs) {
    return drawString(x, y, s, configuration.dataColor, sgrs);
  }

  public <K> TuiDrawer drawTable(Table<K, String, ? extends Cell> table) {
    Map<String, Integer> widths = table.colIndexes().stream()
        .collect(Collectors.toMap(
            ci -> ci,
            ci -> Math.max(
                ci.length(),
                table.columnValues(ci).stream().mapToInt(Cell::preferredWidth).max().orElse(0)
            )
        ));
    int x = 0;
    int y = 0;
    // header
    for (String ci : table.colIndexes()) {
      drawString(x, y, ci, configuration.dataLabelColor);
      x = x + widths.get(ci) + 1;
    }
    y = y + 1;
    // rows
    for (K ri : table.rowIndexes()) {
      x = 0;
      for (String ci : table.colIndexes()) {
        int w = widths.get(ci);
        TuiDrawer cellTd = in(new Rectangle(new Point(x, y), new Point(x + w + 1, y + 1)));
        table.get(ri, ci).draw(cellTd, w);
        x = x + w + 1;
      }
      y = y + 1;
    }
    return this;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public TuiDrawer in(Rectangle rectangle) {
    return new TuiDrawer(configuration, textGraphics, new Rectangle(
        this.viewport.min().delta(rectangle.min().x(), rectangle.min().y()),
        this.viewport.min().delta(rectangle.min().x(), rectangle.min().y()).delta(rectangle.w(), rectangle.h())
    ));
  }

  public TuiDrawer inX(float x, float w) {
    return in(new Rectangle(
        new Point((int) (viewport.w() * x), 0),
        new Point((int) (viewport.w() * (x + w)), viewport.max().y())
    ));
  }

  public TuiDrawer inY(float y, float h) {
    return in(new Rectangle(
        new Point(0, (int) (viewport.h() * y)),
        new Point(viewport.max().x(), (int) (viewport.h() * (y + h)))
    ));
  }

  public TuiDrawer inner(int delta) {
    return in(viewport.inner(delta));
  }
}
