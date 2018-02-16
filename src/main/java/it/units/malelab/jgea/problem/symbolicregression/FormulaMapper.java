/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.problem.symbolicregression.element.Constant;
import it.units.malelab.jgea.problem.symbolicregression.element.Decoration;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import it.units.malelab.jgea.problem.symbolicregression.element.Operator;
import it.units.malelab.jgea.problem.symbolicregression.element.Variable;

/**
 *
 * @author eric
 */
public class FormulaMapper implements Function<Node<String>, Node<Element>> {

  @Override
  public Node<Element> apply(Node<String> stringNode, Listener listener) throws FunctionException {
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
