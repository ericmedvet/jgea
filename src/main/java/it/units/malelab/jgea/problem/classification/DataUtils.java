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

package it.units.malelab.jgea.problem.classification;

import it.units.malelab.jgea.core.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class DataUtils {

  public static <O, L> List<Pair<O, L>> fold(List<Pair<O, L>> data, int i, int n) {
    List<Pair<O, L>> subset = new ArrayList<>();
    data.stream().map(Pair::second).distinct().forEach((L l) -> {
      List<Pair<O, L>> currentSubset = data.stream()
          .filter((Pair<O, L> pair) -> (pair.second().equals(l)))
          .collect(Collectors.toList());
      subset.addAll(
          currentSubset.stream()
              .skip(currentSubset.size() / n * i)
              .limit(currentSubset.size() / n).collect(Collectors.toList()));
    });
    return subset;
  }

}
