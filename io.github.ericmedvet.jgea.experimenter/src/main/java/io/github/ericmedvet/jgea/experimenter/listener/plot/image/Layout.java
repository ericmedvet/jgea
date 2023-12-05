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
    double rowTitleW
) {
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
    return new Rectangle2D.Double(0, 0, w, mainTitleH);
  }

  Rectangle2D legend() {
    return new Rectangle2D.Double(0, h - legendH, w, legendH);
  }

  Rectangle2D commonColTitle(int plotX) {
    return new Rectangle2D.Double(
        commonYAxesW + (double) plotX * plotOuterW() + yAxisW,
        mainTitleH,
        plotInnerW(),
        commonColTitleH
    );
  }

  Rectangle2D commonRowTitle(int plotY) {
    return new Rectangle2D.Double(
        w - commonRowTitleW,
        mainTitleH + commonColTitleH + (double) plotY * plotOuterH() + colTitleH,
        commonRowTitleW,
        plotInnerH()
    );
  }

  Rectangle2D commonXAxis(int plotX) {
    return new Rectangle2D.Double(
        commonYAxesW + (double) plotX * plotOuterW() + yAxisW,
        h - legendH - commonXAxesH,
        plotInnerW(),
        commonXAxesH
    );
  }

  Rectangle2D commonYAxis(int plotY) {
    return new Rectangle2D.Double(
        0,
        mainTitleH + commonColTitleH + (double) plotY * plotOuterH() + colTitleH,
        commonYAxesW,
        plotInnerH()
    );
  }

  Rectangle2D colTitle(int plotX, int plotY) {
    return new Rectangle2D.Double(
        commonYAxesW + (double) plotX * plotOuterW() + yAxisW,
        mainTitleH + commonColTitleH + (double) plotY * plotOuterH(),
        plotInnerW(),
        colTitleH
    );
  }

  Rectangle2D rowTitle(int plotX, int plotY) {
    return new Rectangle2D.Double(
        commonYAxesW + yAxisW + (double) plotX * plotOuterW() + plotInnerW(),
        mainTitleH + commonColTitleH + (double) plotY * plotOuterH() + colTitleH,
        rowTitleW,
        plotInnerH()
    );
  }

  Rectangle2D yAxis(int plotX, int plotY) {
    return new Rectangle2D.Double(
        commonYAxesW + (double) plotX * plotOuterH(),
        mainTitleH + commonColTitleH + (double) plotY * plotOuterH() + colTitleH,
        yAxisW,
        plotInnerH()
    );
  }

  Rectangle2D xAxis(int plotX, int plotY) {
    return new Rectangle2D.Double(
        commonYAxesW + (double) plotX * plotOuterW() + yAxisW,
        mainTitleH + commonColTitleH + (double) plotY * plotOuterH() + colTitleH + plotInnerH(),
        plotInnerW(),
        xAxisH
    );
  }

  Rectangle2D innerPlot(int plotX, int plotY) {
    return new Rectangle2D.Double(
        commonYAxesW + (double) plotX * plotOuterW() + yAxisW,
        mainTitleH + commonColTitleH + (double) plotY * plotOuterH() + colTitleH,
        plotInnerW(),
        plotInnerH()
    );
  }

}
