/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
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

import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.NumericTreeUtils;
import io.github.ericmedvet.jgea.experimenter.drawer.FormulaDrawer;
import io.github.ericmedvet.jgea.experimenter.drawer.PolyominoDrawer;
import io.github.ericmedvet.jgea.experimenter.drawer.TreeDrawer;
import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.Tree;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import java.awt.*;
import java.util.Map;

@Discoverable(prefixTemplate = "ea.drawer|d")
public class Drawers {

  private Drawers() {
  }

  @Cacheable
  public static Drawer<Tree<Element>> formula(
      @Param(value = "scale", dD = 1) double scale,
      @Param("simplify") boolean simplify
  ) {
    if (simplify) {
      return new FormulaDrawer(FormulaDrawer.Configuration.DEFAULT.scaled(scale))
          .on(NumericTreeUtils::simplify);
    }
    return new FormulaDrawer(FormulaDrawer.Configuration.DEFAULT.scaled(scale));
  }

  @Cacheable
  public static PolyominoDrawer polyomino(
      @Param(value = "maxW", dI = 0) int maxW,
      @Param(value = "maxH", dI = 0) int maxH,
      @Param("colors") Map<Character, Color> colors,
      @Param(value = "borderColor", dNPM = "ea.misc.colorByName(name=white)") Color borderColor
  ) {
    return new PolyominoDrawer(
        new PolyominoDrawer.Configuration(
            maxW == 0 ? null : maxW,
            maxH == 0 ? null : maxH,
            colors.isEmpty() ? PolyominoDrawer.Configuration.DEFAULT.colors() : colors,
            borderColor,
            PolyominoDrawer.Configuration.DEFAULT.marginRate()
        )
    );
  }

  @Cacheable
  public static TreeDrawer tree(
      @Param(value = "scale", dD = 1) double scale
  ) {
    return new TreeDrawer(TreeDrawer.Configuration.DEFAULT.scaled(scale));
  }
}