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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class ArrayTable<T> implements Table<T> {

  private final List<String> names;
  private final List<T> values;

  public ArrayTable(List<String> names) {
    this.names = new ArrayList<>(names);
    this.values = new ArrayList<>();
  }

  @Override
  public void addColumn(String name, List<T> values) {
    if (values.size() != nRows()) {
      throw new IllegalArgumentException(
          String.format(
              "Wrong number of entries in new column: %d expected, %d found",
              nRows(), values.size()));
    }
    List<String> newNames = new ArrayList<>(names);
    newNames.add(name);
    ArrayTable<T> newTable = new ArrayTable<>(newNames);
    for (int y = 0; y < nRows(); y++) {
      List<T> row = new ArrayList<>(row(y));
      row.add(values.get(y));
      newTable.addRow(row);
    }
    names.add(name);
    this.values.clear();
    this.values.addAll(newTable.values);
  }

  @Override
  public void addRow(List<T> values) {
    if (values.size() != nColumns()) {
      throw new IllegalArgumentException(
          String.format(
              "Wrong number of entries in new row: %d expected, %d found",
              nColumns(), values.size()));
    }
    this.values.addAll(values);
  }

  @Override
  public void clear() {
    values.clear();
  }

  @Override
  public T get(int x, int y) {
    checkIndexes(x, y);
    return values.get(index(x, y));
  }

  @Override
  public int nColumns() {
    return names.size();
  }

  @Override
  public int nRows() {
    return values.size() / nColumns();
  }

  @Override
  public List<String> names() {
    return Collections.unmodifiableList(names);
  }

  @Override
  public void set(int x, int y, T t) {
    checkIndexes(x, y);
    values.set(index(x, y), t);
  }

  @Override
  public List<T> row(int y) {
    int nColumns = nColumns();
    return values.subList(y * nColumns, (y + 1) * nColumns);
  }

  private int index(int x, int y) {
    return y * nColumns() + x;
  }

  @Override
  public boolean removeRow(List<T> values) {
    int[] ys = IntStream.range(0, nRows()).filter(y -> row(y).equals(values)).toArray();
    if (ys.length == 0) {
      return false;
    }
    for (int y = ys.length - 1; y >= 0; y = y - 1) {
      removeRow(y);
    }
    return true;
  }

  @Override
  public void removeRow(int y) {
    checkIndexes(0, y);
    int nColumns = nColumns();
    values.subList(y * nColumns, (y + 1) * nColumns).clear();
  }
}
