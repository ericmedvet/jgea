/*
 * Copyright 2023 eric
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

package io.github.ericmedvet.jgea.problem.grid;

import io.github.ericmedvet.jgea.core.problem.ComparableQualityBasedProblem;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import io.github.ericmedvet.jsdynsym.grid.GridUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class CharShapeApproximation implements ComparableQualityBasedProblem<Grid<Character>, Double>,
    ProblemWithExampleSolution<Grid<Character>> {

  private final Grid<Character> target;
  private final boolean translation;

  public CharShapeApproximation(Grid<Character> target, boolean translation) {
    this.target = target;
    this.translation = translation;
  }

  public CharShapeApproximation(String syntheticTargetName, boolean translation) throws IOException {
    try (BufferedReader br =
             new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(
                 "/grids/" + syntheticTargetName + ".txt"))))) {
      List<List<Character>> rows = br.lines().map(l -> l.chars().mapToObj(c -> (char) c).toList()).toList();
      List<Integer> ws = rows.stream().map(List::size).toList();
      if (ws.stream().distinct().count() != 1) {
        throw new IllegalArgumentException("The file has an invalid shape: %s".formatted(ws));
      }
      int w = ws.get(0);
      int h = ws.size();
      target = Grid.create(w, h, (x, y) -> {
        Character c = rows.get(y).get(x);
        return c.equals('·') ? null : c;
      });
      this.translation = translation;
    }
  }

  @Override
  public Grid<Character> example() {
    return Grid.create(
        1,
        1,
        target.values()
            .stream()
            .filter(Objects::nonNull)
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("Empty target grid"))
    );
  }

  @Override
  public Function<Grid<Character>, Double> qualityFunction() {
    return grid -> 0d + GridUtils.hammingDistance(grid, target, translation);
  }
}
