package io.github.ericmedvet.jgea.core.util;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public class HashMapTable<R, C, T> implements Table<R, C, T> {

  private final Map<Key<R, C>, T> map;
  private final LinkedHashSet<R> rowIndexes;
  private final LinkedHashSet<C> colIndexes;
  public HashMapTable() {
    this.map = new HashMap<>();
    rowIndexes = new LinkedHashSet<>();
    colIndexes = new LinkedHashSet<>();
  }

  private record Key<R, C>(R r, C c) {}

  @Override
  public List<R> rowIndexes() {
    return rowIndexes.stream().toList();
  }

  @Override
  public List<C> colIndexes() {
    return colIndexes.stream().toList();
  }

  @Override
  public void addColumn(C columnIndex, Map<R, T> values) {
    if (colIndexes.contains(columnIndex)) {
      throw new IllegalArgumentException("Column %s is already in the table".formatted(columnIndex));
    }
    colIndexes.add(columnIndex);
    rowIndexes.addAll(values.keySet());
    values.forEach((rowIndex, value) -> map.put(new Key<>(rowIndex, columnIndex), value));
  }

  @Override
  public void addRow(R rowIndex, Map<C, T> values) {
    if (rowIndexes.contains(rowIndex)) {
      throw new IllegalArgumentException("Row %s is already in the table".formatted(rowIndex));
    }
    rowIndexes.add(rowIndex);
    colIndexes.addAll(values.keySet());
    values.forEach((colIndex, value) -> map.put(new Key<>(rowIndex, colIndex), value));
  }

  @Override
  public void removeRow(R rowIndex) {
    rowIndexes.remove(rowIndex);
    List<Key<R, C>> toRemoveKeys =
        map.keySet().stream().filter(k -> k.r.equals(rowIndex)).toList();
    toRemoveKeys.forEach(map.keySet()::remove);
  }

  @Override
  public void removeColumn(C columnIndex) {
    colIndexes.remove(columnIndex);
    List<Key<R, C>> toRemoveKeys =
        map.keySet().stream().filter(k -> k.c.equals(columnIndex)).toList();
    toRemoveKeys.forEach(map.keySet()::remove);
  }

  @Override
  public T get(R rowIndex, C columnIndex) {
    return map.get(new Key<>(rowIndex, columnIndex));
  }

  @Override
  public void set(R rowIndex, C columnIndex, T t) {
    rowIndexes.add(rowIndex);
    colIndexes.add(columnIndex);
    map.put(new Key<>(rowIndex, columnIndex), t);
  }
}
