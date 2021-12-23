package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.util.Pair;

import java.io.PrintStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author eric on 2021/01/03 for jgea
 */
public class TabularPrinter<E> implements Listener.Factory<E> {

  private final static String COLOR_RESET = "\u001B[0m";
  private final static String COLOR_DOWN = "\u001B[31m";
  private final static String COLOR_UP = "\u001B[32m";
  private final static String COLOR_SAME = "\u001B[33m";
  private final static String COLOR_NOT_RELEVANT = "\033[0;90m";

  private final static char VARIATION_UP = '↗';
  private final static char VARIATION_DOWN = '↘';
  private final static char VARIATION_SAME = '=';

  private final static String SEP = " ";
  private final static String COLLAPSER_REGEX = "[.→\\[\\]]+";

  private final List<Pair<? extends NamedFunction<? super E, ?>, Integer>> pairs;
  private final PrintStream ps;
  private final int headerInterval;
  private final boolean showLegend;
  private final boolean showVariation;
  private final boolean useColors;

  private final String header;
  private final String legend;

  public TabularPrinter(List<NamedFunction<? super E, ?>> functions) {
    this(functions, System.out, 10, true, true, true);
  }

  public TabularPrinter(
      List<NamedFunction<? super E, ?>> functions,
      PrintStream ps,
      int headerInterval,
      boolean showLegend,
      boolean showVariation,
      boolean useColors
  ) {
    this.pairs = functions.stream()
        .map(f -> Pair.of(f, Math.max(collapse(f.getName()).length(), formatSize(f.getFormat()))))
        .collect(Collectors.toList());
    this.ps = ps;
    this.headerInterval = headerInterval;
    this.showLegend = showLegend;
    this.showVariation = showVariation;
    this.useColors = useColors;
    header = pairs.stream()
        .map(p -> justify(
            collapse(p.first().getName()),
            p.second() + (showVariation ? 1 : 0)
        ))
        .collect(Collectors.joining(SEP));
    int w = pairs.stream().mapToInt(p -> collapse(p.first().getName()).length()).max().orElse(1);
    legend = "Legend:\n" + pairs.stream()
        .map(p -> String.format(
            "%" + w + "." + w + "s → %s [%s]",
            collapse(p.first().getName()),
            p.first().getName(),
            p.first().getFormat()
        ))
        .collect(Collectors.joining("\n"));
  }

  @Override
  public Listener<E> build() {
    if (showLegend) {
      ps.println(legend);
    }
    return new Listener<>() {
      int lineCounter = 0;
      final Object[] lastValues = new Object[pairs.size()];
      final Object[] secondLastValues = new Object[pairs.size()];

      @Override
      public void listen(E e) {
        List<Object> values = pairs.stream()
            .map(p -> p.first().apply(e))
            .collect(Collectors.toList());
        String s = IntStream.range(0, pairs.size())
            .mapToObj(i -> format(
                values.get(i),
                lastValues[i],
                secondLastValues[i],
                pairs.get(i).first().getFormat(),
                pairs.get(i).second()
            ))
            .collect(Collectors.joining(SEP));
        IntStream.range(0, pairs.size()).forEach(i -> {
          secondLastValues[i] = lastValues[i];
          lastValues[i] = values.get(i);
        });
        if (s.isEmpty()) {
          return;
        }
        synchronized (ps) {
          if (headerInterval > 1 && (lineCounter % headerInterval == 0)) {
            ps.println(header);
          }
          ps.println(s);
          lineCounter = lineCounter + 1;
        }
      }
    };
  }

  private String format(Object currentValue, Object lastValue, Object secondLastValue, String format, int l) {
    char currentVariation = showVariation ? variation(currentValue, lastValue) : ' ';
    char lastVariation = showVariation ? variation(lastValue, secondLastValue) : ' ';
    String s = justify(String.format(format, currentValue), l);
    if (showVariation) {
      if (useColors) {
        String color;
        if (currentVariation != lastVariation) {
          if (currentVariation == VARIATION_DOWN) {
            color = COLOR_DOWN;
          } else if (currentVariation == VARIATION_UP) {
            color = COLOR_UP;
          } else {
            color = COLOR_SAME;
          }
        } else {
          color = COLOR_NOT_RELEVANT;
        }
        return s + color + currentVariation + COLOR_RESET;
      }
      return s + currentVariation;
    }
    return s;
  }

  private static String collapse(String name) {
    StringBuilder acronym = new StringBuilder();
    String[] pieces = name.split(COLLAPSER_REGEX);
    for (String piece : pieces) {
      acronym.append(piece.charAt(0));
    }
    return acronym.toString();
  }

  private static int formatSize(String format) {
    int size = 0;
    Matcher matcher = Pattern.compile("\\d++").matcher(format);
    if (matcher.find()) {
      size = Integer.parseInt(matcher.group());
      if (format.contains("+")) {
        size = size + 1;
      }
      return size;
    }
    return String.format(format, (Object[]) null).length();
  }

  private static String justify(String s, int length) {
    if (s.length() > length) {
      return s.substring(0, length);
    }
    StringBuilder sBuilder = new StringBuilder(s);
    while (sBuilder.length() < length) {
      sBuilder.insert(0, " ");
    }
    s = sBuilder.toString();
    return s;
  }

  private static char variation(Object current, Object last) {
    if (current == null || last == null) {
      return ' ';
    }
    if (!(current instanceof Number) || !(last instanceof Number)) {
      return ' ';
    }
    double currentN = ((Number) current).doubleValue();
    double lastN = ((Number) last).doubleValue();
    if (currentN < lastN) {
      return VARIATION_DOWN;
    }
    if (currentN > lastN) {
      return VARIATION_UP;
    }
    return VARIATION_SAME;
  }

}
