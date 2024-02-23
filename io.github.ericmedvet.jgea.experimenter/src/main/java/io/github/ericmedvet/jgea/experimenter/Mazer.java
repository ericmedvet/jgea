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
package io.github.ericmedvet.jgea.experimenter;

import io.github.ericmedvet.jgea.experimenter.listener.plot.video.VideoUtils;
import io.github.ericmedvet.jgea.problem.control.ControlProblem;
import io.github.ericmedvet.jgea.problem.control.maze.*;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.core.StatelessSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalStatelessSystem;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.SortedMap;
import java.util.random.RandomGenerator;

/**
 * @author "Eric Medvet" on 2024/02/23 for jgea
 */
public class Mazer {
  public static void main(String[] args) throws IOException {
    MazeNavigation<StatelessSystem.State> mn = new MazeNavigation<>(
        new DoubleRange(0.4, 0.6),
        new DoubleRange(0.8, 0.8),
        new DoubleRange(-3d * Math.PI / 4d, -Math.PI / 4d),
        new DoubleRange(0.5, 0.5),
        new DoubleRange(0.15, 0.15),
        0.05,
        0.01,
        new DoubleRange(-Math.PI / 2d, Math.PI / 2d),
        5,
        0.5,
        true,
        0.1,
        60,
        Arena.Prepared.DECEPTIVE_MAZE.arena(),
        MazeNavigation.Metric.AVG_DISTANCE,
        new Random(1));
    RandomGenerator rg = new Random();
    NumericalStatelessSystem c =
        NumericalStatelessSystem.from(7, 2, (t, inputs) -> new double[] {.5d, .5d + rg.nextGaussian() * 0.1});
    SortedMap<Double, MazeNavigation.Snapshot> outcome = mn.simulate(c);
    MazeViewer ti = new MazeViewer(MazeViewer.Configuration.DEFAULT);
    BufferedImage img = ti.last(800, 600, new ControlProblem.Outcome<>(outcome, 0d));
    // ImagePlotter.showImage(img);
    VideoUtils.encodeAndSave(
        ti.all(300, 300, new ControlProblem.Outcome<>(outcome, 0d)),
        30,
        new File("../maze.mp4"),
        VideoUtils.EncoderFacility.FFMPEG_SMALL);
  }
}
