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

package it.units.malelab.jgea.problem.extraction;

import com.google.common.collect.Range;
import it.units.malelab.jgea.distance.Distance;

import java.util.Set;

/**
 * @author eric
 */
public class ExtractionSetDistance implements Distance<Set<Range<Integer>>> {

  private final int length;
  private final int bins;

  public ExtractionSetDistance(int length, int bins) {
    this.length = length;
    this.bins = bins;
  }

  @Override
  public Double apply(Set<Range<Integer>> ranges1, Set<Range<Integer>> ranges2) {
    boolean[] mask1 = new boolean[bins + 1];
    boolean[] mask2 = new boolean[bins + 1];
    for (Range<Integer> range : ranges1) {
      mask1[(int) Math.floor((double) range.lowerEndpoint() / (double) length * (double) bins)] = true;
      mask1[(int) Math.floor((double) range.upperEndpoint() / (double) length * (double) bins)] = true;
    }
    for (Range<Integer> range : ranges2) {
      mask2[(int) Math.floor((double) range.lowerEndpoint() / (double) length * (double) bins)] = true;
      mask2[(int) Math.floor((double) range.upperEndpoint() / (double) length * (double) bins)] = true;
    }
    double count = 0;
    for (int i = 0; i < bins; i++) {
      count = count + ((mask1[i] == mask2[i]) ? 1 : 0);
    }
    return ((double) length - count) / (double) length;
  }

}
