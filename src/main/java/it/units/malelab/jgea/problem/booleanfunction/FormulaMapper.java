/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.booleanfunction;

import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.problem.booleanfunction.element.Constant;
import it.units.malelab.jgea.problem.booleanfunction.element.Decoration;
import it.units.malelab.jgea.problem.booleanfunction.element.Element;
import it.units.malelab.jgea.problem.booleanfunction.element.Operator;
import it.units.malelab.jgea.problem.booleanfunction.element.Variable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author eric
 */
public class FormulaMapper implements Function<Node<String>, List<Node<Element>>> {

  public static final String MULTIPLE_OUTPUT_NON_TERMINAL = "<o>";

  @Override
  public List<Node<Element>> apply(Node<String> stringNode, Listener listener) throws FunctionException {
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

  public Node<Element> singleMap(Node<String> stringNode) throws FunctionException {
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
