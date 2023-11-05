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
package io.github.ericmedvet.jgea.experimenter.util;

import io.github.ericmedvet.jgea.core.util.Table;
import java.util.List;
import java.util.Map;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public interface XYPlotTable extends Table<Integer, String, Number> {

  static XYPlotTable from(Table<Integer, String, Number> table) {
    return new XYPlotTable() {
      @Override
      public void addColumn(String columnIndex, Map<Integer, Number> values) {
        table.addColumn(columnIndex, values);
      }

      @Override
      public void addRow(Integer rowIndex, Map<String, Number> values) {
        table.addRow(rowIndex, values);
      }

      @Override
      public List<String> colIndexes() {
        return table.colIndexes();
      }

      @Override
      public Number get(Integer rowIndex, String columnIndex) {
        return table.get(rowIndex, columnIndex);
      }

      @Override
      public void removeColumn(String columnIndex) {
        table.removeColumn(columnIndex);
      }

      @Override
      public void removeRow(Integer rowIndex) {
        table.removeRow(rowIndex);
      }

      @Override
      public List<Integer> rowIndexes() {
        return table.rowIndexes();
      }

      @Override
      public void set(Integer rowIndex, String columnIndex, Number number) {
        table.set(rowIndex, columnIndex, number);
      }
    };
  }

  default String xName() {
    return colIndexes().get(0);
  }

  default double[] xValues() {
    return columnValues(xName()).stream().mapToDouble(Number::doubleValue).toArray();
  }

  default List<String> yNames() {
    return colIndexes().subList(1, colIndexes().size());
  }

  default double[] yValues(String yName) {
    return columnValues(yName).stream().mapToDouble(Number::doubleValue).toArray();
  }
}
