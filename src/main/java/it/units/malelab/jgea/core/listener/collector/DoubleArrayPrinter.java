/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.core.listener.collector;

import java.util.function.Function;

/**
 * @author eric
 */
public class DoubleArrayPrinter implements Function<double[], String> {

  private final String[] formats;

  public DoubleArrayPrinter(String... formats) {
    this.formats = formats;
    if (formats.length == 0) {
      formats = new String[]{"%s"};
    }
  }

  @Override
  public String apply(double[] a) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < a.length; i++) {
      sb.append(String.format(formats[i % formats.length], a[i]));
      if (i < a.length - 1) {
        sb.append(";");
      }
    }
    sb.append("]");
    return sb.toString();
  }

}
