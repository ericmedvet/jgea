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

package it.units.malelab.jgea.problem.booleanfunction;

import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.problem.booleanfunction.element.Constant;
import it.units.malelab.jgea.problem.booleanfunction.element.Decoration;
import it.units.malelab.jgea.problem.booleanfunction.element.Element;
import it.units.malelab.jgea.problem.booleanfunction.element.Operator;
import it.units.malelab.jgea.problem.booleanfunction.element.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author eric
 */
public class FormulaMapper implements Function<Node<String>, List<Node<Element>>> {

  public static final String MULTIPLE_OUTPUT_NON_TERMINAL = "<o>";

  @Override
  public List<Node<Element>> apply(Node<String> stringNode) {
    if (stringNode.getContent().equals(MULTIPLE_OUTPUT_NON_TERMINAL)) {
      List<Node<Element>> nodes = new ArrayList<>();
      for (Node<String> child : stringNode.getChildren()) {
        nodes.add(singleMap(child));
      }
      return nodes;
    } else {
      return Collections.singletonList(singleMap(stringNode));
    }
  }

  public Node<Element> singleMap(Node<String> stringNode) {
    if (stringNode.getChildren().isEmpty()) {
      return new Node<>(fromString(stringNode.getContent()));
    }
    if (stringNode.getChildren().size() == 1) {
      return singleMap(stringNode.getChildren().get(0));
    }
    Node<Element> node = singleMap(stringNode.getChildren().get(0));
    for (int i = 1; i < stringNode.getChildren().size(); i++) {
      node.getChildren().add(singleMap(stringNode.getChildren().get(i)));
    }
    return node;
  }

  private static Element fromString(String string) {
    for (Operator operator : Operator.values()) {
      if (operator.toString().equals(string)) {
        return operator;
      }
    }
    if (string.equals("0")) {
      return new Constant(false);
    }
    if (string.equals("1")) {
      return new Constant(true);
    }
    if (string.matches("[a-zA-Z]+[0-9.]+")) {
      return new Variable(string);
    }
    return new Decoration(string);
  }

}
