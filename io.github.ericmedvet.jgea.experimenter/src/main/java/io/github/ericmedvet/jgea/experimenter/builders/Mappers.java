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

package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.InvertibleMapper;
import io.github.ericmedvet.jgea.core.representation.NamedMultivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.NamedUnivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.grammar.Chooser;
import io.github.ericmedvet.jgea.core.representation.grammar.Developer;
import io.github.ericmedvet.jgea.core.representation.grammar.grid.*;
import io.github.ericmedvet.jgea.core.representation.graph.Graph;
import io.github.ericmedvet.jgea.core.representation.graph.Node;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.functiongraph.FunctionGraph;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.OperatorGraph;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.TreeBasedMultivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.TreeBasedUnivariateRealFunction;
import io.github.ericmedvet.jgea.core.util.Naming;
import io.github.ericmedvet.jgea.problem.ca.MultivariateRealGridCellularAutomaton;
import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.NumericalParametrized;
import io.github.ericmedvet.jnb.datastructure.Pair;
import io.github.ericmedvet.jsdynsym.buildable.builders.NumericalDynamicalSystems;
import io.github.ericmedvet.jsdynsym.core.composed.Stepped;
import io.github.ericmedvet.jsdynsym.core.numerical.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Discoverable(prefixTemplate = "ea.mapper|m")
public class Mappers {
  private Mappers() {}

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, T> InvertibleMapper<X, Grid<T>> bsToGrammarGrid(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, BitString> beforeM,
      @Param("grammar") GridGrammar<T> grammar,
      @Param(value = "l", dI = 256) int l,
      @Param(value = "overwrite") boolean overwrite,
      @Param(
              value = "criteria",
              dSs = {"least_recent", "lowest_y", "lowest_x"})
          List<StandardGridDeveloper.SortingCriterion> criteria) {
    Developer<T, Grid<T>, GridGrammar.ReferencedGrid<T>> gridDeveloper =
        new StandardGridDeveloper<>(grammar, overwrite, criteria);
    return beforeM.andThen(InvertibleMapper.from(
        (eGrid, bs) -> {
          Chooser<T, GridGrammar.ReferencedGrid<T>> chooser = new BitStringChooser<>(bs, grammar);
          return gridDeveloper.develop(chooser).orElse(eGrid);
        },
        eGrid -> new BitString(l),
        "bsToGrammarGrid[l=%d;o=%s;c=%s]".formatted(l, overwrite, criteria)));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> InvertibleMapper<X, Pair<List<Double>, List<Double>>> dsSplit(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, List<Double>> beforeM) {
    return beforeM.andThen(InvertibleMapper.from(
        (p, ds) -> {
          if (p.first().size() + p.second().size() != ds.size()) {
            throw new IllegalArgumentException(
                "Cannot split a double string with size %d in two double strings of sizes %d and %d"
                    .formatted(
                        ds.size(),
                        p.first().size(),
                        p.second().size()));
          }
          return new Pair<>(
              ds.subList(0, p.first().size()),
              ds.subList(p.first().size(), ds.size()));
        },
        p -> Stream.concat(p.first().stream(), p.second().stream()).toList(),
        "dsSplit"));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> InvertibleMapper<X, BitString> dsToBitString(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, List<Double>> beforeM,
      @Param(value = "t", dD = 0d) double t) {
    return beforeM.andThen(InvertibleMapper.from(
        (eBs, ds) -> new BitString(ds.stream().map(v -> v < t).toList()),
        eBs -> Collections.nCopies(eBs.size(), 0d),
        "dsToBs[t=%.1f]".formatted(t)));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, T> InvertibleMapper<X, Grid<T>> dsToFixedGrid(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, List<Double>> beforeM,
      @Param(value = "rate", dD = 0.25) double rate,
      @Param("negItem") T negItem,
      @Param("posItem") T posItem) {
    return beforeM.andThen(InvertibleMapper.from(
        (g, ds) -> {
          if (ds.size() != g.w() * g.h()) {
            throw new IllegalArgumentException(
                "Wrong size for the double string: %dx%d=%d expected, %d found"
                    .formatted(g.w(), g.h(), g.w() * g.h(), ds.size()));
          }
          List<Integer> indexes = IntStream.range(0, ds.size())
              .boxed()
              .sorted(Comparator.comparingDouble(ds::get))
              .limit((long) (ds.size() * rate))
              .toList();
          return Grid.create(
              g.w(),
              g.h(),
              IntStream.range(0, ds.size())
                  .boxed()
                  .map(i -> indexes.contains(i) ? posItem : negItem)
                  .toList());
        },
        g -> Collections.nCopies(g.w() * g.h(), 0d),
        "dsToThresholdedGrid[rate=%.2f]".formatted(rate)));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, T> InvertibleMapper<X, Grid<T>> dsToGrammarGrid(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, List<Double>> beforeM,
      @Param("grammar") GridGrammar<T> grammar,
      @Param(value = "l", dI = 256) int l,
      @Param(value = "overwrite") boolean overwrite,
      @Param(
              value = "criteria",
              dSs = {"least_recent", "lowest_y", "lowest_x"})
          List<StandardGridDeveloper.SortingCriterion> criteria) {
    Developer<T, Grid<T>, GridGrammar.ReferencedGrid<T>> gridDeveloper =
        new StandardGridDeveloper<>(grammar, overwrite, criteria);
    return beforeM.andThen(InvertibleMapper.from(
        (eGrid, vs) -> {
          Chooser<T, GridGrammar.ReferencedGrid<T>> chooser = new DoublesChooser<>(vs, grammar);
          return gridDeveloper.develop(chooser).orElse(eGrid);
        },
        eGrid -> Collections.nCopies(l, 0d),
        "dsToGrammarGrid[l=%d;o=%s;c=%s]".formatted(l, overwrite, criteria)));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> InvertibleMapper<X, IntString> dsToIs(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, List<Double>> beforeM,
      @Param(value = "range", dNPM = "ds.range(min=-1;max=1)") DoubleRange range) {
    return beforeM.andThen(InvertibleMapper.from(
        (eIs, ds) -> {
          DoubleRange isRange = new DoubleRange(eIs.lowerBound(), eIs.upperBound());
          return new IntString(
              ds.stream()
                  .map(v -> (int) Math.floor(isRange.denormalize(range.normalize(v))))
                  .map(i -> Math.max(Math.min(i, eIs.upperBound() - 1), eIs.lowerBound()))
                  .toList(),
              eIs.lowerBound(),
              eIs.upperBound());
        },
        eIs -> Collections.nCopies(eIs.size(), 0d),
        "dsToIs[min=%.0f;max=%.0f]".formatted(range.min(), range.max())));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, P extends NumericalDynamicalSystem<S> & NumericalParametrized<P>, S>
      InvertibleMapper<X, P> dsToNpnds(
          @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, List<Double>> beforeM,
          @Param("npnds") NumericalDynamicalSystems.Builder<P, S> builder) {
    return beforeM.andThen(InvertibleMapper.from(
        (p, params) -> builder.apply(p.nOfInputs(), p.nOfOutputs())
            .withParams(params.stream().mapToDouble(v -> v).toArray()),
        p -> Collections.nCopies(
            builder.apply(p.nOfInputs(), p.nOfOutputs()).getParams().length, 0d),
        "dsToNpnds[npnds=%s]".formatted(builder)));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, T> InvertibleMapper<X, Grid<T>> dsToThresholdedGrid(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, List<Double>> beforeM,
      @Param(value = "t", dD = 0) double t,
      @Param("negItem") T negItem,
      @Param("posItem") T posItem) {
    return beforeM.andThen(InvertibleMapper.from(
        (g, ds) -> {
          if (ds.size() != g.w() * g.h()) {
            throw new IllegalArgumentException(
                "Wrong size for the double string: %dx%d=%d expected, %d found"
                    .formatted(g.w(), g.h(), g.w() * g.h(), ds.size()));
          }
          return Grid.create(
              g.w(),
              g.h(),
              ds.stream().map(d -> d > t ? posItem : negItem).toList());
        },
        g -> Collections.nCopies(g.w() * g.h(), 0d),
        "dsToThresholdedGrid[t=%.2f]".formatted(t)));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> InvertibleMapper<X, NumericalDynamicalSystem<?>> enhancedNds(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NumericalDynamicalSystem<?>> beforeM,
      @Param("windowT") double windowT,
      @Param(
              value = "types",
              dSs = {"current", "trend", "avg"})
          List<EnhancedInput.Type> types) {
    return beforeM.andThen(InvertibleMapper.from(
        (eNds, nds) -> new EnhancedInput<>(nds, windowT, types),
        eNds -> eNds,
        "enhanced[wT=%.2f;%s]"
            .formatted(windowT, types.stream().map(Enum::toString).collect(Collectors.joining(";")))));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> InvertibleMapper<X, NamedMultivariateRealFunction> fGraphToNmrf(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, Graph<Node, Double>> beforeM,
      @Param(value = "postOperator", dNPM = "ds.f.doubleOp(activationF=identity)")
          Function<Double, Double> postOperator) {
    return beforeM.andThen(InvertibleMapper.from(
        (nmrf, g) -> NamedMultivariateRealFunction.from(
                new FunctionGraph(g, nmrf.xVarNames(), nmrf.yVarNames()),
                nmrf.xVarNames(),
                nmrf.yVarNames())
            .andThen(toOperator(postOperator)),
        nmrf -> FunctionGraph.sampleFor(nmrf.xVarNames(), nmrf.yVarNames()),
        "fGraphToNmrf[po=%s]".formatted(postOperator)));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> InvertibleMapper<X, X> identity() {
    return InvertibleMapper.identity();
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, T> InvertibleMapper<X, Grid<T>> isToGrammarGrid(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, IntString> beforeM,
      @Param("grammar") GridGrammar<T> grammar,
      @Param(value = "upperBound", dI = 16) int upperBound,
      @Param(value = "l", dI = 256) int l,
      @Param(value = "overwrite") boolean overwrite,
      @Param(
              value = "criteria",
              dSs = {"least_recent", "lowest_y", "lowest_x"})
          List<StandardGridDeveloper.SortingCriterion> criteria) {
    Developer<T, Grid<T>, GridGrammar.ReferencedGrid<T>> gridDeveloper =
        new StandardGridDeveloper<>(grammar, overwrite, criteria);
    return beforeM.andThen(InvertibleMapper.from(
        (eGrid, is) -> {
          Chooser<T, GridGrammar.ReferencedGrid<T>> chooser = new IntStringChooser<>(is, grammar);
          return gridDeveloper.develop(chooser).orElse(eGrid);
        },
        eGrid -> new IntString(Collections.nCopies(l, 0), 0, upperBound),
        "isToGrammarGrid[l=%d;o=%s;c=%s]".formatted(l, overwrite, criteria)));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, T> InvertibleMapper<X, Grid<T>> isToGrid(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, IntString> beforeM,
      @Param("items") List<T> items) {
    return beforeM.andThen(InvertibleMapper.from(
        (g, is) -> {
          if (is.size() != g.w() * g.h()) {
            throw new IllegalArgumentException(
                "Wrong size for the integer string: %dx%d=%d expected, %d found"
                    .formatted(g.w(), g.h(), g.w() * g.h(), is.size()));
          }
          return Grid.create(
              g.w(), g.h(), is.genes().stream().map(items::get).toList());
        },
        g -> new IntString(Collections.nCopies(g.w() * g.h(), 0), 0, items.size()),
        "isToGrid[nOfItems=%d]".formatted(items.size())));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> InvertibleMapper<X, NamedMultivariateRealFunction> multiSrTreeToNmrf(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, List<Tree<Element>>> beforeM,
      @Param(value = "postOperator", dNPM = "ds.f.doubleOp(activationF=identity)")
          Function<Double, Double> postOperator) {
    return beforeM.andThen(InvertibleMapper.from(
        (nmrf, ts) -> new TreeBasedMultivariateRealFunction(ts, nmrf.xVarNames(), nmrf.yVarNames())
            .andThen(toOperator(postOperator)),
        nmrf -> TreeBasedMultivariateRealFunction.sampleFor(nmrf.xVarNames(), nmrf.yVarNames()),
        "multiSrTreeToNmrf[po=%s]".formatted(postOperator)));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, T> InvertibleMapper<X, Grid<T>> nmrfToGrid(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NamedMultivariateRealFunction> beforeM,
      @Param("items") List<T> items) {
    return beforeM.andThen(InvertibleMapper.from(
        (g, nmrf) -> {
          if (nmrf.nOfInputs() != 2) {
            throw new IllegalArgumentException(
                "Wrong input size for the NMRF: 2 expected, %d found".formatted(nmrf.nOfInputs()));
          }
          if (nmrf.nOfOutputs() != items.size()) {
            throw new IllegalArgumentException("Wrong output size for the NMRF: %d expected, %d found"
                .formatted(items.size(), nmrf.nOfOutputs()));
          }
          return Grid.create(g.w(), g.h(), (x, y) -> {
            double[] values =
                nmrf.apply(new double[] {(double) x / (double) g.w(), (double) y / (double) g.h()});
            return items.get(IntStream.range(0, values.length)
                .boxed()
                .min(Comparator.comparingDouble(i -> values[i]))
                .orElse(0));
          });
        },
        g -> NamedMultivariateRealFunction.from(
            MultivariateRealFunction.from(vs -> new double[items.size()], 2, items.size()),
            List.of("x", "y"),
            IntStream.range(0, items.size())
                .mapToObj("item%02d"::formatted)
                .toList()),
        "nmrfToGrid[nOfItems=%d]".formatted(items.size())));
  }

  @SuppressWarnings("unused")
  public static <X> InvertibleMapper<X, MultivariateRealGridCellularAutomaton> nmrfToMrca(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NamedMultivariateRealFunction> beforeM,
      @Param(value = "nOfAdditionalChannels", dI = 1) int nOfAdditionalChannels,
      @Param(value = "kernel", dS = "sobel_edges") MultivariateRealGridCellularAutomaton.Kernel kernel,
      @Param(value = "initializer", dS = "center_one")
          MultivariateRealGridCellularAutomaton.Initializer initializer,
      @Param(value = "range", dNPM = "m.range(min=-1;max=1)") DoubleRange range,
      @Param(value = "additiveCoefficient", dD = 1d) double additiveCoefficient,
      @Param(value = "toroidal", dB = false) boolean toroidal,
      @Param(value = "clipping", dB = true) boolean clipping) {
    return beforeM.andThen(InvertibleMapper.from(
        (mrca, nmrf) -> {
          int minStateSize = MultivariateRealGridCellularAutomaton.minStateSize(mrca.getInitialStates());
          int nOfInputs = (minStateSize + nOfAdditionalChannels)
              * kernel.get().size();
          int nOfOutputs = minStateSize + nOfAdditionalChannels;
          if (nmrf.nOfInputs() != nOfInputs) {
            throw new IllegalArgumentException("Wrong input size for the MRF: %d expected, %d found"
                .formatted(nOfInputs, nmrf.nOfInputs()));
          }
          if (nmrf.nOfOutputs() != nOfOutputs) {
            throw new IllegalArgumentException("Wrong output size for the MRF: %d expected, %d found"
                .formatted(nOfOutputs, nmrf.nOfOutputs()));
          }
          if (clipping) {
            nmrf = nmrf.andThen(range::clip);
          }
          return new MultivariateRealGridCellularAutomaton(
              initializer.initialize(
                  mrca.getInitialStates().w(),
                  mrca.getInitialStates().h(),
                  minStateSize + nOfAdditionalChannels,
                  range),
              kernel.get(),
              nmrf,
              additiveCoefficient,
              toroidal);
        },
        mrca -> {
          int minStateSize = MultivariateRealGridCellularAutomaton.minStateSize(mrca.getInitialStates());
          int nOfInputs = (minStateSize + nOfAdditionalChannels)
              * kernel.get().size();
          int nOfOutputs = minStateSize + nOfAdditionalChannels;
          return NamedMultivariateRealFunction.from(
              MultivariateRealFunction.from(vs -> new double[nOfOutputs], nOfInputs, nOfOutputs),
              MultivariateRealFunction.varNames("pc", nOfInputs),
              MultivariateRealFunction.varNames("c", nOfOutputs));
        },
        "nmrfToMrCA[addChannels=%d;kernel=%s]".formatted(nOfAdditionalChannels, kernel)));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> InvertibleMapper<X, NumericalDynamicalSystem<?>> nmrfToNds(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NamedMultivariateRealFunction> beforeM) {
    return beforeM.andThen(InvertibleMapper.from(
        (nds, nmrf) -> nmrf,
        nds -> NamedMultivariateRealFunction.from(
            MultivariateRealFunction.from(
                in -> new double[nds.nOfOutputs()], nds.nOfInputs(), nds.nOfOutputs()),
            MultivariateRealFunction.varNames("i", nds.nOfInputs()),
            MultivariateRealFunction.varNames("o", nds.nOfOutputs())),
        "nmrfToNds"));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> InvertibleMapper<X, NamedUnivariateRealFunction> nmrfToNurf(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NamedMultivariateRealFunction> beforeM) {
    return beforeM.andThen(InvertibleMapper.from(
        (nurf, nmrf) -> NamedUnivariateRealFunction.from(nmrf), nurf -> nurf, "nmrfToNurf"));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> InvertibleMapper<X, NumericalDynamicalSystem<?>> noisedNds(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NumericalDynamicalSystem<?>> beforeM,
      @Param(value = "inputSigma", dD = 0) double inputSigma,
      @Param(value = "outputSigma", dD = 0) double outputSigma,
      @Param(value = "randomGenerator", dNPM = "m.defaultRG()") RandomGenerator randomGenerator) {
    return beforeM.andThen(InvertibleMapper.from(
        (eNds, nds) -> new Noised<>(nds, inputSigma, outputSigma, randomGenerator),
        eNds -> eNds,
        "noised[in=%.2f;out=%.2f]".formatted(inputSigma, outputSigma)));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> InvertibleMapper<X, NamedMultivariateRealFunction> noisedNmrf(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NamedMultivariateRealFunction> beforeM,
      @Param(value = "sigma", dD = 0) double sigma,
      @Param(value = "randomGenerator", dNPM = "m.defaultRG()") RandomGenerator randomGenerator) {
    return beforeM.andThen(InvertibleMapper.from(
        (eNmrf, nmrf) -> nmrf.andThen(Naming.named("noised[out=%.2f]".formatted(sigma), (DoubleUnaryOperator)
            v -> v + randomGenerator.nextGaussian() * sigma)),
        eNmrf -> eNmrf,
        "noised[out=%.2f]".formatted(sigma)));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> InvertibleMapper<X, NamedMultivariateRealFunction> ntissToNmrf(
      @Param(value = "of", dNPM = "ea.m.identity()")
          InvertibleMapper<X, NumericalTimeInvariantStatelessSystem> beforeM) {
    return beforeM.andThen(InvertibleMapper.from(
        (nmrf, ntiss) -> NamedMultivariateRealFunction.from(
            MultivariateRealFunction.from(ntiss, nmrf.nOfInputs(), nmrf.nOfOutputs()),
            nmrf.xVarNames(),
            nmrf.yVarNames()),
        nmrf -> MultivariateRealFunction.from(
            v -> new double[nmrf.nOfOutputs()], nmrf.nOfInputs(), nmrf.nOfOutputs()),
        "ntissToNmrf"));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> InvertibleMapper<X, NamedMultivariateRealFunction> oGraphToNmrf(
      @Param(value = "of", dNPM = "ea.m.identity()")
          InvertibleMapper<X, Graph<Node, OperatorGraph.NonValuedArc>> beforeM,
      @Param(value = "postOperator", dNPM = "ds.f.doubleOp(activationF=identity)")
          Function<Double, Double> postOperator) {
    return beforeM.andThen(InvertibleMapper.from(
        (nmrf, g) -> new OperatorGraph(g, nmrf.xVarNames(), nmrf.yVarNames()).andThen(toOperator(postOperator)),
        nmrf -> OperatorGraph.sampleFor(nmrf.xVarNames(), nmrf.yVarNames()),
        "oGraphToNmrf[po=%s]".formatted(postOperator)));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, F1, S1, F2, S2> InvertibleMapper<X, Pair<F2, S2>> pair(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, Pair<F1, S1>> beforeM,
      @Param(value = "first", dNPM = "ea.m.identity()") InvertibleMapper<F1, F2> firstM,
      @Param(value = "second", dNPM = "ea.m.identity()") InvertibleMapper<S1, S2> secondM) {
    return beforeM.andThen(InvertibleMapper.from(
        (p2, p1) -> new Pair<>(
            firstM.mapperFor(p2.first()).apply(p1.first()),
            secondM.mapperFor(p2.second()).apply(p1.second())),
        p2 -> new Pair<>(firstM.exampleFor(p2.first()), secondM.exampleFor(p2.second())),
        "pair[first=%s;second=%s]".formatted(firstM, secondM)));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> InvertibleMapper<X, NamedUnivariateRealFunction> srTreeToNurf(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, Tree<Element>> beforeM,
      @Param(value = "postOperator", dNPM = "ds.f.doubleOp(activationF=identity)")
          Function<Double, Double> postOperator) {
    return beforeM.andThen(InvertibleMapper.from(
        (nurf, t) -> new TreeBasedUnivariateRealFunction(t, nurf.xVarNames(), nurf.yVarName())
            .andThen(toOperator(postOperator)),
        nurf -> TreeBasedUnivariateRealFunction.sampleFor(nurf.xVarNames(), nurf.yVarName()),
        "srTreeToNurf[po=%s]".formatted(postOperator)));
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X> InvertibleMapper<X, NumericalDynamicalSystem<?>> steppedNds(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NumericalDynamicalSystem<?>> beforeM,
      @Param(value = "stepT", dD = 1) double interval) {
    return beforeM.andThen(InvertibleMapper.from(
        (eNds, nds) ->
            NumericalDynamicalSystem.from(new Stepped<>(nds, interval), nds.nOfInputs(), nds.nOfOutputs()),
        eNds -> eNds,
        "stepped[t=%.2f]".formatted(interval)));
  }

  private static DoubleUnaryOperator toOperator(Function<Double, Double> f) {
    return new DoubleUnaryOperator() {
      @Override
      public double applyAsDouble(double v) {
        return f.apply(v);
      }

      @Override
      public String toString() {
        return f.toString();
      }
    };
  }
}
