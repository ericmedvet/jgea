/*-
 * ========================LICENSE_START=================================
 * jgea-problem
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
package io.github.ericmedvet.jgea.problem.control;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.SortedMap;

public interface OutcomeViewer<S> {
  default BufferedImage last(int w, int h, ControlProblem.Outcome<S, ?> outcome) {
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g = prepare(img);
    drawHistory(g, w, h, outcome.behavior());
    drawOne(
        g,
        w,
        h,
        outcome.behavior().lastKey(),
        outcome.behavior().get(outcome.behavior().lastKey()));
    g.dispose();
    return img;
  }

  default List<BufferedImage> all(int w, int h, ControlProblem.Outcome<S, ?> outcome) {
    return outcome.behavior().entrySet().stream()
        .map(e -> {
          BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
          Graphics2D g = prepare(img);
          drawOne(g, w, h, e.getKey(), e.getValue());
          g.dispose();
          return img;
        })
        .toList();
  }

  Graphics2D prepare(BufferedImage image);

  void drawOne(Graphics2D g, int w, int h, double t, S snapshot);

  void drawHistory(Graphics2D g, int w, int h, SortedMap<Double, S> snapshots);
}
