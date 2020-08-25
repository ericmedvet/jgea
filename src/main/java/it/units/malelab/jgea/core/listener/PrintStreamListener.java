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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.units.malelab.jgea.core.listener.collector.DataCollector;
import it.units.malelab.jgea.core.listener.collector.Item;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class PrintStreamListener<G, S, F> implements Listener<G, S, F> {

  private final PrintStream ps;
  private final boolean format;
  private final int headerInterval;
  private final String innerSeparator;
  private final String outerSeparator;
  private final List<DataCollector<? super G, ? super S, ? super F>> collectors;

  private final List<List<Item>> firstItems;
  private final List<List<Integer>> sizes;
  private int lines;

  private final static Logger L = Logger.getLogger(PrintStreamListener.class.getName());

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
    firstItems = new ArrayList<>();
    sizes = new ArrayList<>();
    lines = 0;
  }

  @Override
  public void listen(Event<? extends G, ? extends S, ? extends F> event) {
    //collect items
    List<List<Item>> items = collectItems(event);
    //possibly print headers
    if ((lines == 0) || ((headerInterval > 0) && (event.getState().getIterations() % headerInterval == 0))) {
      String headers = buildHeadersString();
      synchronized (ps) {
        ps.println(headers);
      }
    }
    //print values: collectors
    String data = buildDataString(items);
    synchronized (ps) {
      ps.println(data);
    }
  }

  protected List<List<Item>> collectItems(final Event<? extends G, ? extends S, ? extends F> event) {
    List<List<Item>> items = new ArrayList<>();
    //collect
    for (DataCollector<? super G, ? super S, ? super F> collector : collectors) {
      try {
        items.add(collector.collect(event));
      } catch (Throwable t) {
        L.log(Level.WARNING, String.format("Cannot collect from %s due to %s", collector.getClass().getSimpleName(), t), t);
      }
    }
    if (firstItems.isEmpty()) {
      firstItems.addAll(items);
      sizes.addAll(firstItems.stream()
          .map(is -> is.stream().map(
              i -> formatName(i.getName(), i.getFormat(), format).length()
          ).collect(Collectors.toList()))
          .collect(Collectors.toList()));
    }
    return items;
  }

  protected String buildDataString(List<List<Item>> items) {
    List<Map<String, Item>> maps = items.stream()
        .map(cItems -> cItems.stream().collect(Collectors.toMap(Item::getName, i -> i)))
        .collect(Collectors.toList());
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < firstItems.size(); i++) {
      for (int j = 0; j < firstItems.get(i).size(); j++) {
        String name = firstItems.get(i).get(j).getName();
        Object value = maps.get(i).get(name).getValue();
        if (format) {
          try {
            sb.append(pad(
                String.format(firstItems.get(i).get(j).getFormat(), value),
                sizes.get(i).get(j), format
            ));
          } catch (IllegalFormatException ex) {
            sb.append(pad(
                (value != null) ? value.toString() : "",
                sizes.get(i).get(j), format
            ));
          }
        } else {
          sb.append((value != null) ? value.toString() : "");
        }
        if (j != firstItems.get(i).size() - 1) {
          sb.append(innerSeparator);
        }
      }
      if (i != firstItems.size() - 1) {
        sb.append(outerSeparator);
      }
    }
    lines = lines + 1;
    return sb.toString();
  }

  public String buildHeadersString() {
    StringBuilder sb = new StringBuilder();
    //print header: collectors
    for (int i = 0; i < firstItems.size(); i++) {
      for (int j = 0; j < firstItems.get(i).size(); j++) {
        sb.append(formatName(firstItems.get(i).get(j).getName(), firstItems.get(i).get(j).getFormat(), format));
        if (j != firstItems.get(i).size() - 1) {
          sb.append(innerSeparator);
        }
      }
      if (i != firstItems.size() - 1) {
        sb.append(outerSeparator);
      }
    }
    return sb.toString();
  }

  private String formatName(String name, String format, boolean doFormat) {
    if (!doFormat) {
      return name;
    }
    String acronym = "";
    String[] pieces = name.split("\\.");
    for (String piece : pieces) {
      acronym = acronym + piece.substring(0, 1);
    }
    acronym = pad(acronym, formatSize(format), doFormat);
    return acronym;
  }

  private int formatSize(String format) {
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

  private String pad(String s, int length, boolean doFormat) {
    while (doFormat && (s.length() < length)) {
      s = " " + s;
    }
    return s;
  }

}
