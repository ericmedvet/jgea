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
package io.github.ericmedvet.jgea.core.util;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface Table<R, C, T> {

  void addColumn(C columnIndex, Map<R, T> values);

  void addRow(R rowIndex, Map<C, T> values);

  List<C> colIndexes();

  T get(R rowIndex, C columnIndex);

  void removeColumn(C columnIndex);

  void removeRow(R rowIndex);

  List<R> rowIndexes();

  void set(R rowIndex, C columnIndex, T t);

  default void clear() {
    rowIndexes().forEach(this::removeRow);
  }

  default Table<R, C, T> colSlide(int n, Function<List<T>, T> aggregator) {
    Table<R, C, T> table = new HashMapTable<>();
    rowIndexes().forEach(ri -> IntStream.range(n, colIndexes().size()).forEach(i -> {
      List<T> ts = IntStream.range(i - n, i)
          .mapToObj(j -> get(ri, colIndexes().get(j)))
          .toList();
      table.set(ri, colIndexes().get(i - 1), aggregator.apply(ts));
    }));
    return table;
  }

  default Map<R, T> column(C columnIndex) {
    return rowIndexes().stream().collect(Collectors.toMap(ri -> ri, ri -> get(ri, columnIndex)));
  }

  default List<T> columnValues(C columnIndex) {
    Map<R, T> column = column(columnIndex);
    return rowIndexes().stream().map(column::get).toList();
  }

  default T get(int x, int y) {
    R ri = rowIndexes().get(y);
    C ci = colIndexes().get(x);
    if (ri == null || ci == null) {
      throw new IndexOutOfBoundsException(String.format(
          "Invalid %d,%d coords in a %d,%d table",
          x, y, colIndexes().size(), rowIndexes().size()
      ));
    }
    return get(ri, ci);
  }

  default int nColumns() {
    return colIndexes().size();
  }

  default int nRows() {
    return rowIndexes().size();
  }

  default String prettyPrint(String rFormat, String cFormat, String tFormat) {
    String colSep = " ";
    Map<C, Integer> widths = colIndexes().stream()
        .collect(Collectors.toMap(
            ci -> ci,
            ci -> Math.max(
                cFormat.formatted(ci).length(),
                rowIndexes().stream()
                    .mapToInt(ri -> tFormat.formatted(get(ri, ci)).length())
                    .max()
                    .orElse(0)
            )
        ));
    int riWidth = rowIndexes().stream()
        .mapToInt(ri -> rFormat.formatted(ri).length())
        .max()
        .orElse(0);
    StringBuilder sb = new StringBuilder();
    // print header
    sb.append(StringUtils.justify("", riWidth));
    sb.append(riWidth > 0 ? colSep : "");
    sb.append(colIndexes().stream()
        .map(ci -> StringUtils.justify(cFormat.formatted(ci), widths.get(ci)))
        .collect(Collectors.joining(colSep)));
    sb.append("\n");
    // print rows
    sb.append(rowIndexes().stream().map(ri -> {
      String s = StringUtils.justify(rFormat.formatted(ri), riWidth);
      s = s+(riWidth > 0 ? colSep : "");
      s = s+colIndexes().stream()
          .map(ci -> StringUtils.justify(tFormat.formatted(get(ri, ci)), widths.get(ci)))
          .collect(Collectors.joining(colSep));
      return s;
    }).collect(Collectors.joining("\n")));
    return sb.toString();
  }

  default Map<C, T> row(R rowIndex) {
    return colIndexes().stream().collect(Collectors.toMap(ci -> ci, ci -> get(rowIndex, ci)));
  }

  default Table<R, C, T> rowSlide(int n, Function<List<T>, T> aggregator) {
    Table<R, C, T> table = new HashMapTable<>();
    colIndexes().forEach(ci -> IntStream.range(n, rowIndexes().size()).forEach(i -> {
      List<T> ts = IntStream.range(i - n, i)
          .mapToObj(j -> get(rowIndexes().get(j), ci))
          .toList();
      table.set(rowIndexes().get(i - 1), ci, aggregator.apply(ts));
    }));
    return table;
  }

  default List<T> rowValues(R rowIndex) {
    Map<C, T> row = row(rowIndex);
    return colIndexes().stream().map(row::get).toList();
  }

  default String prettyPrint() {
    return prettyPrint("%s", "%s", "%s");
  }

  default Table<R, C, T> sorted(C c, Comparator<T> comparator) {
    Table<R, C, T> thisTable = this;
    return new Table<R, C, T>() {
      @Override
      public List<R> rowIndexes() {
        return thisTable.rowIndexes().stream()
            .sorted((ri1, ri2) -> comparator.compare(get(ri1, c), get(ri2, c)))
            .toList();
      }

      @Override
      public List<C> colIndexes() {
        return thisTable.colIndexes();
      }

      @Override
      public void addRow(R rowIndex, Map<C, T> values) {
        thisTable.addRow(rowIndex, values);
      }

      @Override
      public void addColumn(C columnIndex, Map<R, T> values) {
        thisTable.addColumn(columnIndex, values);
      }

      @Override
      public void removeRow(R rowIndex) {
        thisTable.removeRow(rowIndex);
      }

      @Override
      public void removeColumn(C columnIndex) {
        thisTable.removeColumn(columnIndex);
      }

      @Override
      public T get(R rowIndex, C columnIndex) {
        return thisTable.get(rowIndex, columnIndex);
      }

      @Override
      public void set(R rowIndex, C columnIndex, T t) {
        thisTable.set(rowIndex, columnIndex, t);
      }
    };
  }

  default void set(int x, int y, T t) {
    R ri = rowIndexes().get(y);
    C ci = colIndexes().get(x);
    if (ri == null || ci == null) {
      throw new IndexOutOfBoundsException(String.format(
          "Invalid %d,%d coords in a %d,%d table",
          x, y, colIndexes().size(), rowIndexes().size()
      ));
    }
    set(ri, ci, t);
  }

}
