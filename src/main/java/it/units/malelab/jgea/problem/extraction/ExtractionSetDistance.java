/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
