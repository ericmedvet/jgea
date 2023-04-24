/*
 * Copyright 2023 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jgea.sample.experimenter;

import io.github.ericmedvet.jgea.experimenter.Experimenter;
import io.github.ericmedvet.jgea.experimenter.InvertibleMapper;
import io.github.ericmedvet.jgea.problem.synthetic.Sphere;
import io.github.ericmedvet.jnb.core.NamedBuilder;
import io.github.ericmedvet.jnb.core.Param;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2022/11/24 for jgea
 */
public class NetStarter {

  public static class Builders {
    public static InvertibleMapper<List<Double>, List<Double>> fixed(@Param("n") int n) {
      return new InvertibleMapper<>() {
        @Override
        public List<Double> apply(List<Double> doubles) {
          if (doubles.size() != n) {
            throw new IllegalArgumentException("Wrong input size: %d found, %d expected".formatted(doubles.size(), n));
          }
          return doubles;
        }

        @Override
        public List<Double> exampleInput() {
          return Arrays.stream((new double[n])).boxed().toList();
        }
      };
    }

    public static Function<List<Double>, Double> sphere() {
      return new Sphere().qualityFunction();
    }
  }

  public static void main(String[] args) {
    String expDesc = """
        ea.experiment(
          runs = (randomGenerator = (seed = [1:1:20]) * [ea.rg.defaultRG()]) *
            (solver = [
              ea.s.numGA(mapper = fixed(n = 100); nEval = 200000; nPop = 10000);
              ea.s.simpleES(mapper = fixed(n = 500); nEval = 100000; nPop = 10000)
            ]) * [
            ea.run(
              problem = ea.p.totalOrder(qFunction = sphere())
            )
          ];
          listeners = [
            ea.l.net(
              serverKeyFilePath = "/home/eric/Documenti/experiments/vsrs/server-key.txt";
              functions = [
                ea.nf.fitness(individual = ea.nf.best(); s = "%6.2f");
                ea.nf.hist(collection = ea.nf.each(map = ea.nf.fitness(); collection = ea.nf.all()));
                ea.nf.percentile(collection = ea.nf.each(map = ea.nf.fitness(); collection = ea.nf.all()); p = 0.75; s = "%6.2f")
              ];
              plots = [ea.plot.xyPlot(
                x = ea.nf.progress();
                y = ea.nf.fitness(individual = ea.nf.best());
                minX = 0;
                maxX = 1
              )]
            )
          ]
        )
        """;
    NamedBuilder<?> nb = NamedBuilder.empty()
        .and(NamedBuilder.fromUtilityClass(Builders.class));
    Experimenter experimenter = new Experimenter(nb, 1, 1);
    experimenter.run(expDesc);
  }
}
