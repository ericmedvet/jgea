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

package it.units.malelab.jgea.representation.sequence;

import it.units.malelab.jgea.core.Sized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author eric
 */
public interface Sequence<T> extends Sized, Cloneable {

  T get(int index);

  void set(int index, T t);

  Sequence<T> clone();

  default T[] toArray() {
    List<T> ts = new ArrayList<>(size());
    for (int i = 0; i < size(); i++) {
      ts.add(get(i));
    }
    return (T[]) ts.toArray();
  }

  static <T> Sequence<T> from(final T... ts) {
    return from(Arrays.asList(ts));
  }

  static <T> Sequence<T> from(final List<T> list) {
    return new Sequence<T>() {
      @Override
      public T get(int index) {
        return list.get(index);
      }

      @Override
      public int size() {
        return list.size();
      }

      @Override
      public Sequence<T> clone() {
        return from(new ArrayList<T>(list));
      }

      @Override
      public void set(int index, T t) {
        throw new UnsupportedOperationException("Cannot set in read-only view of a list");
      }
    };
  }

}
