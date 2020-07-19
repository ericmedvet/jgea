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

import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.problem.symbolicregression.element.Constant;
import it.units.malelab.jgea.problem.symbolicregression.element.Decoration;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import it.units.malelab.jgea.problem.symbolicregression.element.Operator;
import it.units.malelab.jgea.problem.symbolicregression.element.Variable;

import java.util.function.Function;

/**
 * @author eric
 */
public class FormulaMapper implements Function<Node<String>, Node<Element>> {

  @Override
  public Node<Element> apply(Node<String> stringNode) {
    if (stringNode.getChildren().isEmpty()) {
      return new Node<>(fromString(stringNode.getContent()));
    }
    if (stringNode.getChildren().size() == 1) {
      return apply(stringNode.getChildren().get(0));
    }
    Node<Element> node = apply(stringNode.getChildren().get(0));
    for (int i = 1; i < stringNode.getChildren().size(); i++) {
      node.getChildren().add(apply(stringNode.getChildren().get(i)));
    }
    return node;
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
