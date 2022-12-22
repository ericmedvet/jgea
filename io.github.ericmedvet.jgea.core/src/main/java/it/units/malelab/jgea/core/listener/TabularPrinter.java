/*
 * Copyright 2022 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.core.util.StringUtils;

import java.io.PrintStream;
import java.util.List;
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

  private final static String SEP = " ";

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
    this(eFunctions, kFunctions, System.out, 10, true, true, true, true);
  }

  public TabularPrinter(
      List<? extends NamedFunction<? super E, ?>> eFunctions,
      List<? extends NamedFunction<? super K, ?>> kFunctions,
      PrintStream ps,
      int headerInterval,
      boolean showLegend,
      boolean showVariation,
      boolean useColors,
      boolean robust
  ) {
    eFunctions = robust?eFunctions.stream().map(NamedFunction::robust).toList():eFunctions;
    kFunctions = robust?kFunctions.stream().map(NamedFunction::robust).toList():kFunctions;
    ePairs = eFunctions.stream().map(f -> Pair.of(
        f,
        Math.max(StringUtils.collapse(f.getName()).length(), StringUtils.formatSize(f.getFormat()))
    )).collect(Collectors.toList());
    kPairs = kFunctions.stream().map(f -> Pair.of(
        f,
        Math.max(StringUtils.collapse(f.getName()).length(), StringUtils.formatSize(f.getFormat()))
    )).collect(Collectors.toList());
    this.ps = ps;
    this.headerInterval = headerInterval;
    this.showLegend = showLegend;
    this.showVariation = showVariation;
    this.useColors = useColors;
    List<String> kHeaders = kPairs.stream().map(p -> StringUtils.justify(
        StringUtils.collapse(p.first().getName()),
        p.second()
    )).toList();
    List<String> eHeaders = ePairs.stream().map(p -> StringUtils.justify(
        StringUtils.collapse(p.first().getName()),
        p.second()
    ) + (showVariation ? " " : "")).toList();
    header = String.join(SEP, Misc.concat(List.of(kHeaders, eHeaders)));
    int w = ePairs.stream().mapToInt(p -> StringUtils.collapse(p.first().getName()).length()).max().orElse(1);
    legend = "Legend:\n" + Misc.concat(List.of(kPairs, ePairs))
        .stream()
        .map(p -> String.format(
            "%" + w + "." + w + "s → %s [%s]",
            StringUtils.collapse(p.first().getName()),
            p.first().getName(),
            p.first().getFormat()
        ))
        .collect(Collectors.joining("\n"));
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

  private static String format(Object currentValue, String format, int l) {
    return StringUtils.justify(String.format(format, currentValue), l);
  }

  private String format(Object currentValue, Object lastValue, Object secondLastValue, String format, int l) {
    char currentVariation = showVariation ? StringUtils.variation(currentValue, lastValue) : ' ';
    char lastVariation = showVariation ? StringUtils.variation(lastValue, secondLastValue) : ' ';
    String s = StringUtils.justify(String.format(format, currentValue), l);
    if (showVariation) {
      if (useColors) {
        String color;
        if (currentVariation != lastVariation) {
          if (currentVariation == StringUtils.VARIATION_DOWN) {
            color = COLOR_DOWN;
          } else if (currentVariation == StringUtils.VARIATION_UP) {
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
