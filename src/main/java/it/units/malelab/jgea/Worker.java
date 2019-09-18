/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea;

import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.PrintStreamListener;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import it.units.malelab.jgea.core.listener.collector.DataCollector;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 *
 * @author eric
 */
public abstract class Worker implements Runnable {

  static {
    try {
      LogManager.getLogManager().readConfiguration(Worker.class.getClassLoader().getResourceAsStream("logging.properties"));
    } catch (IOException ex) {
      //ignore
    } catch (SecurityException ex) {
      //ignore
    }
  }
  
  protected final ExecutorService executorService;
  protected final String[] args;
  
  private final static Logger L = Logger.getLogger(Worker.class.getName());

  public Worker(String[] args) throws FileNotFoundException {
    this.args = args;
    executorService = Executors.newFixedThreadPool(i(a("threads", Integer.toString(Runtime.getRuntime().availableProcessors()))));
    run();
    executorService.shutdown();
  }

  private final static String PIECES_SEP = "-";
  private final static String OPTIONS_SEP = ",";
  private final static String RANGE_SEP = ":";
  private final static String KEYVAL_SEP = "=";

  protected String p(String s, int n) {
    String[] pieces = s.split(PIECES_SEP);
    if (n < pieces.length) {
      return pieces[n];
    }
    return null;
  }

  protected int i(String s) {
    return Integer.parseInt(s);
  }

  protected boolean b(String s) {
    return Boolean.parseBoolean(s);
  }

  protected double d(String s) {
    return Double.parseDouble(s);
  }

  protected String a(String name, String defaultValue) {
    for (String arg : args) {
      String[] pieces = arg.split(KEYVAL_SEP);
      if (pieces[0].equals(name)) {
        return pieces[1];
      }
    }
    return defaultValue;
  }
  
  protected int[] ri(String s) {
    String[] pieces = s.split(RANGE_SEP);
    return IntStream.range(Integer.parseInt(pieces[0]), Integer.parseInt(pieces[1])).toArray();
  }

  protected List<String> l(String s) {
    return Arrays.stream(s.split(OPTIONS_SEP)).collect(Collectors.toList());
  }

  protected List<Integer> i(List<String> strings) {
    return strings.stream().map(Integer::parseInt).collect(Collectors.toList());
  }
  
  protected List<Double> d(List<String> strings) {
    return strings.stream().map(Double::parseDouble).collect(Collectors.toList());
  }

  protected Listener listener(DataCollector... collectors) {
    return new PrintStreamListener(System.out, true, 10, " ", " | ", collectors);
  }

}
