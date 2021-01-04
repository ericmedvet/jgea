package it.units.malelab.jgea.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author eric on 2021/01/04 for jgea
 */
public class ArrayTable<T> implements Table<T> {

  private final List<String> names;
  private final List<T> values;

  public ArrayTable(List<String> names) {
    this.names = new ArrayList<>(names);
    this.values = new ArrayList<>();
  }

  @Override
  public List<T> row(int y) {
    int nColumns = nColumns();
    return values.subList(y * nColumns, (y + 1) * nColumns);
  }

  @Override
  public int nRows() {
    return values.size() / nColumns();
  }

  @Override
  public int nColumns() {
    return names.size();
  }

  @Override
  public void set(int x, int y, T t) {
    checkIndexes(x, y);
    values.set(index(x, y), t);
  }

  @Override
  public T get(int x, int y) {
    checkIndexes(x, y);
    return values.get(index(x, y));
  }

  @Override
  public void clear() {
    values.clear();
  }

  @Override
  public List<String> names() {
    return Collections.unmodifiableList(names);
  }

  @Override
  public void addColumn(String name, List<T> values) {
    if (values.size() != nRows()) {
      throw new IllegalArgumentException(String.format(
          "Wrong number of entries in new column: %d expected, %d found",
          nRows(),
          values.size()
      ));
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
      throw new IllegalArgumentException(String.format(
          "Wrong number of entries in new row: %d expected, %d found",
          nColumns(),
          values.size()
      ));
    }
    this.values.addAll(values);
  }

  private int index(int x, int y) {
    return y * nColumns() + x;
  }

}
