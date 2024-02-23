/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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
package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.problem.control.maze.OutcomeDrawer;
import io.github.ericmedvet.jnb.core.Discoverable;

/**
 * @author "Eric Medvet" on 2024/02/23 for jgea
 */
@Discoverable(prefixTemplate = "ea.drawer|d")
public class Drawers {
  private Drawers() {}

  @SuppressWarnings("unused")
  public static OutcomeDrawer navigationOutcome() {
    return new OutcomeDrawer(OutcomeDrawer.Configuration.DEFAULT);
  }
}
