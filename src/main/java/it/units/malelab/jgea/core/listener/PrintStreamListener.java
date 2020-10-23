/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.listener.collector.DataCollector;
import it.units.malelab.jgea.core.listener.collector.Item;

import java.io.PrintStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class PrintStreamListener<G, S, F> implements Listener<G, S, F> {

  protected static class Column {
    private final String name;
    private final String format;
    private final int size;

    public Column(String name, String format, int size) {
      this.name = name;
      this.format = format;
      this.size = size;
    }

    public String getName() {
      return name;
    }

    public String getFormat() {
      return format;
    }

    public int getSize() {
      return size;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Column column = (Column) o;
      return size == column.size &&
          Objects.equals(name, column.name) &&
          Objects.equals(format, column.format);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, format, size);
    }
  }

  private final PrintStream ps;
  private final boolean format;
  private final int headerInterval;
  private final String innerSeparator;
  private final String outerSeparator;
  private final List<DataCollector<? super G, ? super S, ? super F>> collectors;

  private final List<List<Column>> columnGroups;

  private int lines;

  private final static Logger L = Logger.getLogger(PrintStreamListener.class.getName());

  @SafeVarargs
  public PrintStreamListener(
      PrintStream ps,
      boolean format,
      int headerInterval,
      String innerSeparator,
      String outerSeparator,
      DataCollector<? super G, ? super S, ? super F>... collectors) {
    this.ps = ps;
    this.format = format;
    this.headerInterval = headerInterval;
    this.innerSeparator = innerSeparator;
    this.outerSeparator = outerSeparator;
    this.collectors = Arrays.asList(collectors);
    columnGroups = new ArrayList<>();
    lines = 0;
  }

  @Override
  public void listen(Event<? extends G, ? extends S, ? extends F> event) {
    //collect items
    List<List<Item>> itemGroups = collectItems(event, collectors);
    //possibly init columns
    if (columnGroups.isEmpty()) {
      columnGroups.addAll(buildColumnGroups(itemGroups, format));
    }
    //check consistency of item names
    checkConsistency(itemGroups, columnGroups);
    //possibly print headers
    if ((lines == 0) || ((headerInterval > 0) && (event.getState().getIterations() % headerInterval == 0))) {
      String headers = buildHeaderString(columnGroups, innerSeparator, outerSeparator, format);
      synchronized (ps) {
        ps.println(headers);
      }
    }
    //print values: collectors
    String line = buildDataString(itemGroups, columnGroups, innerSeparator, outerSeparator, format);
    synchronized (ps) {
      ps.println(line);
      lines = lines + 1;
    }
  }

  public static List<List<Column>> buildColumnGroups(List<List<Item>> itemGroups, boolean format) {
    return itemGroups.stream().map(items -> items.stream().map(
        item -> new Column(
            item.getName(),
            item.getFormat(),
            format ? formatName(item.getName(), item.getFormat()).length() : item.getName().length()
        ))
        .collect(Collectors.toList())
    ).collect(Collectors.toList());
  }

  public static <G, S, F> List<List<Item>> collectItems(final Event<? extends G, ? extends S, ? extends F> event, List<DataCollector<? super G, ? super S, ? super F>> collectors) {
    List<List<Item>> itemGroups = new ArrayList<>();
    //collect
    for (DataCollector<? super G, ? super S, ? super F> collector : collectors) {
      try {
        itemGroups.add(collector.collect(event));
      } catch (Throwable t) {
        L.log(Level.WARNING, String.format("Cannot collect from %s due to %s", collector.getClass().getSimpleName(), t), t);
      }
    }
    return itemGroups;
  }

  public static String buildDataString(List<List<Item>> itemGroups, List<List<Column>> columnGroups, String innerSeparator, String outerSeparator, boolean format) {
    Map<String, Item> map = itemGroups.stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toMap(Item::getName, i -> i));
    return columnGroups.stream().map(
        columns -> columns.stream().map(
            column -> toString(map.get(column.getName()), column, format)
        ).collect(Collectors.joining(innerSeparator))
    ).collect(Collectors.joining(outerSeparator));
  }

  public static String buildHeaderString(List<List<Column>> columnGroups, String innerSeparator, String outerSeparator, boolean format) {
    return columnGroups.stream().map(
        columns -> columns.stream().map(
            column -> format ? formatName(column.getName(), column.getFormat()) : column.getName()
        ).collect(Collectors.joining(innerSeparator))
    ).collect(Collectors.joining(outerSeparator));
  }

  public static void checkConsistency(List<List<Item>> itemGroups, List<List<Column>> columnGroups) {
    List<String> currentNames = itemGroups.stream().flatMap(Collection::stream).map(Item::getName).collect(Collectors.toList());
    List<String> expectedNames = columnGroups.stream().flatMap(Collection::stream).map(Column::getName).collect(Collectors.toList());
    if (!currentNames.equals(expectedNames)) {
      L.warning(String.format("%d items received, %d expected", currentNames.size(), expectedNames.size()));
    }
  }

  private static String toString(Item item, Column column, boolean format) {
    Object value = item == null ? null : item.getValue();
    if (value == null) {
      return format ? justify("", column.getSize()) : "";
    } else {
      String string = string = value.toString();
      if (format) {
        try {
          string = String.format(column.getFormat(), value);
        } catch (IllegalFormatException ex) {
          L.log(Level.WARNING, String.format("Cannot format value for item %s", column.getName()), ex);
        }
      }
      return format ? justify(string, column.getSize()) : string;
    }
  }

  private static String formatName(String name, String format) {
    String acronym = "";
    String[] pieces = name.split("\\.");
    for (String piece : pieces) {
      acronym = acronym + piece.substring(0, 1);
    }
    acronym = justify(acronym, formatSize(format));
    return acronym;
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
    while (s.length() < length) {
      s = " " + s;
    }
    return s;
  }

}
