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

package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.representation.NamedMultivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.NamedUnivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.graph.Graph;
import io.github.ericmedvet.jgea.core.representation.graph.Node;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.functiongraph.FunctionGraph;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.OperatorGraph;
import io.github.ericmedvet.jgea.core.representation.grid.*;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.TreeBasedMultivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.TreeBasedUnivariateRealFunction;
import io.github.ericmedvet.jgea.experimenter.InvertibleMapper;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.buildable.builders.NumericalDynamicalSystems;
import io.github.ericmedvet.jsdynsym.core.NumericalParametrized;
import io.github.ericmedvet.jsdynsym.core.StatelessSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.MultivariateRealFunction;
import io.github.ericmedvet.jsdynsym.core.numerical.ann.MultiLayerPerceptron;
import io.github.ericmedvet.jsdynsym.grid.Grid;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2023/05/01 for jgea
 */
public class Mappers {
  private Mappers() {
  }

  @SuppressWarnings("unused")
  public static <A, B, C> InvertibleMapper<A, C> compose(
      @Param(value = "first") InvertibleMapper<A, B> first,
      @Param(value = "second") InvertibleMapper<B, C> second
  ) {
    return first.andThen(second);
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<Graph<Node, Double>, NamedMultivariateRealFunction> fGraphMRF(
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator
  ) {
    return InvertibleMapper.from(
        (nmrf, g) -> new FunctionGraph(g, nmrf.xVarNames(), nmrf.yVarNames()),
        nmrf -> FunctionGraph.sampleFor(nmrf.xVarNames(), nmrf.yVarNames())
    );
  }

  @SuppressWarnings("unused")
  public static <T> InvertibleMapper<NamedMultivariateRealFunction, NamedUnivariateRealFunction> fromMRFToURF() {
    return InvertibleMapper.from(
        (nurf, nmrf) -> NamedUnivariateRealFunction.from(nmrf),
        nurf -> nurf
    );
  }

  @SuppressWarnings("unused")
  public static <X> InvertibleMapper<X, X> identity() {
    return InvertibleMapper.identity();
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<BitString, BitString> identityBitString() {
    return InvertibleMapper.from(
        (bs, g) -> g,
        bs -> bs
    );
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<IntString, IntString> identityIntString() {
    return InvertibleMapper.from(
        (is, g) -> g,
        is -> is
    );
  }

  @SuppressWarnings("unused")
  public static <T> InvertibleMapper<IntString, Grid<T>> intGrammarGrid(
      @Param("grammar") GridGrammar<T> grammar,
      @Param(value = "upperBound", dI = 16) int upperBound,
      @Param(value = "l", dI = 256) int l,
      @Param(value = "overwrite", dB = true) boolean overwrite,
      @Param(value = "criteria", dSs = {"least_recent"}) List<StandardGridDeveloper.SortingCriterion> criteria
  ) {
    GridDeveloper<T> gridDeveloper = new StandardGridDeveloper<>(grammar, overwrite, criteria);
    return InvertibleMapper.from(
        (eGrid, is) -> {
          IntStringOptionChooser<T> chooser = new IntStringOptionChooser<>(is, grammar);
          return gridDeveloper.develop(chooser).orElse(eGrid);
        },
        eGrid -> new IntString(0, upperBound, l)
    );
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<List<Double>, NamedMultivariateRealFunction> mlpMRF(
      @Param(value = "innerLayerRatio", dD = 0.65) double innerLayerRatio,
      @Param(value = "nOfInnerLayers", dI = 1) int nOfInnerLayers,
      @Param(value = "activationFunction", dS = "tanh") MultiLayerPerceptron.ActivationFunction activationFunction
  ) {
    Function<NamedMultivariateRealFunction, int[]> innerNeuronsFunction = nmrf -> {
      int[] innerNeurons = new int[nOfInnerLayers];
      int centerSize = (int) Math.max(2, Math.round(nmrf.xVarNames().size() * innerLayerRatio));
      if (nOfInnerLayers > 1) {
        for (int i = 0; i < nOfInnerLayers / 2; i++) {
          innerNeurons[i] = nmrf.xVarNames().size() + (centerSize - nmrf.xVarNames()
              .size()) / (nOfInnerLayers / 2 + 1) * (i + 1);
        }
        for (int i = nOfInnerLayers / 2; i < nOfInnerLayers; i++) {
          innerNeurons[i] =
              centerSize + (nmrf.yVarNames().size() - centerSize) / (nOfInnerLayers / 2 + 1) * (i - nOfInnerLayers / 2);
        }
      } else if (nOfInnerLayers > 0) {
        innerNeurons[0] = centerSize;
      }
      return innerNeurons;
    };
    return InvertibleMapper.from(
        (nmrf, params) ->
            NamedMultivariateRealFunction.from(new MultiLayerPerceptron(
                activationFunction,
                nmrf.xVarNames().size(),
                innerNeuronsFunction.apply(nmrf),
                nmrf.yVarNames().size(),
                params.stream().mapToDouble(v -> v).toArray()
            ), nmrf.xVarNames(), nmrf.yVarNames()),
        nmrf -> Collections.nCopies(
            new MultiLayerPerceptron(
                activationFunction,
                nmrf.xVarNames().size(),
                innerNeuronsFunction.apply(nmrf),
                nmrf.yVarNames().size()
            ).getParams().length,
            0d
        )
    );
  }

  @SuppressWarnings("unused")
  public static <T> InvertibleMapper<List<Double>, Grid<T>> numGrammarGrid(
      @Param("grammar") GridGrammar<T> grammar,
      @Param(value = "l", dI = 256) int l,
      @Param(value = "overwrite", dB = true) boolean overwrite,
      @Param(value = "criteria", dSs = {"least_recent"}) List<StandardGridDeveloper.SortingCriterion> criteria
  ) {
    GridDeveloper<T> gridDeveloper = new StandardGridDeveloper<>(grammar, overwrite, criteria);
    return InvertibleMapper.from(
        (eGrid, vs) -> {
          GridDeveloper.Chooser<T> chooser = new DoublesOptionChooser<>(vs, grammar);
          return gridDeveloper.develop(chooser).orElse(eGrid);
        },
        eGrid -> Collections.nCopies(l, 0d)
    );
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<List<Double>, NamedMultivariateRealFunction> numericalParametrizedMRF(
      @Param("function") NumericalDynamicalSystems.Builder<MultivariateRealFunction, StatelessSystem.State> function
  ) {
    return InvertibleMapper.from(
        (nmrf, params) -> {
          if (nmrf instanceof NumericalParametrized parametrized) {
            MultivariateRealFunction np = function.apply(nmrf.xVarNames(), nmrf.yVarNames());
            ((NumericalParametrized) np).setParams(params.stream().mapToDouble(v -> v).toArray());
            return NamedMultivariateRealFunction.from(np, nmrf.xVarNames(), nmrf.yVarNames());
          }
          throw new IllegalArgumentException("The provided function is not numerical parametrized.");
        },
        nmrf -> {
          if (nmrf instanceof NumericalParametrized parametrized) {
            return Arrays.stream(parametrized.getParams()).boxed().toList();
          }
          throw new IllegalArgumentException("The provided function is not numerical parametrized.");
        }
    );
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<Graph<Node, OperatorGraph.NonValuedArc>, NamedMultivariateRealFunction> oGraphMRF(
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator
  ) {
    return InvertibleMapper.from(
        (nmrf, g) -> new OperatorGraph(g, nmrf.xVarNames(), nmrf.yVarNames()),
        nmrf -> OperatorGraph.sampleFor(nmrf.xVarNames(), nmrf.yVarNames())
    );
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<List<Tree<Element>>, NamedMultivariateRealFunction> treeMRF(
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator
  ) {
    return InvertibleMapper.from(
        (nmrf, ts) -> new TreeBasedMultivariateRealFunction(ts, nmrf.xVarNames(), nmrf.yVarNames()),
        nmrf -> TreeBasedMultivariateRealFunction.sampleFor(nmrf.xVarNames(), nmrf.yVarNames())
    );
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<Tree<Element>, NamedUnivariateRealFunction> treeURF(
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator
  ) {
    return InvertibleMapper.from(
        (nurf, t) -> new TreeBasedUnivariateRealFunction(t, nurf.xVarNames(), nurf.yVarName()),
        nurf -> TreeBasedUnivariateRealFunction.sampleFor(nurf.xVarNames(), nurf.yVarName())
    );
  }
}
