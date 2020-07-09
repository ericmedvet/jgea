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

package it.units.malelab.jgea.representation.grammar;

import it.units.malelab.jgea.representation.tree.Node;

import java.util.function.Function;

/**
 *
 * @author eric
 */
public abstract class GrammarBasedMapper<G, T> implements Function<G, Node<T>> {
  
  protected final Grammar<T> grammar;

  public GrammarBasedMapper(Grammar<T> grammar) {
    this.grammar = grammar;
  }

  public Grammar<T> getGrammar() {
    return grammar;
  }
  
}
