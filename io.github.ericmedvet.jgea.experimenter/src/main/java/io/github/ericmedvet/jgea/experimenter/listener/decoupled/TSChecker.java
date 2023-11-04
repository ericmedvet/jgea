package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author "Eric Medvet" on 2023/11/04 for jgea
 */
public class TSChecker {

  private final TabledSource ts;

  public TSChecker(TabledSource ts) {
    this.ts = ts;
    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> check(ts), 0, 1, TimeUnit.SECONDS);
  }

  private static void check(TabledSource ts) {
    ts.refresh();
    System.out.println(ts.getDataItems().prettyPrint(
        p -> "%tT %s".formatted(p.first(), p.second().name()),
        "%5.5s"::formatted,
        v -> v.content().toString()
    ));
  }

}
