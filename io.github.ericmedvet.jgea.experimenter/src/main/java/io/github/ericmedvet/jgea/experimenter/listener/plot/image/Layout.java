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
package io.github.ericmedvet.jgea.experimenter.listener.plot.image;

import java.awt.geom.Rectangle2D;

record Layout(
    double w,
    double h,
    int plotCols,
    int plotRows,
    double mainTitleH,
    double legendH,
    double commonColTitleH,
    double commonRowTitleW,
    double commonXAxesH,
    double commonYAxesW,
    double xAxisH,
    double yAxisW,
    double colTitleH,
    double rowTitleW,
    Configuration.Layout configuration) {
  Layout refit(double newXAxisH, double newYAxisW) {
    return new Layout(
        w,
        h,
        plotCols,
        plotRows,
        mainTitleH,
        legendH,
        commonColTitleH,
        commonRowTitleW,
        commonXAxesH == 0 ? 0 : newXAxisH,
        commonYAxesW == 0 ? 0 : newYAxisW,
        xAxisH == 0 ? 0 : newXAxisH,
        yAxisW == 0 ? 0 : newYAxisW,
        colTitleH,
        rowTitleW,
        configuration);
  }

  double plotInnerW() {
    return plotOuterW() - yAxisW - rowTitleW;
  }

  double plotOuterW() {
    return (w - commonYAxesW - commonRowTitleW) / (double) plotCols;
  }

  double plotInnerH() {
    return plotOuterH() - xAxisH - colTitleH;
  }

  double plotOuterH() {
    return (h - mainTitleH - commonColTitleH - legendH - commonXAxesH) / (double) plotRows;
  }

  Rectangle2D mainTitle() {
    return new Rectangle2D.Double(
        0,
        configuration.mainTitleMarginHRate() * h,
        w,
        mainTitleH - 2d * configuration.mainTitleMarginHRate() * h);
  }

  Rectangle2D legend() {
    return new Rectangle2D.Double(
        configuration.legendMarginWRate() * w,
        h - legendH + configuration.legendMarginHRate() * h,
        w - 2d * configuration.legendMarginWRate() * w,
        legendH - 2d * configuration.legendMarginHRate() * h);
  }

  Rectangle2D commonColTitle(int plotX) {
    return new Rectangle2D.Double(
        commonYAxesW + (double) plotX * plotOuterW() + yAxisW,
        mainTitleH + configuration.colTitleMarginHRate() * h,
        plotInnerW(),
        commonColTitleH - 2d * configuration.colTitleMarginHRate() * h);
  }

  Rectangle2D commonRowTitle(int plotY) {
    return new Rectangle2D.Double(
        w - commonRowTitleW + configuration.rowTitleMarginWRate() * w,
        mainTitleH + commonColTitleH + (double) plotY * plotOuterH() + colTitleH,
        commonRowTitleW - 2d * configuration.rowTitleMarginWRate() * w,
        plotInnerH());
  }

  Rectangle2D commonXAxis(int plotX) {
    return new Rectangle2D.Double(
        innerPlot(plotX, 0).getX(),
        h - legendH - commonXAxesH + configuration.xAxisMarginHRate() * h,
        innerPlot(plotX, 0).getWidth(),
        commonXAxesH - 2d * configuration.xAxisMarginHRate() * h);
  }

  Rectangle2D commonYAxis(int plotY) {
    return new Rectangle2D.Double(
        colTitleH + configuration.yAxisMarginWRate() * w,
        innerPlot(0, plotY).getY(),
        commonYAxesW - 2d * configuration.yAxisMarginWRate() * w,
        innerPlot(0, plotY).getHeight());
  }

  Rectangle2D colTitle(int plotX, int plotY) {
    return new Rectangle2D.Double(
        commonYAxesW + (double) plotX * plotOuterW() + yAxisW,
        mainTitleH + commonColTitleH + (double) plotY * plotOuterH() + configuration.colTitleMarginHRate() * h,
        plotInnerW(),
        colTitleH - 2d * configuration.colTitleMarginHRate() * h);
  }

  Rectangle2D rowTitle(int plotX, int plotY) {
    return new Rectangle2D.Double(
        commonYAxesW
            + yAxisW
            + (double) plotX * plotOuterW()
            + plotInnerW()
            + configuration.rowTitleMarginWRate() * w,
        mainTitleH + commonColTitleH + (double) plotY * plotOuterH() + colTitleH,
        rowTitleW - 2d * configuration.rowTitleMarginWRate() * w,
        plotInnerH());
  }

  Rectangle2D yAxis(int plotX, int plotY) {
    return new Rectangle2D.Double(
        commonYAxesW + (double) plotX * plotOuterW() + configuration.yAxisMarginWRate() * w,
        innerPlot(plotX, plotY).getY(),
        yAxisW - 2d * configuration.yAxisMarginWRate() * w,
        innerPlot(plotX, plotY).getHeight());
  }

  Rectangle2D xAxis(int plotX, int plotY) {
    return new Rectangle2D.Double(
        innerPlot(plotX, plotY).getX(),
        mainTitleH
            + commonColTitleH
            + (double) plotY * plotOuterH()
            + colTitleH
            + plotInnerH()
            + configuration.xAxisMarginHRate() * h,
        innerPlot(plotX, plotY).getWidth(),
        xAxisH - 2d * configuration.xAxisMarginHRate() * h);
  }

  Rectangle2D innerPlot(int plotX, int plotY) {
    return new Rectangle2D.Double(
        commonYAxesW + (double) plotX * plotOuterW() + yAxisW + configuration.plotMarginWRate() * w,
        mainTitleH
            + commonColTitleH
            + (double) plotY * plotOuterH()
            + colTitleH
            + configuration.plotMarginHRate() * h,
        plotInnerW() - 2d * configuration.plotMarginWRate() * w,
        plotInnerH() - 2d * configuration.plotMarginHRate() * h);
  }
}
