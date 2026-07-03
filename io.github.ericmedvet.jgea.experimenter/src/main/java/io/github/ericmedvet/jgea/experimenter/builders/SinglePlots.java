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

import io.github.ericmedvet.jgea.core.InvertibleMapper;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jnb.core.*;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jviz.core.plot.accumulator.LandscapeSEPAF;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@Discoverable(prefixTemplate = "ea.plot.single|s")
@Alias(
    name = "fieldRun", value = // spotless:off
    """
        viz.plot.single.field(
          title = f.interpolated(name = title; s = "{solver.name} on {problem.name} (seed={randomGenerator.seed})");
          predicateValue = f.quantized(of = ea.f.rate(of = ea.f.progress()); q = 0.05; format = "%.2f");
          condition = predicate.inD(values = [0; 0.1; 0.25; 0.50; 1])
        )
        """) // spotless:on
@Alias(
    name = "coMeStrategies", value = // spotless:off
    """
        ea.plot.single.fieldRun(
          title = f.interpolated(name = title; s = "Strategies (2D fields) of {solver.name} on {problem.name} (seed={runrandomGenerator.seed})");
          fields = [ea.f.coMeStrategy1Field(); ea.f.coMeStrategy2Field()];
          pointPairs = [f.identity()]
        )
        """) // spotless:on
// TODO add heatRun
@Alias(
    name = "gridRun", value = // spotless:off
    """
        viz.plot.single.grid(
          title = f.interpolated(name = title; s = "{solver.name} on {problem.name} (seed={randomGenerator.seed})");
          predicateValue = f.quantized(of = ea.f.rate(of = ea.f.progress()); q = 0.05; format = "%.2f");
          condition = predicate.inD(values = [0; 0.1; 0.25; 0.50; 1])
        )
        """) // spotless:on
@Alias( // TODO replace gridRun with heatRun
    name = "me", passThroughParams = {@PassThroughParam(name = "q", value = "f.identity()", type = ParamMap.Type.NAMED_PARAM_MAP)
    }, value = // spotless:off
    """
        ea.plot.single.gridRun(
          title = f.interpolated(name = title; s = "Archive of {solver.name} on {problem.name} (seed={randomGenerator.seed})");
          values = [f.composition(of = ea.f.quality(); then = $q)];
          grids = [ea.f.archiveToGrid(of = ea.f.meArchive())]
        )
        """) // spotless:on
@Alias( // TODO replace gridRun with heatRun
    name = "coMe", passThroughParams = {@PassThroughParam(name = "q", value = "f.identity()", type = ParamMap.Type.NAMED_PARAM_MAP)
    }, value = // spotless:off
    """
        ea.plot.single.gridRun(
          title = f.interpolated(name = title; s = "Archives of {solver.name} on {problem.name} (seed={randomGenerator.seed})");
          values = [f.composition(of = ea.f.quality(); then = $q)];
          grids = [ea.f.archiveToGrid(of = ea.f.coMeArchive1()); ea.f.archiveToGrid(of = ea.f.coMeArchive2())]
        )
        """) // spotless:on
@Alias( // TODO replace gridRun with heatRun
    name = "maMe2", passThroughParams = {@PassThroughParam(name = "q", value = "f.identity()", type = ParamMap.Type.NAMED_PARAM_MAP)
    }, value = // spotless:off
    """
        ea.plot.single.gridRun(
          title = f.interpolated(name = title; s = "Archives of {solver.name} on {problem.name} (seed={randomGenerator.seed})");
          values = [f.composition(of = ea.f.quality(); then = $q)];
          grids = [ea.f.archiveToGrid(of = ea.f.maMeArchive(n = 0)); ea.f.archiveToGrid(of = ea.f.maMeArchive(n = 1))]
        )
        """) // spotless:on
@Alias(
    name = "gridState", passThroughParams = {@PassThroughParam(name = "q", value = "f.identity()", type = ParamMap.Type.NAMED_PARAM_MAP)
    }, value = // spotless:off
    """
        ea.plot.single.gridRun(
          title = f.interpolated(name = title; s = "Grid population of {solver.name} on {problem.name} (seed={randomGenerator.seed})");
          values = [f.composition(of = ea.f.quality(); then = $q)];
          grids = [ea.f.stateGrid()]
        )
        """) // spotless:on
@Alias(
    name = "biObjectivePopulation", value = // spotless:off
    """
        viz.plot.single.xyes(
          title = f.interpolated(name = title; s = "Fronts with {solver.name} on {problem.name} (seed={randomGenerator.seed})");
          x = f.nThMapValue(of = ea.f.quality(); n = 0);
          y = f.nThMapValue(of = ea.f.quality(); n = 1);
          points = [
            ea.f.firsts();
            ea.f.mids();
            ea.f.lasts()
          ];
          predicateValue = f.quantized(of = ea.f.rate(of = ea.f.progress()); q = 0.05; format = "%.2f");
          condition = predicate.inD(values = [0; 0.1; 0.25; 0.50; 1])
        )
        """) // spotless:on
@Alias(
    name = "populationValidation", passThroughParams = {@PassThroughParam(name = "q", value = "f.identity()", type = ParamMap.Type.NAMED_PARAM_MAP), @PassThroughParam(name = "v", type = ParamMap.Type.NAMED_PARAM_MAP)
    }, value = // spotless:off
    """
        viz.plot.single.xyes(
          title = f.interpolated(name = title; s = "Population validation of {solver.name} on {problem.name} (seed={randomGenerator.seed})");
          x = f.composition(of = ea.f.quality(); then = $q);
          y = f.composition(of = f.composition(of = ea.f.solution(); then = $v); then = $q);
          points = [ea.f.all()];
          predicateValue = f.quantized(of = ea.f.rate(of = ea.f.progress()); q = 0.05; format = "%.2f");
          condition = predicate.inD(values = [0; 0.1; 0.25; 0.50; 1])
        )
        """) // spotless:on
@Alias(
    name = "xyrsRun", value = // spotless:off
    """
        viz.plot.single.xyrs(
          title = f.interpolated(name = title; s = "{solver.name} on {problem.name} (seed={randomGenerator.seed})");
          x = ea.f.nOfEvals()
        )
        """) // spotless:on
@Alias(
    name = "quality", passThroughParams = {@PassThroughParam(name = "q", value = "f.identity()", type = ParamMap.Type.NAMED_PARAM_MAP)
    }, value = // spotless:off
    """
        ea.plot.single.xyrsRun(ys = [f.composition(of = ea.f.quality(of = ea.f.best()); then = $q)])
        """) // spotless:on
@Alias(
    name = "uniqueness", value = // spotless:off
    """
        ea.plot.single.xyrsRun(
          ys = [
            f.uniqueness(of = f.each(mapF = ea.f.genotype(); of = ea.f.all()));
            f.uniqueness(of = f.each(mapF = ea.f.solution(); of = ea.f.all()));
            f.uniqueness(of = f.each(mapF = ea.f.quality(); of = ea.f.all()))
          ]
        )
        """) // spotless:on
public class SinglePlots {

  private SinglePlots() {
  }

  @SuppressWarnings("unused")
  public static <X, Q, P extends QualityBasedProblem<S, Q>, S> LandscapeSEPAF<POCPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q, P>, Run<?, List<Double>, S, Q>, X, Individual<List<Double>, S, Q>> landscape(
      @Param(
          value = "title", dNPM = "f.interpolated(name=title;s=\"{solver.name} on {problem.name} (seed={randomGenerator" + ".seed})\")") Function<? super Run<?, List<Double>, S, Q>, String> titleFunction,
      @Param(
          value = "predicateValue", dNPM = "f.quantized(of=ea.f.rate(of=ea.f.progress());q=0.05;format=\"%.2f\")") Function<POCPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q, P>, X> predicateValueFunction,
      @Param(value = "condition", dNPM = "predicate.inD(values=[0;0.1;0.25;0.50;1])") Predicate<X> condition,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<List<Double>, S> mapper,
      @Param(value = "q", dNPM = "ea.m.identity()") Function<Q, Double> qFunction,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange,
      @Param(value = "xF", dNPM = "f.nTh(of=ea.f.genotype();n=0)") Function<Individual<List<Double>, S, Q>, Double> xF,
      @Param(value = "yF", dNPM = "f.nTh(of=ea.f.genotype();n=1)") Function<Individual<List<Double>, S, Q>, Double> yF,
      @Param(value = "valueRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange valueRange,
      @Param(value = "unique", dB = true) boolean unique
  ) {
    return new LandscapeSEPAF<>(
        titleFunction,
        predicateValueFunction,
        condition,
        unique,
        List.of(NamedFunction.from(s -> s.pocPopulation().all(), "all")),
        xF,
        yF,
        s -> (x, y) -> s.problem()
            .qualityFunction()
            .andThen(qFunction)
            .apply(
                mapper.mapperFor(
                    s.pocPopulation()
                        .all()
                        .iterator()
                        .next()
                        .solution()
                )
                    .apply(List.of(x, y))
            ),
        xRange,
        yRange,
        valueRange
    );
  }
}