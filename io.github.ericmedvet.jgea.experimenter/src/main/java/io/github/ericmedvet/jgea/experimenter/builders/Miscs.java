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

import io.github.ericmedvet.jgea.core.distance.Distance;
import io.github.ericmedvet.jgea.core.distance.LNorm;
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.representation.tree.bool.TreeBasedBooleanFunction;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element.Variable;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.TreeBasedMultivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.TreeBasedUnivariateRealFunction;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.bi.AbstractBiEvolver;
import io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.experimenter.drawer.DoubleGridDrawer;
import io.github.ericmedvet.jgea.problem.bool.synthetic.EvenParity;
import io.github.ericmedvet.jgea.problem.bool.synthetic.MultipleOutputParallelMultiplier;
import io.github.ericmedvet.jgea.problem.ca.MultivariateRealGridCellularAutomaton;
import io.github.ericmedvet.jgea.problem.image.ImageUtils;
import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.Pair;
import io.github.ericmedvet.jsdynsym.control.Simulation;
import io.github.ericmedvet.jsdynsym.control.SimulationOutcomeDrawer;
import io.github.ericmedvet.jsdynsym.core.bool.BooleanFunction;
import io.github.ericmedvet.jsdynsym.core.numerical.named.NamedMultivariateRealFunction;
import io.github.ericmedvet.jsdynsym.core.numerical.named.NamedUnivariateRealFunction;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.drawer.VideoBuilder;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Discoverable(prefixTemplate = "ea.misc")
public class Miscs {

  private Miscs() {
  }

  @Cacheable
  public static <G, S, Q, O> AbstractBiEvolver.OpponentsSelector<Individual<G, S, Q>, S, Q, O> bestSelector(
      @Param(value = "name", iS = "best[{nOfOpponents}]") String name,
      @Param(value = "nOfOpponents", dI = 1) int nOfOpponents
  ) {
    return (population, individual, problem, random) -> {
      Collection<Individual<G, S, Q>> evaluatedPopulation = population.stream()
          .filter(i -> i.quality() != null)
          .toList();
      Collection<Individual<G, S, Q>> nullQualityPopulation = population.stream()
          .filter(i -> i.quality() == null)
          .toList();
      PartialComparator<Individual<G, S, Q>> partialComparator = (me1, me2) -> problem.qualityComparator()
          .compare(me1.quality(), me2.quality());
      List<Collection<Individual<G, S, Q>>> fronts = new ArrayList<>(
          PartiallyOrderedCollection
              .from(evaluatedPopulation, partialComparator)
              .fronts()
      );
      if (!nullQualityPopulation.isEmpty()) {
        fronts.add(nullQualityPopulation);
      }
      List<Individual<G, S, Q>> opponents = new ArrayList<>();
      for (Collection<Individual<G, S, Q>> front : fronts) {
        for (Individual<G, S, Q> individualFromFront : front) {
          if (opponents.size() >= nOfOpponents) {
            break;
          }
          opponents.add(individualFromFront);
        }
        if (opponents.size() >= nOfOpponents) {
          break;
        }
      }
      return opponents;
    };
  }

  @Cacheable
  public static BooleanFunction bf(
      @Param("exprs") List<String> exprs
  ) {
    return new TreeBasedBooleanFunction(
        exprs.stream()
            .map(e -> io.github.ericmedvet.jgea.core.representation.tree.bool.Element.stringParser(true).parse(e))
            .toList()
    );
  }

  public static VideoBuilder<MultivariateRealGridCellularAutomaton> caVideo(
      @Param(value = "gray", dB = true) boolean gray,
      @Param(value = "caStateRange", dNPM = "m.range(min=-1;max=1)") DoubleRange caStateRange,
      @Param(value = "nOfSteps", dI = 100) int nOfSteps,
      @Param(value = "sizeRate", dI = 10) int sizeRate,
      @Param(value = "marginRate", dD = 0d) double marginRate,
      @Param(value = "frameRate", dD = 10d) double frameRate,
      @Param(value = "fontSize", dD = 10d) double fontSize
  ) {
    DoubleGridDrawer gDrawer = new DoubleGridDrawer(
        new DoubleGridDrawer.Configuration(
            gray ? DoubleGridDrawer.Configuration.ColorType.GRAY : DoubleGridDrawer.Configuration.ColorType.RGB,
            caStateRange,
            sizeRate,
            marginRate
        )
    );
    Drawer<Pair<Integer, Grid<double[]>>> pDrawer = new Drawer<>() {
      @Override
      public void draw(Graphics2D g, Pair<Integer, Grid<double[]>> p) {
        gDrawer.draw(g, p.second());
        Drawer.stringWriter(null, Color.PINK, (float) fontSize, Function.identity())
            .draw(g, "k=%3d".formatted(p.first()));
      }

      @Override
      public ImageInfo imageInfo(Pair<Integer, Grid<double[]>> p) {
        return gDrawer.imageInfo(p.second());
      }
    };
    return VideoBuilder.from(pDrawer, Function.identity(), frameRate).on(ca -> {
      List<Grid<double[]>> seq = ca.evolve(nOfSteps);
      return IntStream.range(0, seq.size())
          .mapToObj(i -> new Pair<>(i, seq.get(i)))
          .toList();
    });
  }

  @Cacheable
  public static Character ch(@Param("s") String s) {
    return s.charAt(0);
  }

  @Cacheable
  public static Color colorByName(@Param("name") String name) {
    try {
      return (Color) Color.class.getField(name.toUpperCase()).get(null);
    } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException |
             SecurityException e) {
      throw new RuntimeException(e);
    }
  }

  @Cacheable
  public static Color colorByRgb(@Param("r") int r, @Param("g") int g, @Param("b") int b) {
    return new Color(r, g, b);
  }

  @Cacheable
  public static BooleanFunction evenParityBf(
      @Param("n") int n
  ) {
    return EvenParity.booleanFunction(n);
  }

  @Cacheable
  public static <G, S, Q, O> AbstractBiEvolver.OpponentsSelector<MEIndividual<G, S, Q>, S, Q, O> farthestMESelector(
      @Param(value = "name", iS = "farthest[{nOfOpponents}]") String name,
      @Param(value = "nOfOpponents", dI = 1) int nOfOpponents
  ) {
    return (population, individual, problem, random) -> {
      Distance<List<Double>> d = new LNorm(2);
      return population.stream()
          .sorted(
              Comparator.comparingDouble(
                  candidate -> -d.apply(
                      individual.descriptorValues(),
                      candidate.descriptorValues()
                  )
              )
          )
          .limit(nOfOpponents)
          .collect(Collectors.toList());
    };
  }

  @Cacheable
  public static <X> X fromTextFile(
      @Param("path") String filePath,
      @Param(value = "f", dNPM = "f.identity()") Function<String, X> f
  ) {
    try {
      String s = Files.readString(Path.of(filePath));
      return f.apply(s);
    } catch (IOException e) {
      throw new RuntimeException("Cannot read file", e);
    }
  }

  @Cacheable
  public static BufferedImage imgByName(
      @Param("name") String name,
      @Param(value = "gateBGColor", dNPM = "ea.misc.colorByName(name = black)") Color bgColor,
      @Param(value = "w", dI = 15) int w,
      @Param(value = "h", dI = 15) int h,
      @Param(value = "marginRate", dD = 0.1) double marginRate
  ) {
    return ImageUtils.imageDrawer(bgColor, marginRate)
        .buildRaster(new Drawer.ImageInfo(w, h), ImageUtils.loadFromResource(name));
  }

  @Cacheable
  public static BufferedImage imgFromString(
      @Param("s") String s,
      @Param(value = "borderColor", dNPM = "ea.misc.colorByName(name = white)") Color fgColor,
      @Param(value = "gateBGColor", dNPM = "ea.misc.colorByName(name = black)") Color bgColor,
      @Param(value = "w", dI = 159) int w,
      @Param(value = "h", dI = 15) int h,
      @Param(value = "marginRate", dD = 0.1) double marginRate
  ) {
    return ImageUtils.stringDrawer(fgColor, bgColor, marginRate)
        .buildRaster(new Drawer.ImageInfo(w, h), s);
  }

  @Cacheable
  public static BinaryOperator<Double> lossyAverage(
      @Param(value = "memoryFactor", dD = 0.5) double memoryFactor
  ) {
    return (q1, q2) -> q1 * memoryFactor + (1 - memoryFactor) * q2;
  }

  @Cacheable
  public static BinaryOperator<Double> minValue() {
    return Math::min;
  }

  @Cacheable
  public static BooleanFunction mopmBf(
      @Param("n") int n
  ) {
    return MultipleOutputParallelMultiplier.booleanFunction(n);
  }

  @Cacheable
  public static <G, S, Q, O> AbstractBiEvolver.OpponentsSelector<MEIndividual<G, S, Q>, S, Q, O> nearestMESelector(
      @Param(value = "name", iS = "nearest[{nOfOpponents}]") String name,
      @Param(value = "nOfOpponents", dI = 1) int nOfOpponents
  ) {
    return (population, individual, problem, random) -> {
      Distance<List<Double>> d = new LNorm(2);
      return population.stream()
          .filter(candidate -> !candidate.equals(individual))
          .sorted(
              Comparator.comparingDouble(
                  candidate -> d.apply(
                      individual.descriptorValues(),
                      candidate.descriptorValues()
                  )
              )
          )
          .limit(nOfOpponents)
          .collect(Collectors.toList());
    };
  }

  @Cacheable
  public static NamedMultivariateRealFunction nmrf(
      @Param("exprs") List<String> exprs,
      @Param("yVarNames") List<String> yNames
  ) {
    if (exprs.size() != yNames.size()) {
      throw new IllegalArgumentException(
          "Different number of trees and y names: %d != %d".formatted(
              exprs.size(),
              yNames.size()
          )
      );
    }
    return new TreeBasedMultivariateRealFunction(
        exprs.stream().map(e -> Element.stringParser(true).parse(e)).toList(),
        yNames,
        false
    );
  }

  @Cacheable
  public static <S> NamedUnivariateRealFunction nurf(
      @Param("expr") String expr,
      @Param(value = "yVarName", dS = "y") String yVarName,
      @Param("additionalVars") List<String> additionalVars,
      @Param(value = "postOperator", dNPM = "ds.f.doubleOp(activationF=identity)") Function<Double, Double> postOperator,
      @Param("simplify") boolean simplify
  ) {
    Tree<Element> tree = Element.stringParser(true).parse(expr);
    return new TreeBasedUnivariateRealFunction(
        tree,
        Stream.concat(
            tree.leaves()
                .stream()
                .filter(t -> t.content() instanceof Variable)
                .map(t -> ((Variable) t.content()).toString()),
            additionalVars.stream()
        ).distinct().sorted(String::compareTo).toList(),
        yVarName,
        Mappers.toOperator(postOperator),
        simplify
    );
  }

  @Cacheable
  public static <G, S, Q, O> AbstractBiEvolver.OpponentsSelector<Individual<G, S, Q>, S, Q, O> oldestSelector(
      @Param(value = "name", iS = "oldest[{nOfOpponents}]") String name,
      @Param(value = "nOfOpponents", dI = 1) int nOfOpponents
  ) {
    return (population, individual, problem, random) -> population.stream()
        .sorted(Comparator.comparingLong(Individual::genotypeBirthIteration))
        .limit(nOfOpponents)
        .collect(Collectors.toList());
  }

  @Cacheable
  public static <G, S, Q, O> AbstractBiEvolver.OpponentsSelector<Individual<G, S, Q>, S, Q, O> randomSelector(
      @Param(value = "name", iS = "random[{nOfOpponents}]") String name,
      @Param(value = "nOfOpponents", dI = 1) int nOfOpponents
  ) {
    return (population, individual, problem, random) -> IntStream.range(0, nOfOpponents)
        .mapToObj(j -> Misc.pickRandomly(population, random))
        .toList();
  }

  @Cacheable
  public static <V> Map.Entry<String, V> sEntry(@Param("key") String key, @Param("value") V value) {
    return Map.entry(key, value);
  }

  @Cacheable
  public static <S> VideoBuilder<Simulation.Outcome<S>> toVideo(
      @Param("drawer") SimulationOutcomeDrawer<S> drawer
  ) {
    return drawer.videoBuilder();
  }
}