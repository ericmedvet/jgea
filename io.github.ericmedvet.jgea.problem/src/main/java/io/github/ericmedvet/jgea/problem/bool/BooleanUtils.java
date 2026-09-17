/*-
 * ========================LICENSE_START=================================
 * jgea-problem
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
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

package io.github.ericmedvet.jgea.problem.bool;

import java.util.List;
import java.util.stream.IntStream;

public class BooleanUtils {

  public static List<boolean[]> buildCompleteCases(int n) {
    return IntStream.range(0, pow2(n))
        .mapToObj(i -> toBinary(i, n))
        .toList();
  }

  public static int fromBinary(boolean[] bits) {
    int n = 0;
    for (int i = bits.length - 1; i >= 0; i--) {
      n = (n << 1) | (bits[i] ? 1 : 0);
    }
    return n;
  }

  private static int pow2(int n) {
    if (n <= 0) {
      return 1;
    }
    return 2 * pow2(n - 1);
  }

  public static boolean[] toBinary(int input, int size) {
    boolean[] bits = new boolean[size];
    for (int i = size - 1; i >= 0; i--) {
      bits[i] = (input & (1 << i)) != 0;
    }
    return bits;
  }

}
