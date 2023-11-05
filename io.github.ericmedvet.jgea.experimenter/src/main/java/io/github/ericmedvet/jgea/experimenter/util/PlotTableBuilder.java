/*-
 * ========================LICENSE_START=================================
 * jgea-core
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
package io.github.ericmedvet.jgea.experimenter.util;

import io.github.ericmedvet.jgea.core.listener.AccumulatorFactory;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import java.util.List;

public interface PlotTableBuilder<E> extends AccumulatorFactory<E, XYPlotTable, Object> {

  NamedFunction<? super E, ? extends Number> xFunction();

  List<NamedFunction<? super E, ? extends Number>> yFunctions();

  default String xFormat() {
    return xFunction().getFormat();
  }

  default String xName() {
    return xFunction().getName();
  }

  default List<String> yFormats() {
    return yFunctions().stream().map(NamedFunction::getFormat).toList();
  }

  default List<String> yNames() {
    return yFunctions().stream().map(NamedFunction::getName).toList();
  }
}
