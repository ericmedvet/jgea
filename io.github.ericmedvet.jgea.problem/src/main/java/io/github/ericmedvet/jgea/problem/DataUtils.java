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

package io.github.ericmedvet.jgea.problem;

import java.util.List;
import java.util.stream.IntStream;

/**
 * @author eric
 */
public class DataUtils {

  public static <E> List<E> fold(List<E> items, int fold, int n) {
    return folds(items, List.of(fold), n);
  }

  public static <E> List<E> folds(List<E> items, List<Integer> folds, int n) {
    return IntStream.range(0, items.size())
        .filter(i -> folds.contains(i % n))
        .mapToObj(items::get)
        .toList();
  }

}
