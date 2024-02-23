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

package io.github.ericmedvet.jgea.experimenter.builders;

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
import io.github.ericmedvet.jgea.experimenter.InvertibleMapper;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.buildable.builders.NumericalDynamicalSystems;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.core.NumericalParametrized;
import io.github.ericmedvet.jsdynsym.core.StatelessSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.MultivariateRealFunction;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.ann.MultiLayerPerceptron;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Discoverable(prefixTemplate = "ea.mapper|m")
public class Mappers {
  private Mappers() {}

  @SuppressWarnings("unused")
  public static <T> InvertibleMapper<BitString, Grid<T>> bitStringToGrammarGrid(
      @Param("grammar") GridGrammar<T> grammar,
      @Param(value = "l", dI = 256) int l,
      @Param(value = "overwrite") boolean overwrite,
      @Param(
              value = "criteria",
              dSs = {"least_recent", "lowest_y", "lowest_x"})
          List<StandardGridDeveloper.SortingCriterion> criteria) {
    Developer<T, Grid<T>, GridGrammar.ReferencedGrid<T>> gridDeveloper =
        new StandardGridDeveloper<>(grammar, overwrite, criteria);
    return InvertibleMapper.from(
        (eGrid, bs) -> {
          Chooser<T, GridGrammar.ReferencedGrid<T>> chooser = new BitStringChooser<>(bs, grammar);
          return gridDeveloper.develop(chooser).orElse(eGrid);
        },
        eGrid -> new BitString(l));
  }

  @SuppressWarnings("unused")
  public static <A, B, C> InvertibleMapper<A, C> compose(
      @Param(value = "first") InvertibleMapper<A, B> first,
      @Param(value = "second") InvertibleMapper<B, C> second) {
    return first.andThen(second);
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<List<Double>, BitString> doubleStringToBitString(
      @Param(value = "t", dD = 0d) double t) {
    return InvertibleMapper.from(
        (eBs, ds) -> new BitString(ds.stream().map(v -> v < t).toList()),
        eBs -> Collections.nCopies(eBs.size(), 0d));
  }

  @SuppressWarnings("unused")
  public static <T> InvertibleMapper<List<Double>, Grid<T>> doubleStringToGrammarGrid(
      @Param("grammar") GridGrammar<T> grammar,
      @Param(value = "l", dI = 256) int l,
      @Param(value = "overwrite") boolean overwrite,
      @Param(
              value = "criteria",
              dSs = {"least_recent", "lowest_y", "lowest_x"})
          List<StandardGridDeveloper.SortingCriterion> criteria) {
    Developer<T, Grid<T>, GridGrammar.ReferencedGrid<T>> gridDeveloper =
        new StandardGridDeveloper<>(grammar, overwrite, criteria);
    return InvertibleMapper.from(
        (eGrid, vs) -> {
          Chooser<T, GridGrammar.ReferencedGrid<T>> chooser = new DoublesChooser<>(vs, grammar);
          return gridDeveloper.develop(chooser).orElse(eGrid);
        },
        eGrid -> Collections.nCopies(l, 0d));
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<List<Double>, IntString> doubleStringToIntString(
      @Param(value = "range", dNPM = "ds.range(min=-1;max=1)") DoubleRange range) {
    return InvertibleMapper.from(
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
        eIs -> Collections.nCopies(eIs.size(), 0d));
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<Graph<Node, Double>, NamedMultivariateRealFunction> fGraphToMrf(
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator) {
    return InvertibleMapper.from(
        (nmrf, g) -> new FunctionGraph(g, nmrf.xVarNames(), nmrf.yVarNames()),
        nmrf -> FunctionGraph.sampleFor(nmrf.xVarNames(), nmrf.yVarNames()));
  }

  @SuppressWarnings("unused")
  public static <X> InvertibleMapper<X, X> identity() {
    return InvertibleMapper.identity();
  }

  @SuppressWarnings("unused")
  public static <T> InvertibleMapper<IntString, Grid<T>> intStringToGrammarGrid(
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
    return InvertibleMapper.from(
        (eGrid, is) -> {
          Chooser<T, GridGrammar.ReferencedGrid<T>> chooser = new IntStringChooser<>(is, grammar);
          return gridDeveloper.develop(chooser).orElse(eGrid);
        },
        eGrid -> new IntString(Collections.nCopies(l, 0), 0, upperBound));
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<List<Double>, NamedMultivariateRealFunction> mlpToMrf(
      @Param(value = "innerLayerRatio", dD = 0.65) double innerLayerRatio,
      @Param(value = "nOfInnerLayers", dI = 1) int nOfInnerLayers,
      @Param(value = "activationFunction", dS = "tanh")
          MultiLayerPerceptron.ActivationFunction activationFunction) {
    Function<NamedMultivariateRealFunction, int[]> innerNeuronsFunction = nmrf -> {
      int[] innerNeurons = new int[nOfInnerLayers];
      int centerSize = (int) Math.max(2, Math.round(nmrf.xVarNames().size() * innerLayerRatio));
      if (nOfInnerLayers > 1) {
        for (int i = 0; i < nOfInnerLayers / 2; i++) {
          innerNeurons[i] = nmrf.xVarNames().size()
              + (centerSize - nmrf.xVarNames().size()) / (nOfInnerLayers / 2 + 1) * (i + 1);
        }
        for (int i = nOfInnerLayers / 2; i < nOfInnerLayers; i++) {
          innerNeurons[i] = centerSize
              + (nmrf.yVarNames().size() - centerSize)
                  / (nOfInnerLayers / 2 + 1)
                  * (i - nOfInnerLayers / 2);
        }
      } else if (nOfInnerLayers > 0) {
        innerNeurons[0] = centerSize;
      }
      return innerNeurons;
    };
    return InvertibleMapper.from(
        (nmrf, params) -> NamedMultivariateRealFunction.from(
            new MultiLayerPerceptron(
                activationFunction,
                nmrf.xVarNames().size(),
                innerNeuronsFunction.apply(nmrf),
                nmrf.yVarNames().size(),
                params.stream().mapToDouble(v -> v).toArray()),
            nmrf.xVarNames(),
            nmrf.yVarNames()),
        nmrf -> Collections.nCopies(
            new MultiLayerPerceptron(
                    activationFunction,
                    nmrf.xVarNames().size(),
                    innerNeuronsFunction.apply(nmrf),
                    nmrf.yVarNames().size())
                .getParams()
                .length,
            0d));
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<NamedMultivariateRealFunction, NumericalDynamicalSystem<?>> mrfToNds() {
    return InvertibleMapper.from(
        (exampleNds, nmrf) -> nmrf,
        exampleNds -> NamedMultivariateRealFunction.from(
            MultivariateRealFunction.from(
                in -> new double[exampleNds.nOfOutputs()],
                exampleNds.nOfInputs(),
                exampleNds.nOfOutputs()),
            MultivariateRealFunction.varNames("x", exampleNds.nOfInputs()),
            MultivariateRealFunction.varNames("y", exampleNds.nOfOutputs())));
  }

  @SuppressWarnings("unused")
  public static <T> InvertibleMapper<NamedMultivariateRealFunction, NamedUnivariateRealFunction> mrfToUrf() {
    return InvertibleMapper.from((nurf, nmrf) -> NamedUnivariateRealFunction.from(nmrf), nurf -> nurf);
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<List<Tree<Element>>, NamedMultivariateRealFunction> multiSrTreeToMrf(
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator) {
    return InvertibleMapper.from(
        (nmrf, ts) -> new TreeBasedMultivariateRealFunction(ts, nmrf.xVarNames(), nmrf.yVarNames()),
        nmrf -> TreeBasedMultivariateRealFunction.sampleFor(nmrf.xVarNames(), nmrf.yVarNames()));
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<List<Double>, NamedMultivariateRealFunction> numericalParametrizedToMrf(
      @Param("function")
          NumericalDynamicalSystems.Builder<MultivariateRealFunction, StatelessSystem.State> function) {
    return InvertibleMapper.from(
        (nmrf, params) -> {
          if (nmrf instanceof NumericalParametrized) {
            MultivariateRealFunction np = function.apply(nmrf.xVarNames(), nmrf.yVarNames());
            ((NumericalParametrized) np)
                .setParams(params.stream().mapToDouble(v -> v).toArray());
            return NamedMultivariateRealFunction.from(np, nmrf.xVarNames(), nmrf.yVarNames());
          }
          throw new IllegalArgumentException("The provided function is not numerical parametrized.");
        },
        nmrf -> {
          if (nmrf instanceof NumericalParametrized parametrized) {
            return Arrays.stream(parametrized.getParams()).boxed().toList();
          }
          throw new IllegalArgumentException("The provided function is not numerical parametrized.");
        });
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<Graph<Node, OperatorGraph.NonValuedArc>, NamedMultivariateRealFunction> oGraphToMrf(
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator) {
    return InvertibleMapper.from(
        (nmrf, g) -> new OperatorGraph(g, nmrf.xVarNames(), nmrf.yVarNames()),
        nmrf -> OperatorGraph.sampleFor(nmrf.xVarNames(), nmrf.yVarNames()));
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<Tree<Element>, NamedUnivariateRealFunction> srTreeToUrf(
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator) {
    return InvertibleMapper.from(
        (nurf, t) -> new TreeBasedUnivariateRealFunction(t, nurf.xVarNames(), nurf.yVarName()),
        nurf -> TreeBasedUnivariateRealFunction.sampleFor(nurf.xVarNames(), nurf.yVarName()));
  }
}
