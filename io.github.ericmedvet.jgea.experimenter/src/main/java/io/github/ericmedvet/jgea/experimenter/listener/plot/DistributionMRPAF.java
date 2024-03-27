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
/*
 * Copyright 2024 eric
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
package io.github.ericmedvet.jgea.experimenter.listener.plot;

import io.github.ericmedvet.jnb.datastructure.*;
import io.github.ericmedvet.jviz.core.plot.DistributionPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author "Eric Medvet" on 2024/01/04 for jgea
 */
public class DistributionMRPAF<E, R, K, X>
    extends AbstractMultipleRPAF<E, DistributionPlot, R, List<DistributionPlot.Data>, K, Map<K, List<Number>>> {
  protected final Function<? super E, X> predicateValueFunction;
  private final Function<? super R, ? extends K> lineFunction;
  private final Function<? super E, ? extends Number> yFunction;
  private final Predicate<? super X> predicate;
  private final DoubleRange yRange;

  public DistributionMRPAF(
      Function<? super R, ? extends K> xSubplotFunction,
      Function<? super R, ? extends K> ySubplotFunction,
      Function<? super R, ? extends K> lineFunction,
      Function<? super E, ? extends Number> yFunction,
      Function<? super E, X> predicateValueFunction,
      Predicate<? super X> predicate,
      DoubleRange yRange) {
    super(xSubplotFunction, ySubplotFunction);
    this.lineFunction = lineFunction;
    this.yFunction = yFunction;
    this.predicateValueFunction = predicateValueFunction;
    this.predicate = predicate;
    this.yRange = yRange;
  }

  @Override
  protected List<DistributionPlot.Data> buildData(K xK, K yK, Map<K, List<Number>> map) {
    return map.entrySet().stream()
        .map(e -> new DistributionPlot.Data(
            FormattedFunction.format(lineFunction).formatted(e.getKey()),
            e.getValue().stream().map(Number::doubleValue).toList()))
        .toList();
  }

  @Override
  protected DistributionPlot buildPlot(Table<K, K, List<DistributionPlot.Data>> data) {
    Grid<XYPlot.TitledData<List<DistributionPlot.Data>>> grid = Grid.create(
        data.nColumns(),
        data.nRows(),
        (x, y) -> new XYPlot.TitledData<>(
            FormattedFunction.format(xSubplotFunction)
                .formatted(data.colIndexes().get(x)),
            FormattedFunction.format(ySubplotFunction)
                .formatted(data.rowIndexes().get(y)),
            data.get(x, y)));
    String subtitle = "";
    if (grid.w() > 1 && grid.h() == 1) {
      subtitle = "→ %s".formatted(NamedFunction.name(xSubplotFunction));
    } else if (grid.w() == 1 && grid.h() > 1) {
      subtitle = "↓ %s".formatted(NamedFunction.name(ySubplotFunction));
    } else if (grid.w() > 1 && grid.h() > 1) {
      subtitle =
          "→ %s, ↓ %s".formatted(NamedFunction.name(xSubplotFunction), NamedFunction.name(ySubplotFunction));
    }
    return new DistributionPlot(
        "%s distribution%s"
            .formatted(
                NamedFunction.name(yFunction),
                subtitle.isEmpty() ? subtitle : (" (%s)".formatted(subtitle))),
        NamedFunction.name(xSubplotFunction),
        NamedFunction.name(ySubplotFunction),
        NamedFunction.name(lineFunction),
        NamedFunction.name(yFunction),
        yRange,
        grid);
  }

  @Override
  protected Map<K, List<Number>> init(K xK, K yK) {
    return new HashMap<>();
  }

  @Override
  protected Map<K, List<Number>> update(K xK, K yK, Map<K, List<Number>> map, E e, R r) {
    X predicateValue = predicateValueFunction.apply(e);
    if (predicate.test(predicateValue)) {
      K lineK = lineFunction.apply(r);
      List<Number> values = map.computeIfAbsent(lineK, k -> new ArrayList<>());
      values.add(yFunction.apply(e));
    }
    return map;
  }

  @Override
  public String toString() {
    return "distributionMRPAF(yFunction=" + yFunction + ')';
  }
}
