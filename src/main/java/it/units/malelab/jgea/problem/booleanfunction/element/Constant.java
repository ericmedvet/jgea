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

package it.units.malelab.jgea.problem.booleanfunction.element;

/**
 * @author eric
 */
public class Constant implements Element {

  private final boolean value;

  public Constant(boolean value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return Boolean.toString(value);
  }

  public boolean getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 17 * hash + (this.value ? 1 : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Constant other = (Constant) obj;
    if (this.value != other.value) {
      return false;
    }
    return true;
  }

}
