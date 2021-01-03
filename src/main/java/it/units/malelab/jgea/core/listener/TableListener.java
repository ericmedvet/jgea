package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.util.Pair;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author eric on 2021/01/03 for jgea
 */
public class TableListener<G, S, F> implements Listener<G, S, F> {

  private final static String SEP = " ";
  private final static String COLLAPSER_REGEX = "[.→\\[\\]]+";

  private final List<Pair<? extends NamedFunction<Event<? extends G, ? extends S, ? extends F>, ?>, Integer>> pairs;
  private final PrintStream ps;
  private final int headerInterval;
  private final boolean legendFlag;
  private final String header;
  private final String legend;

  private int lineCounter;

  public TableListener(
      List<NamedFunction<Event<? extends G, ? extends S, ? extends F>, ?>> functions,
      PrintStream ps,
      int headerInterval,
      boolean legendFlag
  ) {
    this.pairs = functions.stream()
        .map(f -> Pair.of(f, Math.max(collapse(f.getName()).length(), formatSize(f.getFormat()))))
        .collect(Collectors.toList());
    this.ps = ps;
    this.headerInterval = headerInterval;
    this.legendFlag = legendFlag;
    header = pairs.stream()
        .map(p -> justify(
            collapse(p.first().getName()),
            p.second()
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
    lineCounter = 0;
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

  @Override
  public void listen(Event<? extends G, ? extends S, ? extends F> event) {
    String s = pairs.stream()
        .map(p -> justify(
            String.format(p.first().getFormat(), p.first().apply(event)),
            p.second()
        ))
        .collect(Collectors.joining(SEP));
    synchronized (ps) {
      if (legendFlag && lineCounter == 0) {
        ps.println(legend);
      }
      if (headerInterval > 1 && (lineCounter % headerInterval == 0)) {
        ps.println(header);
      }
      ps.println(s);
      lineCounter = lineCounter + 1;
    }
  }

  @Override
  public void listenSolutions(Collection<? extends S> solutions) {
    synchronized (ps) {
      lineCounter = 0;
    }
  }

}
