
package io.github.ericmedvet.jgea.core.solver.state;

import io.github.ericmedvet.jgea.core.util.Copyable;
import io.github.ericmedvet.jgea.core.util.Progress;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class State implements Copyable {

  protected final LocalDateTime startingDateTime;
  protected long elapsedMillis;
  protected long nOfIterations;
  protected Progress progress;

  public State() {
    startingDateTime = LocalDateTime.now();
    elapsedMillis = 0;
    nOfIterations = 0;
    progress = Progress.NA;
  }

  protected State(LocalDateTime startingDateTime, long elapsedMillis, long nOfIterations, Progress progress) {
    this.startingDateTime = startingDateTime;
    this.elapsedMillis = elapsedMillis;
    this.nOfIterations = nOfIterations;
    this.progress = progress;
  }

  public long getElapsedMillis() {
    return elapsedMillis;
  }

  public long getNOfIterations() {
    return nOfIterations;
  }

  public Progress getProgress() {
    return progress;
  }

  public void setProgress(Progress progress) {
    this.progress = progress;
  }

  @Override
  public State immutableCopy() {
    return new State(startingDateTime, elapsedMillis, nOfIterations, progress) {

      @Override
      public void incNOfIterations() {
        throw new UnsupportedOperationException("Read-only instance");
      }

      @Override
      public void updateElapsedMillis() {
        throw new UnsupportedOperationException("Read-only instance");
      }

      @Override
      public void setProgress(Progress progress) {
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
