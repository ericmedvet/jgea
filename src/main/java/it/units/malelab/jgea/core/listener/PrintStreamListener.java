/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.listener.collector.Collector;
import it.units.malelab.jgea.core.listener.event.Event;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author eric
 */
public class PrintStreamListener<G, S, F> implements Listener {

  private final PrintStream ps;
  private final boolean format;
  private final int headerInterval;
  private final String innerSeparator;
  private final String outerSeparator;
  private final List<Collector<G, S, F>> collectors;

  private final List<Map<String, String>> formattedNames;
  private int lines;

  public PrintStreamListener(
          PrintStream ps,
          boolean format,
          int headerInterval,
          String innerSeparator,
          String outerSeparator,
          Collector<G, S, F>... collectors) {
    this.ps = ps;
    this.format = format;
    this.headerInterval = headerInterval;
    this.innerSeparator = innerSeparator;
    this.outerSeparator = outerSeparator;
    this.collectors = Arrays.asList(collectors);
    formattedNames = new ArrayList<>(this.collectors.size());
    for (Collector<G, S, F> collector : collectors) {
      Map<String, String> localFormattedNames = new LinkedHashMap<>();
      for (String name : collector.getFormattedNames().keySet()) {
        localFormattedNames.put(name, formatName(name, collector.getFormattedNames().get(name), format));
      }
      formattedNames.add(localFormattedNames);
    }
    lines = 0;
  }

  @Override
  public void listen(Event event) {
    EvolutionEvent<G, S, F> evolutionEvent = null;
    if (event instanceof EvolutionEvent) {
      evolutionEvent = ((EvolutionEvent<G, S, F>) event);
    } else {
      return;
    }
    StringBuilder sb = new StringBuilder();
    if ((lines == 0) || ((headerInterval > 0) && (evolutionEvent.getIteration() % headerInterval == 0))) {
      //print header: collectors
      for (int i = 0; i < formattedNames.size(); i++) {
        int j = 0;
        for (String name : formattedNames.get(i).keySet()) {
          sb.append(formattedNames.get(i).get(name));
          if (j != formattedNames.get(i).size() - 1) {
            sb.append(innerSeparator);
          }
          j = j + 1;
        }
        if (i != formattedNames.size() - 1) {
          sb.append(outerSeparator);
        }
      }
      synchronized (ps) {
        ps.println(sb.toString());
      }
      sb.setLength(0);
    }
    //print values: collectors
    for (int i = 0; i < formattedNames.size(); i++) {
      int j = 0;
      Map<String, Object> values = collectors.get(i).collect(evolutionEvent);
      for (String name : formattedNames.get(i).keySet()) {
        if (format) {
          String value;
          try {
            value = String.format(collectors.get(i).getFormattedNames().get(name), values.get(name));
          } catch (IllegalFormatException ex) {
            value = values.get(name).toString();
          }
          sb.append(pad(value, formattedNames.get(i).get(name).length(), format));
        } else {
          sb.append(values.get(name));
        }
        if (j != formattedNames.get(i).size() - 1) {
          sb.append(innerSeparator);
        }
        j = j + 1;
      }
      if (i != formattedNames.size() - 1) {
        sb.append(outerSeparator);
      }
    }
    synchronized (ps) {
      ps.println(sb.toString());
    }
    lines = lines + 1;
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
