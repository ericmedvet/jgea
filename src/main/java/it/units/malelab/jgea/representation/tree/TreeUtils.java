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

package it.units.malelab.jgea.representation.tree;

/**
 * @author eric
 */
public class TreeUtils {

  private TreeUtils() {
  }

  public static <N> Tree<N> replaceAll(Tree<N> t, Tree<N> oldT, Tree<N> newT) {
    if (t.equals(oldT)) {
      return Tree.copyOf(newT);
    }
    Tree<N> rebuilt = Tree.of(t.content());
    t.childStream().map(c -> replaceAll(c, oldT, newT)).forEach(rebuilt::addChild);
    return rebuilt;
  }

  public static <N> Tree<N> replaceFirst(Tree<N> t, Tree<N> oldT, Tree<N> newT) {
    if (t.equals(oldT)) {
      return Tree.copyOf(newT);
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
