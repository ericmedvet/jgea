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

import it.units.malelab.jgea.representation.tree.Tree;
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
public class FormulaMapper implements Function<Tree<String>, List<Tree<Element>>> {

  public static final String MULTIPLE_OUTPUT_NON_TERMINAL = "<o>";

  @Override
  public List<Tree<Element>> apply(Tree<String> stringTree) {
    if (stringTree.getContent().equals(MULTIPLE_OUTPUT_NON_TERMINAL)) {
      List<Tree<Element>> trees = new ArrayList<>();
      for (Tree<String> child : stringTree.getChildren()) {
        trees.add(singleMap(child));
      }
      return trees;
    } else {
      return Collections.singletonList(singleMap(stringTree));
    }
  }

  public Tree<Element> singleMap(Tree<String> stringTree) {
    if (stringTree.getChildren().isEmpty()) {
      return new Tree<>(fromString(stringTree.getContent()));
    }
    if (stringTree.getChildren().size() == 1) {
      return singleMap(stringTree.getChildren().get(0));
    }
    Tree<Element> tree = singleMap(stringTree.getChildren().get(0));
    for (int i = 1; i < stringTree.getChildren().size(); i++) {
      tree.getChildren().add(singleMap(stringTree.getChildren().get(i)));
    }
    return tree;
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
