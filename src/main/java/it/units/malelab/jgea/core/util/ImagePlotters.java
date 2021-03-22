package it.units.malelab.jgea.core.util;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.Marker;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.awt.image.BufferedImage;
import java.util.function.Function;

/**
 * @author eric on 2021/01/04 for jgea
 */
public class ImagePlotters {

  private ImagePlotters() {
  }

  public static Function<Table<? extends Number>, BufferedImage> xyLines(int w, int h) {
    return data -> {
      if (data.nColumns() < 2) {
        throw new IllegalArgumentException(String.format(
            "Wrong number of data series: >1 expected, %d found",
            data.nColumns()
        ));
      }
      XYChart chart = new XYChartBuilder()
          .width(w).height(h)
          .xAxisTitle(data.names().get(0))
          .theme(Styler.ChartTheme.XChart)
          .build();
      if (data.nColumns() == 2) {
        chart.setYAxisTitle(data.names().get(1));
        chart.getStyler().setLegendVisible(false);
      }
      chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
      chart.getStyler().setSeriesMarkers(new Marker[]{SeriesMarkers.NONE});
      chart.getStyler().setYAxisDecimalPattern("#.##");
      chart.getStyler().setXAxisDecimalPattern("#.##");
      chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
      double[] xs = data.column(0).stream().mapToDouble(Number::doubleValue).toArray();
      for (int c = 1; c < data.nColumns(); c++) {
        double[] ys = data.column(c).stream().mapToDouble(Number::doubleValue).toArray();
        chart.addSeries(data.names().get(c), xs, ys);
      }
      return BitmapEncoder.getBufferedImage(chart);
    };
  }

}
