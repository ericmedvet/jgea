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
package io.github.ericmedvet.jgea.problem.control.maze;

import io.github.ericmedvet.jgea.problem.control.ControlProblem;
import io.github.ericmedvet.jgea.problem.control.OutcomeViewer;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageViewer implements OutcomeViewer<MazeNavigation.Snapshot, BufferedImage> {
  private final int w;
  private final int h;
  private final Configuration configuration;

  public record Configuration(
      Color robotColor,
      Color targetColor,
      Color segmentColor,
      Color infoColor,
      Color trajectoryColor,
      double robotThickness,
      double targetThickness,
      double segmentThickness,
      double targetSize,
      double marginRate) {
    public static final Configuration DEFAULT =
        new Configuration(Color.MAGENTA, Color.RED, Color.DARK_GRAY, Color.BLUE, Color.CYAN, 1, 5, 1, 1, 0.1);
  }

  public ImageViewer(int w, int h, Configuration configuration) {
    this.w = w;
    this.h = h;
    this.configuration = configuration;
  }

  @Override
  public BufferedImage apply(ControlProblem.Outcome<MazeNavigation.Snapshot, ?> snapshotOutcome) {
    return null;
  }
}
