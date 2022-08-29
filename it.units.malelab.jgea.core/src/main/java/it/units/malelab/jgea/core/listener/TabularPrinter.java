package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.util.Misc;
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
public class TabularPrinter<E, K> implements ListenerFactory<E, K> {

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

  private final List<Pair<? extends NamedFunction<? super E, ?>, Integer>> ePairs;
  private final List<Pair<? extends NamedFunction<? super K, ?>, Integer>> kPairs;
  private final PrintStream ps;
  private final int headerInterval;
  private final boolean showLegend;
  private final boolean showVariation;
  private final boolean useColors;

  private final String header;
  private final String legend;

  public TabularPrinter(List<NamedFunction<? super E, ?>> eFunctions, List<NamedFunction<? super K, ?>> kFunctions) {
    this(eFunctions, kFunctions, System.out, 10, true, true, true);
  }

  public TabularPrinter(
      List<NamedFunction<? super E, ?>> eFunctions,
      List<NamedFunction<? super K, ?>> kFunctions,
      PrintStream ps,
      int headerInterval,
      boolean showLegend,
      boolean showVariation,
      boolean useColors
  ) {
    ePairs = eFunctions.stream().map(f -> Pair.of(
        f,
        Math.max(collapse(f.getName()).length(), formatSize(f.getFormat()))
    )).collect(Collectors.toList());
    kPairs = kFunctions.stream().map(f -> Pair.of(
        f,
        Math.max(collapse(f.getName()).length(), formatSize(f.getFormat()))
    )).collect(Collectors.toList());
    this.ps = ps;
    this.headerInterval = headerInterval;
    this.showLegend = showLegend;
    this.showVariation = showVariation;
    this.useColors = useColors;
    List<String> kHeaders = kPairs.stream().map(p -> justify(collapse(p.first().getName()), p.second())).toList();
    List<String> eHeaders = ePairs.stream().map(p -> justify(
        collapse(p.first().getName()),
        p.second()
    ) + (showVariation ? " " : "")).toList();
    header = String.join(SEP, Misc.concat(List.of(kHeaders, eHeaders)));
    int w = ePairs.stream().mapToInt(p -> collapse(p.first().getName()).length()).max().orElse(1);
    legend = "Legend:\n" + Misc.concat(List.of(kPairs, ePairs))
        .stream()
        .map(p -> String.format(
            "%" + w + "." + w + "s → %s [%s]",
            collapse(p.first().getName()),
            p.first().getName(),
            p.first().getFormat()
        ))
        .collect(Collectors.joining("\n"));
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
    int size;
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

  @Override
  public Listener<E> build(K k) {
    if (showLegend) {
      ps.println(legend);
    }
    List<?> fixedValues = kPairs.stream().map(p -> p.first().apply(k)).toList();
    final String fixedS = IntStream.range(0, kPairs.size())
        .mapToObj(i -> format(fixedValues.get(i), kPairs.get(i).first().getFormat(), kPairs.get(i).second()))
        .collect(Collectors.joining(SEP));
    return new Listener<>() {
      final Object[] lastValues = new Object[ePairs.size()];
      final Object[] secondLastValues = new Object[ePairs.size()];
      int lineCounter = 0;

      @Override
      public void listen(E e) {
        List<?> values = ePairs.stream().map(p -> p.first().apply(e)).toList();
        String s = IntStream.range(0, ePairs.size()).mapToObj(i -> format(
            values.get(i),
            lastValues[i],
            secondLastValues[i],
            ePairs.get(i).first().getFormat(),
            ePairs.get(i).second()
        )).collect(Collectors.joining(SEP));
        IntStream.range(0, ePairs.size()).forEach(i -> {
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
          ps.println((fixedS.isEmpty() ? "" : (fixedS + SEP)) + s);
          lineCounter = lineCounter + 1;
        }
      }
    };
  }

  private String format(Object currentValue, String format, int l) {
    return justify(String.format(format, currentValue), l);
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

}
