package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.util.ArrayTable;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.core.util.Table;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2022/11/26 for jgea
 */
public class XYPlotTableBuilder<E> extends TableBuilder<E, Number, Object> implements PlotTableBuilder<E> {
  private final NamedFunction<? super E, ? extends Number> xFunction;
  private final List<NamedFunction<? super E, ? extends Number>> yFunctions;
  private final int width;
  private final int height;
  private final double minX;
  private final double maxX;
  private final double minY;
  private final double maxY;

  private final boolean sorted;
  private final boolean firstDifference;

  public XYPlotTableBuilder(
      NamedFunction<? super E, ? extends Number> xFunction, List<NamedFunction<? super E, ? extends Number>> yFunctions
  ) {
    this(xFunction, yFunctions, 1, 1, Double.NaN, Double.NaN, Double.NaN, Double.NaN, true, false);
  }

  public XYPlotTableBuilder(
      NamedFunction<? super E, ? extends Number> xFunction,
      List<NamedFunction<? super E, ? extends Number>> yFunctions,
      int width,
      int height,
      double minX,
      double maxX,
      double minY,
      double maxY,
      boolean sorted,
      boolean firstDifference
  ) {
    super(Misc.concat(List.of(List.of(xFunction), yFunctions)), List.of());
    this.xFunction = xFunction;
    //noinspection unchecked,rawtypes
    this.yFunctions = (List) yFunctions.stream()
        .map(f -> firstDifference ? f.rename("delta[%s]".formatted(f.getName())) : f)
        .toList();
    this.width = width;
    this.height = height;
    this.minX = minX;
    this.maxX = maxX;
    this.minY = minY;
    this.maxY = maxY;
    this.sorted = sorted;
    this.firstDifference = firstDifference;
  }

  @Override
  public Accumulator<E, Table<Number>> build(Object o) {
    Accumulator<E, Table<Number>> accumulator = super.build(o);
    return new Accumulator<>() {
      @Override
      public Table<Number> get() {
        Table<Number> table = accumulator.get();
        if (sorted) {
          Table<Number> sTable = new ArrayTable<>(table.names());
          List<List<Pair<String, Number>>> rows = table.rows();
          rows.stream()
              .sorted(Comparator.comparing(r -> r.get(0).second().doubleValue()))
              .forEach(r -> sTable.addRow(r.stream().map(Pair::second).toList()));
        }
        if (firstDifference) {
          Table<Number> dTable = new ArrayTable<>(Misc.concat(List.of(
              List.of(xName()),
              yNames()
          )));
          for (int rI = 1; rI < table.nRows(); rI++) {
            List<Number> currentRow = table.row(rI);
            List<Number> lastRow = table.row(rI - 1);
            List<Number> diffRow = IntStream.range(0, currentRow.size()).mapToObj(cI -> {
              if (cI == 0) {
                return currentRow.get(cI);
              } else {
                return currentRow.get(cI).doubleValue() - lastRow.get(cI).doubleValue();
              }
            }).toList();
            dTable.addRow(diffRow);
          }
          table = dTable;
        }
        return table;
      }

      @Override
      public void listen(E e) {
        accumulator.listen(e);
      }
    };
  }

  public int getHeight() {
    return height;
  }

  public double getMaxX() {
    return maxX;
  }

  public double getMaxY() {
    return maxY;
  }

  public double getMinX() {
    return minX;
  }

  public double getMinY() {
    return minY;
  }

  public int getWidth() {
    return width;
  }

  @Override
  public NamedFunction<? super E, ? extends Number> xFunction() {
    return xFunction;
  }

  @Override
  public List<NamedFunction<? super E, ? extends Number>> yFunctions() {
    return yFunctions;
  }
}
