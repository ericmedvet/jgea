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

package it.units.malelab.jgea.representation.tree;

import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.util.Misc;

import java.util.HashSet;
import java.util.Random;

/**
 * @author eric
 * @created 2020/08/05
 * @project jgea
 */
public class TreeUtils {

  private TreeUtils() {
  }

  public static <N> Tree<N> replaceAll(Tree<N> t, Tree<N> oldT, Tree<N> newT) {
    if (t.equals(oldT)) {
      return newT;
    }
    Tree<N> rebuilt = Tree.of(t.content());
    t.childStream().map(c -> replaceAll(c, oldT, newT)).forEach(rebuilt::addChild);
    return rebuilt;
  }

  public static <N> Tree<N> replaceFirst(Tree<N> t, Tree<N> oldT, Tree<N> newT) {
    if (t.equals(oldT)) {
      return newT;
    }
    Tree<N> rebuilt = Tree.of(t.content());
    boolean replaced = false;
    for (Tree<N> c : t) {
      Tree<N> newC = replaced ? c : replaceFirst(c, oldT, newT);
      replaced = replaced || !newC.equals(c);
      rebuilt.addChild(newC);
    }
    return rebuilt;
  }
}
