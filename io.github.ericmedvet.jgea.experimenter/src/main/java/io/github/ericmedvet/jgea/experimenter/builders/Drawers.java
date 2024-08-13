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

import io.github.ericmedvet.jgea.experimenter.drawer.PolyominoDrawer;
import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import java.awt.*;
import java.util.Map;

@Discoverable(prefixTemplate = "ea.drawer|d")
public class Drawers {

  private Drawers() {}

  @SuppressWarnings("unused")
  @Cacheable
  public static PolyominoDrawer polyomino(
      @Param(value = "maxW", dI = 0) int maxW,
      @Param(value = "maxH", dI = 0) int maxH,
      @Param(value = "colors", dNPM = "ea.misc.map(entries=[])") Map<Character, Color> colors,
      @Param(value = "borderColor", dNPM = "ea.misc.colorByName(name=white)") Color borderColor) {
    return new PolyominoDrawer(new PolyominoDrawer.Configuration(
        maxW == 0 ? null : maxW,
        maxH == 0 ? null : maxH,
        colors.isEmpty() ? PolyominoDrawer.Configuration.DEFAULT.colors() : colors,
        borderColor,
        PolyominoDrawer.Configuration.DEFAULT.marginRate()));
  }
}
