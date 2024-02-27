/*-
 * ========================LICENSE_START=================================
 * jgea-problem
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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

package io.github.ericmedvet.jgea.problem.extraction;

import io.github.ericmedvet.jgea.core.distance.Distance;
import io.github.ericmedvet.jgea.core.util.IntRange;
import java.util.Set;

public class ExtractionSetDistance implements Distance<Set<IntRange>> {

  private final int length;
  private final int bins;

  public ExtractionSetDistance(int length, int bins) {
    this.length = length;
    this.bins = bins;
  }

  @Override
  public Double apply(Set<IntRange> ranges1, Set<IntRange> ranges2) {
    boolean[] mask1 = new boolean[bins + 1];
    boolean[] mask2 = new boolean[bins + 1];
    for (IntRange range : ranges1) {
      mask1[(int) Math.floor((double) range.min() / (double) length * (double) bins)] = true;
      mask1[(int) Math.floor((double) range.max() / (double) length * (double) bins)] = true;
    }
    for (IntRange range : ranges2) {
      mask2[(int) Math.floor((double) range.min() / (double) length * (double) bins)] = true;
      mask2[(int) Math.floor((double) range.max() / (double) length * (double) bins)] = true;
    }
    double count = 0;
    for (int i = 0; i < bins; i++) {
      count = count + ((mask1[i] == mask2[i]) ? 1 : 0);
    }
    return ((double) length - count) / (double) length;
  }
}
