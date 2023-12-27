/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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
package io.github.ericmedvet.jgea.experimenter.listener.plot;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;

/**
 * @author "Eric Medvet" on 2023/12/27 for jgea
 */
public interface RangedGrid<T> extends Grid<T> {
  DoubleRange xRange();

  DoubleRange yRange();

  default DoubleRange xRange(int x) {
    return new DoubleRange(
        xRange().denormalize(new DoubleRange(0, w()).normalize(x)),
        xRange().denormalize(new DoubleRange(0, w()).normalize(x + 1)));
  }

  default DoubleRange yRange(int y) {
    return new DoubleRange(
        yRange().denormalize(new DoubleRange(0, h()).normalize(y)),
        yRange().denormalize(new DoubleRange(0, h()).normalize(y + 1)));
  }

  static <T> RangedGrid<T> from(Grid<T> grid, DoubleRange xRange, DoubleRange yRange) {
    return new RangedGrid<T>() {
      @Override
      public DoubleRange xRange() {
        return xRange;
      }

      @Override
      public DoubleRange yRange() {
        return yRange;
      }

      @Override
      public T get(Key key) {
        return grid.get(key);
      }

      @Override
      public int h() {
        return grid.h();
      }

      @Override
      public void set(Key key, T t) {
        grid.set(key, t);
      }

      @Override
      public int w() {
        return grid.w();
      }
    };
  }
}
