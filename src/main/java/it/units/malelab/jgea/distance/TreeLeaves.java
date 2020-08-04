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

package it.units.malelab.jgea.distance;

import it.units.malelab.jgea.representation.sequence.Sequence;
import it.units.malelab.jgea.representation.tree.Tree;

import java.util.stream.Collectors;

/**
 * @author eric
 */
public class TreeLeaves<T> implements Distance<Tree<T>> {

  private final Distance<Sequence<T>> innerDistance;

  public TreeLeaves(Distance<Sequence<T>> innerDistance) {
    this.innerDistance = innerDistance;
  }

  @Override
  public Double apply(Tree<T> t1, Tree<T> t2) {
    Sequence<T> s1 = Sequence.from(t1.leafNodes().stream().map(Tree::getContent).collect(Collectors.toList()));
    Sequence<T> s2 = Sequence.from(t2.leafNodes().stream().map(Tree::getContent).collect(Collectors.toList()));
    return innerDistance.apply(s1, s2);
  }


}
