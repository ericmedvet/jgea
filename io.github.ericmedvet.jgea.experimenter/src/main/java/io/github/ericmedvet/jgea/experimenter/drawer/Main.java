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
package io.github.ericmedvet.jgea.experimenter.drawer;

import io.github.ericmedvet.jgea.core.representation.NamedMultivariateRealFunction;
import io.github.ericmedvet.jgea.problem.ca.MRCAMorphogenesis;
import io.github.ericmedvet.jgea.problem.ca.MultivariateRealGridCellularAutomaton;
import io.github.ericmedvet.jgea.problem.image.ImageUtils;
import io.github.ericmedvet.jnb.core.NamedBuilder;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jsdynsym.core.numerical.MultivariateRealFunction;
import io.github.ericmedvet.jsdynsym.core.numerical.ann.MultiLayerPerceptron;
import io.github.ericmedvet.jviz.core.drawer.ImageBuilder;
import io.github.ericmedvet.jviz.core.drawer.VideoBuilder;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.random.RandomGenerator;

public class Main {

  public static void main(String[] args) {

    NamedBuilder<Object> nb = NamedBuilder.fromDiscovery();
    MRCAMorphogenesis prob = (MRCAMorphogenesis)
        nb.build(
            """
ea.p.s.mrCaStringMorphogenesis(s = "🌶"; w = 15; h = 15; name = "+"; fromStep = 40; toStep = 60)
""");
    DoubleGridDrawer drawer = new DoubleGridDrawer(new DoubleGridDrawer.Configuration(
        DoubleGridDrawer.Configuration.ColorType.GRAY, DoubleRange.SYMMETRIC_UNIT, 20, 0));
    drawer.show(prob.getTargetGrid());
    ImageUtils.stringDrawer(Color.WHITE, Color.BLACK, 0.1).show(new ImageBuilder.ImageInfo(100, 100), "I");

    int stateSize = 5;
    int l = 15;
    int nOfSteps = 100;
    RandomGenerator rg = new Random();
    List<Grid<Double>> kernels = List.of(
        Grid.create(3, 3, List.of(-1d, 0d, +1d, -2d, 0d, +2d, -1d, 0d, +1d)),
        Grid.create(3, 3, List.of(-1d, -2d, -1d, 0d, 0d, 0d, +1d, +2d, +1d)),
        Grid.create(3, 3, List.of(0d, 0d, 0d, 0d, 1d, 0d, 0d, 0d, 0d)));
    MultiLayerPerceptron mlp = new MultiLayerPerceptron(
        MultiLayerPerceptron.ActivationFunction.SIGMOID, stateSize * kernels.size(), new int[] {10}, stateSize);
    mlp.randomize(rg, DoubleRange.SYMMETRIC_UNIT);
    MultivariateRealGridCellularAutomaton ca = new MultivariateRealGridCellularAutomaton(
        Grid.create(l, l, (x, y) -> {
          if (x == l / 2 && y == l / 2) {
            double[] v = new double[stateSize];
            v[0] = 1;
            return v;
          }
          return new double[stateSize];
        }),
        DoubleRange.SYMMETRIC_UNIT,
        kernels,
        NamedMultivariateRealFunction.from(
            mlp,
            MultivariateRealFunction.varNames("c", mlp.nOfInputs()),
            MultivariateRealFunction.varNames("c", mlp.nOfOutputs())),
        0.1,
        0d,
        true);
    VideoBuilder<List<Grid<double[]>>> vb = VideoBuilder.from(drawer, Function.identity(), 10);
    List<Grid<double[]>> caEvolution = ca.evolve(nOfSteps);
    vb.save(new File("../ca.mp4"), caEvolution);
  }
}
