/*
 * Copyright 2022 eric
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

package it.units.malelab.jgea.sample.experimenter;

import it.units.malelab.jgea.experimenter.Experimenter;
import it.units.malelab.jgea.experimenter.InvertibleMapper;
import it.units.malelab.jgea.problem.synthetic.Sphere;
import it.units.malelab.jnb.core.NamedBuilder;
import it.units.malelab.jnb.core.Param;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2022/11/24 for jgea
 */
public class Starter {

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
          runs = [
            ea.run(
              solver = ea.s.numGA(
                mapper = fixed(n = 10);
                nEval = 100000
              );
              randomGenerator = ea.rg.defaultRG(seed = 1);
              problem = ea.p.totalOrder(
                qFunction = sphere();
                cExtractor = ea.f.identity()
              )
            )
          ];
          listeners = [
            ea.l.tui(
              functions = [
                ea.nf.fitness(individual = ea.nf.best(); s = "%6.2f");
                ea.nf.hist(collection = ea.nf.each(map = ea.nf.fitness(); collection = ea.nf.all()));
                ea.nf.percentile(collection = ea.nf.each(map = ea.nf.fitness(); collection = ea.nf.all()); p = 0.75; s = "%6.2f")
              ];
              runKeys = ["randomGenerator.seed"; "solver.mapper.n"];
              plots = [ea.plot.fitness()]
            );
            ea.l.telegram(
              chatId = "207490209";
              botIdFilePath = "/home/eric/experiments/2dmrsim/tlg.txt";
              plots = [ea.plot.fitness()]
            );
            ea.l.bestCsv(
              filePath = "/home/eric/experiments/2dmrsim/new-res.txt";
              functions = [
                ea.nf.fitness(individual = ea.nf.best(); s = "%6.2f")
              ]
            )
          ]
        )
        """;
    NamedBuilder<?> nb = NamedBuilder.empty()
        .and(NamedBuilder.fromUtilityClass(Builders.class));
    Experimenter experimenter = new Experimenter(nb, 2);
    experimenter.run(expDesc);
  }
}
