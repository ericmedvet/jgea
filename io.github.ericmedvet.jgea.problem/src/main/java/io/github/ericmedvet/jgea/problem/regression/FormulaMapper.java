
package io.github.ericmedvet.jgea.problem.regression;

import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;

import java.util.function.Function;
public class FormulaMapper implements Function<Tree<String>, Tree<Element>> {

  private static Element fromString(String string) {
    for (Element.Operator operator : Element.Operator.values()) {
      if (operator.toString().equals(string)) {
        return operator;
      }
    }
    try {
      double value = Double.parseDouble(string);
      return new Element.Constant(value);
    } catch (NumberFormatException ex) {
      //just ignore
    }
    if (string.matches("[a-zA-Z]\\w*")) {
      return new Element.Variable(string);
    }
    return new Element.Decoration(string);
  }

  @Override
  public Tree<Element> apply(Tree<String> stringTree) {
    if (stringTree.isLeaf()) {
      return Tree.of(fromString(stringTree.content()));
    }
    if (stringTree.nChildren() == 1) {
      return apply(stringTree.child(0));
    }
    Tree<Element> tree = apply(stringTree.child(0));
    for (int i = 1; i < stringTree.nChildren(); i++) {
      tree.addChild(apply(stringTree.child(i)));
    }
    return tree;
  }

}
