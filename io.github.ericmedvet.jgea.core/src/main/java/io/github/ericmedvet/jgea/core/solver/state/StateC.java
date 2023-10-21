/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.jgea.core.solver.state;

import io.github.ericmedvet.jgea.core.util.Copyable;
import io.github.ericmedvet.jgea.core.util.Progress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class StateC implements Copyable {

  protected final LocalDateTime startingDateTime;
  protected long elapsedMillis;
  protected long nOfIterations;
  protected Progress progress;

  public StateC() {
    startingDateTime = LocalDateTime.now();
    elapsedMillis = 0;
    nOfIterations = 0;
    progress = Progress.NA;
  }

  protected StateC(
      LocalDateTime startingDateTime, long elapsedMillis, long nOfIterations, Progress progress) {
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
  public StateC immutableCopy() {
    return new StateC(startingDateTime, elapsedMillis, nOfIterations, progress) {

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
