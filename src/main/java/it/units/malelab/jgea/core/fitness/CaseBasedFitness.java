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

package it.units.malelab.jgea.core.fitness;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class CaseBasedFitness<S, C, CF, AF> implements Function<S, AF> {

  private final List<C> cases;
  private final BiFunction<S, C, CF> caseFunction;
  private final Function<List<CF>, AF> aggregateFunction;

  public CaseBasedFitness(List<C> cases, BiFunction<S, C, CF> caseFunction, Function<List<CF>, AF> aggregateFunction) {
    this.cases = cases;
    this.caseFunction = caseFunction;
    this.aggregateFunction = aggregateFunction;
  }

  public List<C> getCases() {
    return cases;
  }

  public BiFunction<S, C, CF> getCaseFunction() {
    return caseFunction;
  }

  public Function<List<CF>, AF> getAggregateFunction() {
    return aggregateFunction;
  }

  @Override
  public AF apply(S s) {
    List<CF> caseFitnesses = cases.stream()
        .map(o -> caseFunction.apply(s, o))
        .collect(Collectors.toList());
    return aggregateFunction.apply(caseFitnesses);
  }
}
