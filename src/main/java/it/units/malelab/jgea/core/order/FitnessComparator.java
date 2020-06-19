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

package it.units.malelab.jgea.core.order;

import it.units.malelab.jgea.core.Individual;

/**
 * @author eric
 * @created 2020/06/17
 * @project jgea
 */
public class FitnessComparator<F> implements PartialComparator<Individual<?, ?, ? extends F>> {
  private final PartialComparator<? super F> comparator;

  public FitnessComparator(PartialComparator<? super F> comparator) {
    this.comparator = comparator;
  }

  @Override
  public PartialComparatorOutcome compare(Individual<?, ?, ? extends F> i1, Individual<?, ?, ? extends F> i2) {
    return comparator.compare(i1.getFitness(), i2.getFitness());
  }

}
