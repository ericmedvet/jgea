/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea;

import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.PrintStreamListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import it.units.malelab.jgea.core.listener.collector.DataCollector;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public abstract class Worker implements Runnable {

  protected final ExecutorService executorService;
  protected final String[] args;
  protected final String baseResultFileName;
  private final Map<List<String>, Listener> fileListeners;
  
  private final static Logger L = Logger.getLogger(Worker.class.getName());

  public Worker(String[] args) throws FileNotFoundException {
    this.args = args;
    baseResultFileName = a(args, "file", null);
    fileListeners = new HashMap<>();
    executorService = Executors.newFixedThreadPool(i(a(args, "threads", Integer.toString(Runtime.getRuntime().availableProcessors()))));
    run();
    executorService.shutdown();
  }

  private final static String PIECES_SEP = "-";
  private final static String OPTIONS_SEP = ",";
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

  protected double d(String s) {
    return Double.parseDouble(s);
  }

  protected String a(String[] args, String name, String defaultValue) {
    for (String arg : args) {
      String[] pieces = arg.split(KEYVAL_SEP);
      if (pieces[0].equals(name)) {
        return pieces[1];
      }
    }
    return defaultValue;
  }

  protected List<String> l(String s) {
    return Arrays.stream(s.split(OPTIONS_SEP)).collect(Collectors.toList());
  }

  protected List<Integer> i(List<String> strings) {
    return strings.stream().map(Integer::parseInt).collect(Collectors.toList());
  }

  protected Listener listener(DataCollector... collectors) {
    if (baseResultFileName == null) {
      return new PrintStreamListener(System.out, true, 10, " ", " | ", collectors);
    }
    List<String> names = Collections.EMPTY_LIST;
    
    //TODO redo
    
    Listener listener = fileListeners.get(names);
    if (listener==null) {
      String fileName = a(args, "dir", ".")+File.separator+String.format(baseResultFileName, names.hashCode());
      try {
        PrintStream filePrintStream = new PrintStream(fileName);
        listener = new PrintStreamListener(filePrintStream, false, 0, "; ", "; ", collectors);
        fileListeners.put(names, listener);
        L.log(Level.INFO, String.format("New output file %s created", fileName));
      } catch (FileNotFoundException ex) {
        L.log(Level.SEVERE, String.format("Cannot create output file %s", fileName), ex);
        return new PrintStreamListener(System.out, true, 10, " ", " | ", collectors);
      }
    }
    return listener;   
  }

}
