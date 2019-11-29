/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea;

import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.PrintStreamListener;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import it.units.malelab.jgea.core.listener.collector.DataCollector;
import it.units.malelab.jgea.core.util.Args;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static it.units.malelab.jgea.core.util.Args.*;

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
  
  protected final static Logger L = Logger.getLogger(Worker.class.getName());

  public Worker(String[] args) throws FileNotFoundException {
    this.args = args;
    executorService = Executors.newFixedThreadPool(i(Args.a(args, "threads", Integer.toString(Runtime.getRuntime().availableProcessors()))));
    run();
    executorService.shutdown();
  }
  
  protected String a(String name, String defaultValue) {
    return Args.a(args, name, defaultValue);
  }

  protected Listener listener(DataCollector... collectors) {
    return new PrintStreamListener(System.out, true, 10, " ", " | ", collectors);
  }

}
