
package io.github.ericmedvet.jgea.sample;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.LogManager;
import java.util.logging.Logger;
public abstract class Worker implements Runnable {

  protected final static Logger L = Logger.getLogger(Worker.class.getName());

  static {
    try {
      LogManager.getLogManager().readConfiguration(Worker.class.getClassLoader()
          .getResourceAsStream("logging.properties"));
    } catch (IOException ex) {
      //ignore
    }
  }

  protected final ExecutorService executorService;
  protected final String[] args;

  public Worker(String[] args) {
    this.args = args;
    executorService = Executors.newFixedThreadPool(Args.i(Args.a(
        args,
        "threads",
        Integer.toString(Runtime.getRuntime().availableProcessors())
    )));
    run();
    executorService.shutdown();
  }

  protected String a(String name, String defaultValue) {
    return Args.a(args, name, defaultValue);
  }

}
