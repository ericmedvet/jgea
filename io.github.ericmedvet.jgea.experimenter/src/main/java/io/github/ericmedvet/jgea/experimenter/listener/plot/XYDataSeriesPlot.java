package io.github.ericmedvet.jgea.experimenter.listener.plot;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;

import java.util.List;

/**
 * @author "Eric Medvet" on 2023/12/10 for jgea
 */
public record XYDataSeriesPlot(
    String title,
    String xTitleName,
    String yTitleName,
    String xName,
    String yName,
    DoubleRange xRange,
    DoubleRange yRange,
    Grid<TitledData<List<XYDataSeries>>> dataGrid
) implements XYPlot<List<XYDataSeries>> {
}
