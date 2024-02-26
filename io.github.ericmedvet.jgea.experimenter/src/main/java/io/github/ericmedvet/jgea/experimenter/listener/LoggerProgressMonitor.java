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

package io.github.ericmedvet.jgea.experimenter.listener;

import io.github.ericmedvet.jgea.core.listener.ProgressMonitor;
import io.github.ericmedvet.jgea.core.util.Progress;
import io.github.ericmedvet.jgea.core.util.TextPlotter;
import java.util.logging.Logger;

public class LoggerProgressMonitor implements ProgressMonitor {

  private static final Logger L = Logger.getLogger(LoggerProgressMonitor.class.getName());

  @Override
  public void notify(Progress progress, String message) {
    L.info(String.format("Progress: %s %s%n", TextPlotter.horizontalBar(progress.rate(), 0, 1, 8), message));
  }
}
