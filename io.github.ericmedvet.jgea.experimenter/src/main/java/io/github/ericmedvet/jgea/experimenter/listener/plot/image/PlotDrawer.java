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

import io.github.ericmedvet.jgea.experimenter.listener.plot.XYPlot;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public interface PlotDrawer<P extends XYPlot<D>, D> {

  double computeLegendH(ImagePlotter ip, Graphics2D g, P plot);

  void drawLegend(ImagePlotter ip, Graphics2D g, Rectangle2D r, P p);

  void drawPlot(ImagePlotter ip, Graphics2D g, Rectangle2D r, D data, Axes a, P p);
}
