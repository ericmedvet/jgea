package it.units.malelab.jgea.core.solver;

import it.units.malelab.jgea.core.util.Copyable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class State implements Copyable {

  protected final LocalDateTime startingDateTime;
  protected long elapsedMillis;
  protected long nOfIterations;

  public State() {
    startingDateTime = LocalDateTime.now();
    elapsedMillis = 0;
    nOfIterations = 0;
  }

  protected State(LocalDateTime startingDateTime, long elapsedMillis, long nOfIterations) {
    this.startingDateTime = startingDateTime;
    this.elapsedMillis = elapsedMillis;
    this.nOfIterations = nOfIterations;
  }

  public long getElapsedMillis() {
    return elapsedMillis;
  }

  public long getNOfIterations() {
    return nOfIterations;
  }

  @Override
  public State immutableCopy() {
    return new State(startingDateTime, elapsedMillis, nOfIterations) {

      @Override
      public void incNOfIterations() {
        throw new UnsupportedOperationException("Read-only instance");
      }

      @Override
      public void updateElapsedMillis() {
        throw new UnsupportedOperationException("Read-only instance");
      }
    };
  }

  public void incNOfIterations() {
    nOfIterations = nOfIterations + 1;
  }

  public void updateElapsedMillis() {
    elapsedMillis = ChronoUnit.MILLIS.between(startingDateTime, LocalDateTime.now());
  }
}
