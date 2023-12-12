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
package io.github.ericmedvet.jgea.core.solver.cabea;

import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.util.function.UnaryOperator;

public interface SubstrateFiller extends UnaryOperator<Grid<Boolean>> {

  enum Predefined implements SubstrateFiller {
    EMPTY(g -> g),
    CONTOUR(new Contour());
    private final SubstrateFiller inner;

    Predefined(SubstrateFiller inner) {
      this.inner = inner;
    }

    @Override
    public Grid<Boolean> apply(Grid<Boolean> grid) {
      return inner.apply(grid);
    }
  }

  class Contour implements SubstrateFiller {
    @Override
    public Grid<Boolean> apply(Grid<Boolean> grid) {
      return grid.map((k, b) -> k.x() != 0 && k.x() != grid.w() - 1 && k.y() != 0 && k.y() != grid.h() - 1 && b);
    }
  }

  record HorizontalSections(int n) implements SubstrateFiller {
    @Override
    public Grid<Boolean> apply(Grid<Boolean> grid) {
      return null;
    }
  }
}
