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
package io.github.ericmedvet.jgea.experimenter.listener.plot;

import io.github.ericmedvet.jsdynsym.core.DoubleRange;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.DoubleStream;

/**
 * @author "Eric Medvet" on 2023/12/01 for jgea
 */
public class ImagePlotter implements Plotter<BufferedImage> {
  public record Configuration(
      int w,
      int h,
      int margin,
      Color bgColor,
      Color plotBgColor,
      Color boxColor,
      Color gridColor,
      Color textColor,
      List<Color> colors,
      float alpha
  ) {}

  private final Configuration c;

  public ImagePlotter() {
    this(new Configuration(
        400,
        300,
        5,
        Color.GRAY,
        Color.WHITE,
        Color.DARK_GRAY,
        Color.LIGHT_GRAY,
        Color.DARK_GRAY,
        List.of(Color.RED, Color.BLUE, Color.GREEN),
        0.2f
    ));
  }

  public ImagePlotter(Configuration c) {
    this.c = c;
  }

  @Override
  public BufferedImage plot(XYSinglePlot<?, ?> plot) {
    //prepare
    BufferedImage img = new BufferedImage(c.w, c.h, BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g = img.createGraphics();
    g.setColor(c.bgColor);
    g.fill(new Rectangle2D.Double(0, 0, c.w, c.h));
    g.setClip(new Rectangle2D.Double(10, 10, c.w - 20, c.h - 20));
    g.setColor(c.plotBgColor);
    g.fill(new Rectangle2D.Double(0, 0, c.w, c.h));
    g.setColor(c.textColor);
    g.drawString("ciao", 20, 20);
    //dispose
    g.dispose();
    return img;
  }

  @Override
  public BufferedImage plot(XYMatrixPlot<?, ?> plot) {
    //prepare
    BufferedImage img = new BufferedImage(c.w, c.h, BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g = img.createGraphics();
    g.setColor(c.bgColor);
    g.fill(new Rectangle2D.Double(0, 0, c.w, c.h));
    g.setClip(new Rectangle2D.Double(10, 10, c.w - 20, c.h - 20));
    g.setColor(c.plotBgColor);
    g.fill(new Rectangle2D.Double(0, 0, c.w, c.h));
    g.setColor(c.textColor);
    g.drawString("ciao", 20, 20);
    //dispose
    g.dispose();
    return img;
  }

  public static void showImage(BufferedImage image) {
    EventQueue.invokeLater(() -> {
      JFrame frame = new JFrame("Image");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.add(new JPanel() {
        protected void paintComponent(Graphics g) {
          super.paintComponent(g);
          Dimension d = getSize();
          Graphics2D g2d = (Graphics2D) g.create();
          g2d.drawImage(image, (d.width - image.getWidth()) / 2, (d.height - image.getHeight()) / 2, this);
          g2d.dispose();
        }

        public Dimension getPreferredSize() {
          return new Dimension(image.getWidth(), image.getHeight());
        }
      });
      frame.pack();
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
    });
  }

  public static void main(String[] args) {
    XYSinglePlot<Value, Value> p = XYSinglePlot.of("x", "y", DoubleRange.UNBOUNDED, DoubleRange.UNBOUNDED,
        List.of(XYDataSeries.of(
            "sin(x)",
            DoubleStream.iterate(0, v -> v < 5, v -> v + .1)
                .mapToObj(x -> new XYDataSeries.Point<>(Value.of(x), Value.of(Math.sin(x) )))
                .toList()
        ))
    );
    ImagePlotter ip = new ImagePlotter();
    showImage(ip.plot(p));
  }
}
