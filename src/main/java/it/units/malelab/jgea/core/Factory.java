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

package it.units.malelab.jgea.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

/**
 * @author eric
 */
@FunctionalInterface
public interface Factory<T> extends Serializable {

  List<T> build(int n, Random random);

  default Factory<T> withOptimisticUniqueness(int maxAttempts) {
    Factory<T> innerFactory = this;
    return new Factory<T>() {
      @Override
      public List<T> build(int n, Random random) {
        int attempts = 0;
        List<T> ts = new ArrayList<>();
        while ((ts.size() < n) && (attempts < maxAttempts)) {
          attempts = attempts + 1;
          ts.addAll(new LinkedHashSet<>(innerFactory.build(n - ts.size(), random)));
        }
        ts.addAll(innerFactory.build(n - ts.size(), random));
        return ts;
      }
    };
  }

  default IndependentFactory<T> independent() {
    Factory<T> thisFactory = this;
    return (IndependentFactory<T>) random -> thisFactory.build(1, random).get(0);
  }

}
