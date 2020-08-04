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

package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.representation.tree.Tree;
import it.units.malelab.jgea.problem.symbolicregression.element.Constant;
import it.units.malelab.jgea.problem.symbolicregression.element.Decoration;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import it.units.malelab.jgea.problem.symbolicregression.element.Operator;
import it.units.malelab.jgea.problem.symbolicregression.element.Variable;

import java.util.function.Function;

/**
 * @author eric
 */
public class FormulaMapper implements Function<Tree<String>, Tree<Element>> {

  @Override
  public Tree<Element> apply(Tree<String> stringTree) {
    if (stringTree.getChildren().isEmpty()) {
      return new Tree<>(fromString(stringTree.getContent()));
    }
    if (stringTree.getChildren().size() == 1) {
      return apply(stringTree.getChildren().get(0));
    }
    Tree<Element> tree = apply(stringTree.getChildren().get(0));
    for (int i = 1; i < stringTree.getChildren().size(); i++) {
      tree.getChildren().add(apply(stringTree.getChildren().get(i)));
    }
    return tree;
  }

  private static Element fromString(String string) {
    for (Operator operator : Operator.values()) {
      if (operator.toString().equals(string)) {
        return operator;
      }
    }
    try {
      double value = Double.parseDouble(string);
      return new Constant(value);
    } catch (NumberFormatException ex) {
      //just ignore
    }
    if (string.matches("[a-zA-Z]\\w*")) {
      return new Variable(string);
    }
    return new Decoration(string);
  }

}
