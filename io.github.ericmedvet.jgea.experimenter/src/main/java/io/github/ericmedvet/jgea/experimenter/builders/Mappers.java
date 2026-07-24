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
import io.github.ericmedvet.jgea.core.representation.grammar.grid.BitStringChooser;
import io.github.ericmedvet.jgea.core.representation.grammar.grid.DoublesChooser;
import io.github.ericmedvet.jgea.core.representation.grammar.grid.GridGrammar;
import io.github.ericmedvet.jgea.core.representation.grammar.grid.IntStringChooser;
import io.github.ericmedvet.jgea.core.representation.grammar.grid.StandardGridDeveloper;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammarBasedProblem;
import io.github.ericmedvet.jgea.core.representation.graph.Graph;
import io.github.ericmedvet.jgea.core.representation.graph.Node;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.functiongraph.FunctionGraph;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.OperatorGraph;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.Program;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Network;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.NetworkStructureException;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Runner;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.type.TypeException;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;
import io.github.ericmedvet.jgea.core.representation.tree.bool.TreeBasedBooleanFunction;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element.Variable;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.TreeBasedMultivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.TreeBasedUnivariateRealFunction;
import io.github.ericmedvet.jgea.problem.ca.MultivariateRealGridCellularAutomaton;
import io.github.ericmedvet.jgea.problem.regression.FormulaMapper;
import io.github.ericmedvet.jnb.core.Alias;
import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.core.ParamMap.Type;
import io.github.ericmedvet.jnb.core.PassThroughParam;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.GridUtils;
import io.github.ericmedvet.jnb.datastructure.Naming;
import io.github.ericmedvet.jnb.datastructure.Pair;
import io.github.ericmedvet.jnb.datastructure.Parametrized;
import io.github.ericmedvet.jnb.datastructure.Tree;
import io.github.ericmedvet.jsdynsym.core.bool.BooleanFunction;
import io.github.ericmedvet.jsdynsym.core.composed.Stepped;
import io.github.ericmedvet.jsdynsym.core.numerical.AggregatedInput;
import io.github.ericmedvet.jsdynsym.core.numerical.BiLevelNumericalDynamicalSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.EnhancedInput;
import io.github.ericmedvet.jsdynsym.core.numerical.MultivariateRealFunction;
import io.github.ericmedvet.jsdynsym.core.numerical.Noised;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalStatelessSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalTimeInvariantStatelessSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;
import io.github.ericmedvet.jsdynsym.core.numerical.named.NamedMultivariateRealFunction;
import io.github.ericmedvet.jsdynsym.core.numerical.named.NamedUnivariateRealFunction;
import io.github.ericmedvet.jsdynsym.core.rl.NumericalReinforcementLearningAgent;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Discoverable(prefixTemplate = "ea.mapper|m")
public class Mappers {

  private Mappers() {
  }

  // TODO add ge, hge, whge mappers

  @Cacheable
  public static <X> InvertibleMapper<X, NumericalDynamicalSystem<?>> aggregatedInputNds(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NumericalDynamicalSystem<?>> beforeM,
      @Param(
          value = "types", dSs = {"current", "trend"}) List<AggregatedInput.Type> types,
      @Param(value = "windowT", dD = 1) double windowT
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (eNds, nds) -> new AggregatedInput<>(nds, windowT, types),
            eNds -> NumericalStatelessSystem.zeros(
                eNds.nOfInputs() * types.size(),
                eNds.nOfOutputs()
            ),
            "aggregated[t=%.2f,%s]"
                .formatted(
                    windowT,
                    types.stream().map(t -> t.name().toLowerCase()).collect(Collectors.joining(";"))
                )
        )
    );
  }

  @Cacheable
  public static <X, T> InvertibleMapper<X, Grid<T>> bsToGrammarGrid(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, BitString> beforeM,
      @Param("grammar") Function<Grid<T>, GridGrammar<T>> grammar,
      @Param(value = "l", dI = 256) int l,
      @Param(value = "overwrite") boolean overwrite,
      @Param(
          value = "criteria", dSs = {"least_recent", "lowest_y", "lowest_x"}) List<StandardGridDeveloper.SortingCriterion> criteria
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (eGrid, bs) -> {
              GridGrammar<T> gridGrammar = grammar.apply(eGrid);
              return new StandardGridDeveloper<>(gridGrammar, overwrite, criteria)
                  .develop(new BitStringChooser<>(bs, gridGrammar))
                  .orElse(eGrid);
            },
            _ -> new BitString(l),
            "grammar.grid[bs;l=%d;o=%s;c=%s]".formatted(l, overwrite, criteria)
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, List<Tree<io.github.ericmedvet.jgea.core.representation.tree.bool.Element>>> cfgTreeToMultiBTree(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, Tree<String>> beforeM
  ) {
    io.github.ericmedvet.jgea.problem.bool.FormulaMapper mapper = new io.github.ericmedvet.jgea.problem.bool.FormulaMapper();
    return beforeM.andThen(
        InvertibleMapper.from(
            (_, t) -> mapper.apply(t),
            eTs -> io.github.ericmedvet.jgea.problem.bool.FormulaMapper.allVarsTree(
                (int) eTs.stream()
                    .flatMap(
                        t -> t.leafLabels()
                            .stream()
                            .filter(
                                l -> l instanceof io.github.ericmedvet.jgea.core.representation.tree.bool.Element.Variable
                            )
                            .map(
                                l -> ((io.github.ericmedvet.jgea.core.representation.tree.bool.Element.Variable) l)
                                    .index()
                            )
                    )
                    .distinct()
                    .count(),
                eTs.size()
            ),
            "multi.b.tree"
        )
    );
  }

  @Cacheable
  public static <X, N, S> InvertibleMapper<X, S> cfgTreeToPb(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, Tree<N>> beforeM,
      @Param("problem") StringGrammarBasedProblem<N, S> problem
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (_, t) -> problem.solutionMapper().apply(t),
            _ -> null,
            "pb[p=%s]".formatted(problem)
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, Tree<Element>> cfgTreeToSrTree(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, Tree<String>> beforeM
  ) {
    FormulaMapper mapper = new FormulaMapper();
    return beforeM.andThen(
        InvertibleMapper.from(
            (_, t) -> mapper.apply(t),
            eT -> FormulaMapper.allVarsTree(
                eT.leafLabels()
                    .stream()
                    .filter(l -> l instanceof Variable)
                    .map(l -> ((Variable) l).name())
                    .collect(Collectors.toSet())
            ),
            "sr.tree"
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, NumericalDynamicalSystem<?>> composedNds(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NumericalDynamicalSystem<?>> beforeM,
      @Param("then") Function<NumericalDynamicalSystem<?>, NumericalDynamicalSystem<?>> exampleBuilder,
      @Param("nOfInternalIOs") int nOfInternalIOs
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (eNds, nds) -> nds.andThen(
                exampleBuilder.apply(
                    MultivariateRealFunction.from(nOfInternalIOs, eNds.nOfOutputs())
                )
            ),
            eNds -> MultivariateRealFunction.from(
                eNds.nOfInputs(),
                nOfInternalIOs
            ),
            "then[%s]".formatted(exampleBuilder)
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, BitString> dsToBitString(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, List<Double>> beforeM,
      @Param(value = "t", dD = 0d) double t
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (eBs, ds) -> new BitString(ds.stream().map(v -> v < t).toList()),
            eBs -> Collections.nCopies(eBs.size(), 0d),
            "bs[t=%.1f]".formatted(t)
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, double[]> dsToDa(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, List<Double>> beforeM
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (e, ds) -> ds.stream().mapToDouble(v -> v).toArray(),
            da -> Arrays.stream(da).boxed().toList(),
            "da"
        )
    );
  }

  @Cacheable
  public static <X, T> InvertibleMapper<X, Grid<T>> dsToFillingRateGrid(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, List<Double>> beforeM,
      @Param(value = "rate", dD = 0.25) double rate,
      @Param("negItem") T negItem,
      @Param("posItem") T posItem
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (g, ds) -> {
              if (ds.size() != g.w() * g.h()) {
                throw new IllegalArgumentException(
                    "Wrong size for the double string: %dx%d=%d expected, %d found"
                        .formatted(g.w(), g.h(), g.w() * g.h(), ds.size())
                );
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
                      .toList()
              );
            },
            g -> Collections.nCopies(g.w() * g.h(), 0d),
            "grid[rate=%.2f]".formatted(rate)
        )
    );
  }

  @Cacheable
  public static <X, T> InvertibleMapper<X, Grid<T>> dsToGrammarGrid(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, List<Double>> beforeM,
      @Param("grammar") Function<Grid<T>, GridGrammar<T>> grammar,
      @Param(value = "l", dI = 256) int l,
      @Param(value = "overwrite") boolean overwrite,
      @Param(
          value = "criteria", dSs = {"least_recent", "lowest_y", "lowest_x"}) List<StandardGridDeveloper.SortingCriterion> criteria
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (eGrid, vs) -> {
              GridGrammar<T> gridGrammar = grammar.apply(eGrid);
              return new StandardGridDeveloper<>(gridGrammar, overwrite, criteria)
                  .develop(new DoublesChooser<>(vs, gridGrammar))
                  .orElse(eGrid);
            },
            _ -> Collections.nCopies(l, 0d),
            "grammar.grid[ds;l=%d;o=%s;c=%s]".formatted(l, overwrite, criteria)
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, IntString> dsToIs(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, List<Double>> beforeM,
      @Param(value = "range", dNPM = "ds.range(min=-1;max=1)") DoubleRange range
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (eIs, ds) -> {
              DoubleRange isRange = new DoubleRange(eIs.lowerBound(), eIs.upperBound());
              return new IntString(
                  ds.stream()
                      .map(v -> (int) Math.floor(isRange.denormalize(range.normalize(v))))
                      .map(i -> Math.clamp(i, eIs.lowerBound(), eIs.upperBound() - 1))
                      .toList(),
                  eIs.lowerBound(),
                  eIs.upperBound()
              );
            },
            eIs -> Collections.nCopies(eIs.size(), 0d),
            "is[min=%.0f;max=%.0f]".formatted(range.min(), range.max())
        )
    );
  }

  @Cacheable
  public static <X, T> InvertibleMapper<X, Grid<T>> dsToThresholdedGrid(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, List<Double>> beforeM,
      @Param(value = "t", dD = 0) double t,
      @Param("negItem") T negItem,
      @Param("posItem") T posItem
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (g, ds) -> {
              if (ds.size() != g.w() * g.h()) {
                throw new IllegalArgumentException(
                    "Wrong size for the double string: %dx%d=%d expected, %d found"
                        .formatted(g.w(), g.h(), g.w() * g.h(), ds.size())
                );
              }
              return Grid.create(
                  g.w(),
                  g.h(),
                  ds.stream().map(d -> d > t ? posItem : negItem).toList()
              );
            },
            g -> Collections.nCopies(g.w() * g.h(), 0d),
            "grid[t=%.2f]".formatted(t)
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, NumericalDynamicalSystem<?>> enhancedNds(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NumericalDynamicalSystem<?>> beforeM,
      @Param("windowT") double windowT,
      @Param(
          value = "types", dSs = {"current", "trend", "avg"}) List<EnhancedInput.Type> types
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (eNds, nds) -> new EnhancedInput<>(nds, windowT, types),
            eNds -> MultivariateRealFunction.from(
                eNds.nOfInputs() * types.size(),
                eNds.nOfOutputs()
            ),
            "enhanced[wT=%.2f;%s]"
                .formatted(
                    windowT,
                    types.stream().map(Enum::toString).collect(Collectors.joining(";"))
                )
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, NamedMultivariateRealFunction> fGraphToNmrf(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, Graph<Node, Double>> beforeM,
      @Param(value = "postOperator", dNPM = "ds.f.doubleOp(activationF=identity)") Function<Double, Double> postOperator
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (nmrf, g) -> NamedMultivariateRealFunction.from(
                nmrf.xVarNames(),
                nmrf.yVarNames()
            )
                .andThen(toOperator(postOperator)),
            nmrf -> FunctionGraph.sampleFor(nmrf.xVarNames(), nmrf.yVarNames()),
            "nmrf[po=%s]".formatted(postOperator)
        )
    );
  }

  @Cacheable
  public static <X, T, K> InvertibleMapper<X, Grid<K>> gridToGrid(
      @Param(value = "name", iS = "cell.map[m={mapper}]") String name,
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, Grid<T>> beforeM,
      @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<T, K> elementMapper,
      @Param(value = "predicate", dNPM = "f.nonNull()") Function<K, Boolean> predicate,
      @Param(value = "defaultElement", dNPM = "m.nullValue()") K defaultElement,
      @Param("onlyLargestConnected") boolean onlyLargestConnected
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (ekg, tg) -> {
              Grid<K> kg = tg.map(t -> elementMapper.mapperFor(ekg.values().getFirst()).apply(t));
              if (onlyLargestConnected) {
                kg = GridUtils.largestConnected(kg, predicate::apply, defaultElement);
              }
              return kg;
            },
            ekg -> ekg.map(elementMapper::exampleFor),
            name
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, X> identity() {
    return InvertibleMapper.identity();
  }

  @Cacheable
  public static <X, T> InvertibleMapper<X, List<T>> isIndexed(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, Pair<List<T>, IntString>> beforeM,
      @Param(value = "relativeLength", dD = 1) double relativeLength
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (eTs, p) -> {
              int tsSize = (int) Math.round(eTs.size() * relativeLength);
              // check for consistency of example and pair
              if (p.second().size() != eTs.size()) {
                throw new IllegalArgumentException(
                    "Sizes of indexes and example do not match: %d vs. %d".formatted(
                        p.second().size(),
                        eTs.size()
                    )
                );
              }
              if (p.second().lowerBound() != 0 || p.second().upperBound() != tsSize) {
                throw new IllegalArgumentException(
                    "Indexes domain is wrong: [%d,%d[ expected, [%d,%d[ found".formatted(
                        0,
                        tsSize,
                        p.second().lowerBound(),
                        p.second().upperBound()
                    )
                );
              }
              // check for self-consistency of pair
              if (p.second().upperBound() != p.first().size()) {
                throw new IllegalArgumentException(
                    "Size of values does not match domain of indexes: %d vs. [%d,%d[".formatted(
                        p.first().size(),
                        0,
                        p.second().upperBound()
                    )
                );
              }
              // do the mapping
              return p.second()
                  .genes()
                  .stream()
                  .map(i -> p.first().get(i))
                  .toList();
            },
            eTs -> new Pair<>(
                Collections.nCopies((int) Math.round(eTs.size() * relativeLength), eTs.getFirst()),
                new IntString(
                    Collections.nCopies(eTs.size(), 0),
                    0,
                    (int) Math.round(eTs.size() * relativeLength)
                )
            ),
            "indexed.by[rl=%f]".formatted(relativeLength)
        )
    );
  }

  @Cacheable
  public static <X, T> InvertibleMapper<X, Grid<T>> isToGrammarGrid(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, IntString> beforeM,
      @Param("grammar") Function<Grid<T>, GridGrammar<T>> grammar,
      @Param(value = "upperBound", dI = 16) int upperBound,
      @Param(value = "l", dI = 256) int l,
      @Param(value = "overwrite") boolean overwrite,
      @Param(
          value = "criteria", dSs = {"least_recent", "lowest_y", "lowest_x"}) List<StandardGridDeveloper.SortingCriterion> criteria
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (eGrid, is) -> {
              GridGrammar<T> gridGrammar = grammar.apply(eGrid);
              return new StandardGridDeveloper<>(gridGrammar, overwrite, criteria)
                  .develop(new IntStringChooser<>(is, gridGrammar))
                  .orElse(eGrid);
            },
            _ -> new IntString(Collections.nCopies(l, 0), 0, upperBound),
            "grid[is;l=%d;o=%s;c=%s]".formatted(l, overwrite, criteria)
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, Grid<String>> isToSGrid(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, IntString> beforeM,
      @Param(value = "maxW", dI = -1) int maxW,
      @Param(value = "maxH", dI = -1) int maxH,
      @Param(value = "nullItem", dB = true) boolean nullItem,
      @Param("items") List<String> items
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (g, is) -> {
              int w = (maxW > 0) ? Math.min(maxW, g.w()) : g.w();
              int h = (maxH > 0) ? Math.min(maxH, g.h()) : g.h();
              int upperBound = items.size() + (nullItem ? 1 : 0);
              if (is.size() != w * h) {
                throw new IllegalArgumentException(
                    "Wrong size of the integer string: %dx%d=%d expected, %d found"
                        .formatted(w, h, w * h, is.size())
                );
              }
              if (is.lowerBound() != 0 || is.upperBound() != upperBound) {
                throw new IllegalArgumentException(
                    "Wrong bounds of the integer string: [0;%d] expected, [%d;%d] found".formatted(
                        upperBound,
                        is.lowerBound(),
                        is.upperBound()
                    )
                );
              }
              Grid<String> grid = Grid.create(
                  w,
                  h,
                  is.genes()
                      .stream()
                      .map(i -> {
                        if (nullItem) {
                          return i == 0 ? null : items.get(i - 1);
                        }
                        return items.get(i);
                      })
                      .toList()
              );
              return Grid.create(
                  g.w(),
                  g.h(),
                  k -> grid.isValid(k) ? grid.get(k) : (nullItem ? null : items.getFirst())
              );
            },
            g -> {
              int w = (maxW > 0) ? Math.min(maxW, g.w()) : g.w();
              int h = (maxH > 0) ? Math.min(maxH, g.h()) : g.h();
              int upperBound = items.size() + (nullItem ? 1 : 0);
              return new IntString(Collections.nCopies(w * h, 0), 0, upperBound);
            },
            "s.grid[nOfItems=%d]".formatted(items.size())
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, String> isToString(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, IntString> beforeM,
      @Param(value = "alphabet", dS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ") String alphabet
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (_, is) -> is.genes()
                .stream()
                .map(i -> alphabet.substring(i, i + 1))
                .collect(Collectors.joining()),
            str -> new IntString(Collections.nCopies(str.length(), 0), 0, alphabet.length()),
            "string[alphabetSize=%d]".formatted(alphabet.length())
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, BooleanFunction> multiBTreeToBf(
      @Param(value = "name", dS = "bf") String name,
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, List<Tree<io.github.ericmedvet.jgea.core.representation.tree.bool.Element>>> beforeM
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (eBf, trees) -> new TreeBasedBooleanFunction(trees, eBf.nOfInputs()),
            TreeBasedBooleanFunction::exampleFor,
            name
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, NamedMultivariateRealFunction> multiSrTreeToNmrf(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, List<Tree<Element>>> beforeM,
      @Param("simplify") boolean simplify,
      @Param(value = "postOperator", dNPM = "ds.f.doubleOp(activationF=identity)") Function<Double, Double> postOperator
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (nmrf, ts) -> new TreeBasedMultivariateRealFunction(
                ts,
                nmrf.xVarNames(),
                nmrf.yVarNames(),
                simplify
            )
                .andThen(toOperator(postOperator)),
            nmrf -> TreeBasedMultivariateRealFunction.exampleFor(
                nmrf.xVarNames(),
                nmrf.yVarNames()
            ),
            "nmrf[po=%s]".formatted(postOperator)
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, NumericalDynamicalSystem<?>> ndsPairToBiLevelNds(
      @Param(value = "name", dS = "bi.level") String name,
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, Pair<NumericalDynamicalSystem<?>, NumericalDynamicalSystem<?>>> beforeM,
      @Param("nOfHighOutputs") int nOfHighOutputs,
      @Param("highPeriod") int highPeriod,
      @Param("highIndexes") List<Integer> highIndexes,
      @Param("lowIndexes") List<Integer> lowIndexes,
      @Param("averageEnabled") boolean averageEnabled
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (_, ndsPair) -> new BiLevelNumericalDynamicalSystem<>(
                ndsPair.first(),
                ndsPair.second(),
                highPeriod,
                highIndexes.stream().mapToInt(Integer::intValue).toArray(),
                lowIndexes.stream().mapToInt(Integer::intValue).toArray(),
                averageEnabled
            ),
            ndsE -> new Pair<>(
                MultivariateRealFunction.from(highIndexes.size(), nOfHighOutputs),
                MultivariateRealFunction.from(lowIndexes.size() + nOfHighOutputs, ndsE.nOfOutputs())
            ),
            name
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, NumericalReinforcementLearningAgent<?>> ndsToNrla(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NumericalDynamicalSystem<?>> beforeM
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (eNrla, nds) -> NumericalReinforcementLearningAgent.from(nds),
            eNrla -> MultivariateRealFunction.from(
                eNrla.nOfInputs(),
                eNrla.nOfOutputs()
            ),
            "rl.agent"
        )
    );
  }

  @Cacheable
  public static <X, T> InvertibleMapper<X, Grid<T>> nmrfToGrid(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NamedMultivariateRealFunction> beforeM,
      @Param("items") List<T> items
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (g, nmrf) -> {
              if (nmrf.nOfInputs() != 2) {
                throw new IllegalArgumentException(
                    "Wrong input size for the NMRF: 2 expected, %d found".formatted(
                        nmrf.nOfInputs()
                    )
                );
              }
              if (nmrf.nOfOutputs() != items.size()) {
                throw new IllegalArgumentException(
                    "Wrong output size for the NMRF: %d expected, %d found"
                        .formatted(items.size(), nmrf.nOfOutputs())
                );
              }
              return Grid.create(
                  g.w(),
                  g.h(),
                  (x, y) -> {
                    double[] values = nmrf.apply(
                        new double[]{(double) x / (double) g.w(), (double) y / (double) g.h()}
                    );
                    return items.get(
                        IntStream.range(0, values.length)
                            .boxed()
                            .min(Comparator.comparingDouble(i -> values[i]))
                            .orElse(0)
                    );
                  }
              );
            },
            _ -> NamedMultivariateRealFunction.from(
                MultivariateRealFunction.from(2, items.size()),
                List.of("x", "y"),
                IntStream.range(0, items.size())
                    .mapToObj("item%02d"::formatted)
                    .toList()
            ),
            "grid[nOfItems=%d]".formatted(items.size())
        )
    );
  }

  public static <X> InvertibleMapper<X, MultivariateRealGridCellularAutomaton> nmrfToMrca(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NamedMultivariateRealFunction> beforeM,
      @Param(value = "nOfAdditionalChannels", dI = 1) int nOfAdditionalChannels,
      @Param(
          value = "kernels", dSs = {"identity", "laplacian", "sobel_edges"}) List<MultivariateRealGridCellularAutomaton.Kernel> kernels,
      @Param(value = "initializer", dS = "center_all") MultivariateRealGridCellularAutomaton.Initializer initializer,
      @Param(value = "range", dNPM = "m.range(min=-1;max=1)") DoubleRange range,
      @Param(value = "additiveCoefficient", dD = 1d) double additiveCoefficient,
      @Param(value = "alivenessThreshold", dD = 0d) double alivenessThreshold,
      @Param(value = "toroidal") boolean toroidal
  ) {
    List<Grid<Double>> kernelGrids = kernels.stream()
        .map(MultivariateRealGridCellularAutomaton.Kernel::get)
        .flatMap(List::stream)
        .toList();
    return beforeM.andThen(
        InvertibleMapper.from(
            (mrca, nmrf) -> {
              int minStateSize = MultivariateRealGridCellularAutomaton.minStateSize(
                  mrca.getInitialStates()
              );
              int nOfInputs = (minStateSize + nOfAdditionalChannels) * kernelGrids.size();
              int nOfOutputs = minStateSize + nOfAdditionalChannels;
              if (nmrf.nOfInputs() != nOfInputs) {
                throw new IllegalArgumentException(
                    "Wrong input size for the MRF: %d expected, %d found"
                        .formatted(nOfInputs, nmrf.nOfInputs())
                );
              }
              if (nmrf.nOfOutputs() != nOfOutputs) {
                throw new IllegalArgumentException(
                    "Wrong output size for the MRF: %d expected, %d found"
                        .formatted(nOfOutputs, nmrf.nOfOutputs())
                );
              }
              return new MultivariateRealGridCellularAutomaton(
                  initializer.initialize(
                      mrca.getInitialStates().w(),
                      mrca.getInitialStates().h(),
                      minStateSize + nOfAdditionalChannels,
                      range
                  ),
                  range,
                  kernelGrids,
                  nmrf,
                  additiveCoefficient,
                  alivenessThreshold,
                  toroidal
              );
            },
            mrca -> {
              int minStateSize = MultivariateRealGridCellularAutomaton.minStateSize(
                  mrca.getInitialStates()
              );
              int nOfInputs = (minStateSize + nOfAdditionalChannels) * kernelGrids.size();
              int nOfOutputs = minStateSize + nOfAdditionalChannels;
              List<String> varNames = MultivariateRealFunction.varNames("c", nOfOutputs);
              return NamedMultivariateRealFunction.from(
                  MultivariateRealFunction.from(
                      nOfInputs,
                      nOfOutputs
                  ),
                  IntStream.range(0, kernelGrids.size())
                      .mapToObj(i -> varNames.stream().map(n -> "%s_k%d".formatted(n, i)))
                      .flatMap(Function.identity())
                      .toList(),
                  varNames
              );
            },
            "mrca[addChannels=%d;kernels=%d]".formatted(
                nOfAdditionalChannels,
                kernelGrids.size()
            )
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, NumericalDynamicalSystem<?>> nmrfToNds(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NamedMultivariateRealFunction> beforeM
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (_, nmrf) -> nmrf,
            nds -> NamedMultivariateRealFunction.from(
                MultivariateRealFunction.from(
                    nds.nOfInputs(),
                    nds.nOfOutputs()
                ),
                MultivariateRealFunction.varNames("i", nds.nOfInputs()),
                MultivariateRealFunction.varNames("o", nds.nOfOutputs())
            ),
            "nds"
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, NamedUnivariateRealFunction> nmrfToNurf(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NamedMultivariateRealFunction> beforeM
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (_, nmrf) -> NamedUnivariateRealFunction.from(nmrf),
            nurf -> nurf,
            "nurf"
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, NumericalDynamicalSystem<?>> noisedNds(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NumericalDynamicalSystem<?>> beforeM,
      @Param(value = "inputSigma", dD = 0) double inputSigma,
      @Param(value = "outputSigma", dD = 0) double outputSigma,
      @Param(value = "randomGenerator", dNPM = "m.defaultRG()") RandomGenerator randomGenerator
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (_, nds) -> new Noised<>(nds, inputSigma, outputSigma, randomGenerator),
            eNds -> eNds,
            "noised[in=%.2f;out=%.2f]".formatted(inputSigma, outputSigma)
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, NamedMultivariateRealFunction> noisedNmrf(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NamedMultivariateRealFunction> beforeM,
      @Param(value = "sigma", dD = 0) double sigma,
      @Param(value = "randomGenerator", dNPM = "m.defaultRG()") RandomGenerator randomGenerator
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (_, nmrf) -> nmrf.andThen(
                Naming.named(
                    "noised[out=%.2f]".formatted(sigma),
                    (DoubleUnaryOperator) v -> v + randomGenerator.nextGaussian() * sigma
                )
            ),
            eNmrf -> eNmrf,
            "noised[out=%.2f]".formatted(sigma)
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, NamedMultivariateRealFunction> ntissToNmrf(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NumericalTimeInvariantStatelessSystem> beforeM
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (nmrf, ntiss) -> NamedMultivariateRealFunction.from(
                nmrf.xVarNames(),
                nmrf.yVarNames()
            ),
            nmrf -> MultivariateRealFunction.from(
                nmrf.nOfInputs(),
                nmrf.nOfOutputs()
            ),
            "nmrf"
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, NumericalDynamicalSystem<?>> nurfToNds(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NamedUnivariateRealFunction> beforeM
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (_, nurf) -> nurf,
            nds -> NamedUnivariateRealFunction.from(
                UnivariateRealFunction.from(_ -> 0d, nds.nOfInputs()),
                MultivariateRealFunction.varNames("i", nds.nOfInputs()),
                "output"
            ),
            "nds"
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, NamedMultivariateRealFunction> oGraphToNmrf(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, Graph<Node, OperatorGraph.NonValuedArc>> beforeM,
      @Param(value = "postOperator", dNPM = "ds.f.doubleOp(activationF=identity)") Function<Double, Double> postOperator
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (nmrf, g) -> new OperatorGraph(g, nmrf.xVarNames(), nmrf.yVarNames()).andThen(
                toOperator(postOperator)
            ),
            nmrf -> OperatorGraph.sampleFor(nmrf.xVarNames(), nmrf.yVarNames()),
            "nmrf[po=%s]".formatted(postOperator)
        )
    );
  }

  @Cacheable
  public static <X, F1, S1, F2, S2> InvertibleMapper<X, Pair<F2, S2>> pair(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, Pair<F1, S1>> beforeM,
      @Param(value = "first", dNPM = "ea.m.identity()") InvertibleMapper<F1, F2> firstM,
      @Param(value = "second", dNPM = "ea.m.identity()") InvertibleMapper<S1, S2> secondM
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (p2, p1) -> new Pair<>(
                firstM.mapperFor(p2.first()).apply(p1.first()),
                secondM.mapperFor(p2.second()).apply(p1.second())
            ),
            p2 -> new Pair<>(firstM.exampleFor(p2.first()), secondM.exampleFor(p2.second())),
            "pair[first=%s;second=%s]".formatted(firstM, secondM)
        )
    );
  }

  @Alias(
      name = "dsToParametrized", value = "parametrized(of = ea.mapper.dsToDa(of = $innerOf))", passThroughParams = @PassThroughParam(name = "innerOf", value = "ea.m.identity()", type = Type.NAMED_PARAM_MAP)
  )
  @Cacheable
  public static <X, E, Y extends Parametrized<? extends E, P>, P> InvertibleMapper<X, E> parametrized(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, P> beforeM,
      @Param("parametrized") Function<E, Y> exampleBuilder
  ) {
    //noinspection unchecked
    return beforeM.andThen(
        InvertibleMapper.from(
            (e, p) -> exampleBuilder.apply(e).withParams(p),
            e -> exampleBuilder.apply(e).getParams(),
            "parametrized[%s]".formatted(exampleBuilder)
        )
    );
  }

  @Cacheable
  public static <X, T> InvertibleMapper<X, Pair<List<T>, List<T>>> splitter(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, List<T>> beforeM
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (ePair, ts) -> {
              // check for length consistency
              if (ePair.first().size() + ePair.second().size() != ts.size()) {
                throw new IllegalArgumentException(
                    "Wrong input size: %d+%d=%d expected, %d found".formatted(
                        ePair.first().size(),
                        ePair.second().size(),
                        ePair.first().size() + ePair.second().size(),
                        ts.size()
                    )
                );
              }
              // do the mapping
              return new Pair<>(
                  ts.subList(0, ePair.first().size()),
                  ts.subList(ePair.first().size(), ts.size())
              );
            },
            ePair -> Stream.concat(
                ePair.first().stream(),
                ePair.second().stream()
            ).toList(),
            "splitter"
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, NamedUnivariateRealFunction> srTreeToNurf(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, Tree<Element>> beforeM,
      @Param("simplify") boolean simplify,
      @Param(value = "postOperator", dNPM = "ds.f.doubleOp(activationF=identity)") Function<Double, Double> postOperator
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (nurf, t) -> new TreeBasedUnivariateRealFunction(
                t,
                nurf.xVarNames(),
                nurf.yVarName(),
                simplify
            )
                .andThen(toOperator(postOperator)),
            nurf -> TreeBasedUnivariateRealFunction.exampleFor(nurf.xVarNames(), nurf.yVarName()),
            "nurf[po=%s;simp=%s]".formatted(
                postOperator,
                Boolean.toString(simplify).substring(0, 1)
            )
        )
    );
  }

  @Cacheable
  public static <X> InvertibleMapper<X, NumericalDynamicalSystem<?>> steppedNds(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, NumericalDynamicalSystem<?>> beforeM,
      @Param(value = "stepT", dD = 1) double interval
  ) {
    return beforeM.andThen(
        InvertibleMapper.from(
            (_, nds) -> NumericalDynamicalSystem.from(
                new Stepped<>(nds, interval),
                nds.nOfInputs(),
                nds.nOfOutputs()
            ),
            eNds -> eNds,
            "stepped[t=%.2f]".formatted(interval)
        )
    );
  }

  protected static DoubleUnaryOperator toOperator(Function<Double, Double> f) {
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

  @Cacheable
  public static <X> InvertibleMapper<X, Program> ttpnToProgram(
      @Param(value = "of", dNPM = "ea.m.identity()") InvertibleMapper<X, Network> beforeM,
      @Param(value = "maxNOfSteps", dI = 128) int maxNOfSteps,
      @Param(value = "maxNOfTokens", dI = 256) int maxNOfTokens,
      @Param(value = "maxTokensSize", dI = 1024) int maxTokensSize,
      @Param(value = "maxSingleTokenSize", dI = 128) int maxSingleTokenSize,
      @Param(value = "skipBlocked", dB = true) boolean skipBlocked
  ) {
    Runner runner = new Runner(
        maxNOfSteps,
        maxNOfTokens,
        maxTokensSize,
        maxSingleTokenSize,
        skipBlocked
    );
    return beforeM.andThen(
        InvertibleMapper.from(
            (_, ttpn) -> runner.asInstrumentedProgram(ttpn),
            eProgram -> {
              try {
                return new Network(
                    Stream.<Gate>concat(
                        eProgram.inputTypes().stream().map(Gate::input),
                        eProgram.outputTypes().stream().map(Gate::output)
                    ).toList(),
                    new LinkedHashSet<>()
                );
              } catch (NetworkStructureException | TypeException e) {
                throw new RuntimeException(e);
              }
            },
            "program[s=%d,t=%d]".formatted(maxNOfSteps, maxNOfTokens)
        )
    );
  }

}