package it.units.malelab.jgea.core.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author eric on 2021/01/04 for jgea
 */
public interface Table<T> {

  int nRows();

  int nColumns();

  List<String> names();

  void set(int x, int y, T t);

  T get(int x, int y);

  void addColumn(String name, List<T> values);

  void addRow(List<T> values);

  void clear();

  default List<T> column(int x) {
    checkIndex("x", x, nColumns());
    return IntStream.range(0, nRows())
        .mapToObj(y -> get(x, y))
        .collect(Collectors.toList());
  }

  default List<T> row(int y) {
    checkIndex("y", y, nRows());
    return IntStream.range(0, nColumns())
        .mapToObj(x -> get(x, y))
        .collect(Collectors.toList());
  }

  default List<Pair<String, List<T>>> columns() {
    return IntStream.range(0, nColumns())
        .mapToObj(x -> Pair.of(names().get(x), column(x)))
        .collect(Collectors.toList());
  }

  default List<List<Pair<String, T>>> rows() {
    int nColumns = nColumns();
    return IntStream.range(0, nRows())
        .mapToObj(y -> IntStream.range(0, nColumns)
            .mapToObj(x -> Pair.of(names().get(x), get(x, y)))
            .collect(Collectors.toList())
        ).collect(Collectors.toList());
  }

  default List<T> column(String name) {
    int x = names().indexOf(name);
    if (x < 0) {
      throw new IndexOutOfBoundsException(String.format("No column %s in the table", name));
    }
    return column(x);
  }

  private static void checkIndex(String name, int i, int maxExcluded) {
    if (i < 0 || i >= maxExcluded) {
      throw new IndexOutOfBoundsException(String.format(
          "Invalid %s index: %d not in [0,%d]",
          name,
          i,
          maxExcluded
      ));
    }
  }

  default void checkIndexes(int x, int y) {
    checkIndex("column", x, nColumns());
    checkIndex("column", y, nRows());
  }

  default String prettyPrint(String format) {
    int[] widths = IntStream.range(0, nColumns())
        .map(x -> Math.max(
            names().get(x).length(),
            IntStream.range(0, nRows())
                .map(y -> String.format(format, get(x, y)).length())
                .max()
                .orElse(1)
        ))
        .toArray();
    StringBuilder sb = new StringBuilder();
    //print header
    sb.append(IntStream.range(0, nColumns())
        .mapToObj(x -> String.format(
            "%" + widths[x] + "." + widths[x] + "s",
            names().get(x)
        ))
        .collect(Collectors.joining(" ")));
    sb.append("\n");
    for (int y = 0; y < nRows(); y++) {
      int finalY = y;
      sb.append(IntStream.range(0, nColumns())
          .mapToObj(x -> String.format(
              "%" + widths[x] + "." + widths[x] + "s",
              String.format(format, get(x, finalY))
          ))
          .collect(Collectors.joining(" ")));
      if (y < nRows() - 1) {
        sb.append("\n");
      }
    }
    return sb.toString();
  }

  default <K> Table<K> map(Function<T, K> function) {
    Table<K> table = new ArrayTable<>(names());
    for (int y = 0; y < nRows(); y++) {
      table.addRow(row(y).stream().map(function).collect(Collectors.toList()));
    }
    return table;
  }

}
