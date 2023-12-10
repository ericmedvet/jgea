package io.github.ericmedvet.jgea.experimenter.listener.plot;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;

import java.util.List;
import java.util.function.DoubleBinaryOperator;

/**
 * @author "Eric Medvet" on 2023/12/10 for jgea
 */
public record LandscapePlot(
    String title,
    String xTitleName,
    String yTitleName,
    String xName,
    String yName,
    DoubleRange xRange,
    DoubleRange yRange,
    Grid<TitledData<Data>> dataGrid
) implements XYPlot<LandscapePlot.Data> {
  record Data (DoubleBinaryOperator f, List<XYDataSeries> xyDataSeries) {}
}
