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

package it.units.malelab.jgea.problem.mapper;

import it.units.malelab.jgea.distance.Distance;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;

/**
 *
 * @author eric
 */
public class EnhancedProblem<N, S, F> {

  private final GrammarBasedProblem<N, S, F> problem;
  private final Distance<S> distance;

  public EnhancedProblem(GrammarBasedProblem<N, S, F> problem, Distance<S> distance) {
    this.problem = problem;
    this.distance = distance;
  }

  public GrammarBasedProblem<N, S, F> getProblem() {
    return problem;
  }

  public Distance<S> getDistance() {
    return distance;
  }

}
