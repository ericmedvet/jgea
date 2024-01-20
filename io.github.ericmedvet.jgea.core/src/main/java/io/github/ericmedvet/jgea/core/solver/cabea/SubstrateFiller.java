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
import java.util.function.Function;

public interface SubstrateFiller extends Function<Grid<Boolean>, Grid<Boolean>> {

  enum Predefined implements SubstrateFiller {
    EMPTY(g -> g),
    CONTOUR(new Contour()),
    H_HALVED(new HorizontalSections(2)),
    V_HALVED(new VerticalSections(2)),
    CROSS(new HorizontalSections(2).andThen(new VerticalSections(2))),
    CONTOUR_CROSS(new Contour().andThen(new HorizontalSections(2).andThen(new VerticalSections(2)))),
    TIC_TAC_TOE(new HorizontalSections(3).andThen(new VerticalSections(3)));
    private final Function<Grid<Boolean>, Grid<Boolean>> inner;

    Predefined(Function<Grid<Boolean>, Grid<Boolean>> inner) {
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
      return grid.map(
          (k, b) -> (k.x() == 0 || k.x() == grid.w() - 1 || k.y() == 0 || k.y() == grid.h() - 1) ? !b : b);
    }
  }

  record HorizontalSections(int n) implements SubstrateFiller {
    @Override
    public Grid<Boolean> apply(Grid<Boolean> grid) {
      return grid.map((k, b) -> ((k.y() + 1) % Math.ceil((double) grid.h() / (double) n)) == 0 ? !b : b);
    }
  }

  record VerticalSections(int n) implements SubstrateFiller {
    @Override
    public Grid<Boolean> apply(Grid<Boolean> grid) {
      return grid.map((k, b) -> ((k.x() + 1) % Math.ceil((double) grid.w() / (double) n)) == 0 ? !b : b);
    }
  }
}
