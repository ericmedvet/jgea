/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.jgea.core.representation.tree.numeric;

import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element.Constant;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element.Operator;
import io.github.ericmedvet.jnb.datastructure.Tree;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NumericTreeUtils {

  private NumericTreeUtils() {
  }

  public enum SimplificationType { ALGEBRAIC, STRUCTURAL }

  private static boolean commutationEquals(Tree<Element> t1, Tree<Element> t2) {
    if (!t1.label().equals(t2.label())) {
      return false;
    }
    if (t1.isLeaf() && t2.isLeaf()) {
      return true;
    }
    if (t1.label() instanceof Element.Operator operator) {
      if (operator.equals(Element.Operator.ADDITION) || operator.equals(
          Element.Operator.MULTIPLICATION
      )) {
        if (t1.children().size() != t2.children().size()) {
          return false;
        }
        if (t1.children().size() == 2) {
          if (commutationEquals(t1.child(0), t2.child(0)) && commutationEquals(
              t1.child(1),
              t2.child(1)
          )) {
            return true;
          }
          if (commutationEquals(t1.child(0), t2.child(1)) && commutationEquals(
              t1.child(1),
              t2.child(0)
          )) {
            return true;
          }
          return false;
        }
        List<Tree<Element>> t1Children = t1.children().stream()
            .sorted(Comparator.comparing(Tree::toString))
            .toList();
        List<Tree<Element>> t2Children = t2.children().stream()
            .sorted(Comparator.comparing(Tree::toString))
            .toList();
        for (int i = 0; i < t1Children.size(); i = i + 1) {
          if (!commutationEquals(t1Children.get(i), t2Children.get(i))) {
            return false;
          }
        }
        return true;
      }
      if (operator.arity() == 1) {
        return commutationEquals(t1.child(0), t2.child(0));
      }
      if (operator.arity() == 2) {
        return commutationEquals(t1.child(0), t2.child(0)) && commutationEquals(
            t1.child(1),
            t2.child(1)
        );
      }
      return IntStream.range(0, operator.arity())
          .allMatch(i -> commutationEquals(t1.child(i), t2.child(i)));
    }
    return false;
  }

  private static boolean hasValue(Tree<Element> tree, double v) {
    if (tree.label() instanceof Element.Constant(double value)) {
      return value == v;
    }
    return false;
  }

  private static boolean isNegative(Tree<Element> tree) {
    if (tree.label() instanceof Constant(double value)) {
      return value < 0;
    }
    return false;
  }

  private static boolean isPositive(Tree<Element> tree) {
    if (tree.label() instanceof Constant(double value)) {
      return value > 0;
    }
    if (tree.label() instanceof Operator o) {
      return switch (o) {
        case EXP, SQRT, SQ -> true;
        default -> false;
      };
    }
    return false;
  }

  private static List<Tree<Element>> operatorDescendants(Operator operator, Tree<Element> t) {
    if (t.label() instanceof Operator o) {
      if (o.equals(operator)) {
        return t.children().stream().flatMap(c -> operatorDescendants(operator, c).stream()).toList();
      }
    }
    return List.of(t);
  }

  public static Tree<Element> simplify(Tree<Element> tree) {
    return simplify(tree, EnumSet.of(SimplificationType.ALGEBRAIC, SimplificationType.STRUCTURAL));
  }

  public static Tree<Element> simplify(
      Tree<Element> tree,
      Set<SimplificationType> types
  ) {
    if (types.isEmpty()) {
      return tree;
    }
    if (tree.isLeaf()) {
      return tree;
    }
    // from here, algebraic simplification
    if (types.contains(SimplificationType.ALGEBRAIC)) {
      if (tree.label() instanceof Element.Operator operator) {
        // all constants
        if (tree.children().stream().allMatch(c -> c.label() instanceof Element.Constant)) {
          return simplify(
              new Tree<>(
                  new Element.Constant(
                      operator.applyAsDouble(
                          tree.children().stream()
                              .mapToDouble(c -> ((Element.Constant) c.label()).value())
                              .toArray()
                      )
                  )
              ),
              types
          );
        }
        // some constants for + and *
        if (operator.equals(Operator.ADDITION) || operator.equals(Operator.MULTIPLICATION)) {
          SequencedMap<Boolean, List<Tree<Element>>> simplifiedChildren = tree.children().stream()
              .map((Tree<Element> t) -> simplify(t, types))
              .collect(
                  Collectors.groupingBy(
                      c -> c.label() instanceof Element.Constant,
                      LinkedHashMap::new,
                      Collectors.toList()
                  )
              );
          if (simplifiedChildren.containsKey(true) && simplifiedChildren.get(true).size() > 1) {
            double reducedV = simplifiedChildren.get(true)
                .stream()
                .mapToDouble(c -> ((Constant) c.label()).value())
                .reduce((v1, v2) -> switch (operator) {
                  case ADDITION -> v1 + v2;
                  case MULTIPLICATION -> v1 * v2;
                  default -> throw new IllegalStateException(
                      "Unexpected operator %s instead of %s or %s".formatted(
                          operator,
                          Operator.ADDITION,
                          Operator.MULTIPLICATION
                      )
                  );
                })
                .orElseThrow();
            if (!simplifiedChildren.containsKey(false)) {
              return new Tree<>(new Constant(reducedV), List.of());
            }
            return new Tree<>(
                operator,
                Stream.concat(
                    simplifiedChildren.get(false).stream(),
                    Stream.of(new Tree<Element>(new Constant(reducedV), List.of()))
                ).toList()
            );
          }
        }
        // x + 0 -> x; 0 + x -> x
        if (operator.equals(Element.Operator.ADDITION) && tree.children().size() == 2) {
          if (hasValue(tree.child(0), 0)) {
            return simplify(tree.child(1), types);
          }
          if (hasValue(tree.child(1), 0)) {
            return simplify(tree.child(0), types);
          }
        }
        // x - 0 -> x
        if (operator.equals(Element.Operator.SUBTRACTION)) {
          if (hasValue(tree.child(1), 0)) {
            return simplify(tree.child(0), types);
          }
        }
        // x - x -> 0
        if (operator.equals(Element.Operator.SUBTRACTION)) {
          if (commutationEquals(tree.child(0), tree.child(1))) {
            return simplify(new Tree<>(new Element.Constant(0)), types);
          }
        }
        // x * 1 -> x; 1 * x -> x
        if (operator.equals(Element.Operator.MULTIPLICATION) && tree.children().size() == 2) {
          if (hasValue(tree.child(0), 1)) {
            return simplify(tree.child(1), types);
          }
          if (hasValue(tree.child(1), 1)) {
            return simplify(tree.child(0), types);
          }
        }
        // x / 1 -> x
        if (operator.equals(Element.Operator.DIVISION) || operator.equals(
            Element.Operator.PROT_DIVISION
        )) {
          if (hasValue(tree.child(1), 1)) {
            return simplify(tree.child(0), types);
          }
        }
        // x / x -> 1
        if (operator.equals(Element.Operator.DIVISION) || operator.equals(
            Element.Operator.PROT_DIVISION
        )) {
          if (commutationEquals(tree.child(0), tree.child(1))) {
            return simplify(new Tree<>(new Element.Constant(1)), types);
          }
        }
        // inv inv (x) -> x
        if (operator.equals(Element.Operator.INVERSE) && tree.child(0)
            .label()
            .equals(Element.Operator.INVERSE)) {
          return simplify(tree.child(0).child(0), types);
        }
        // - - (x) -> x
        if (operator.equals(Element.Operator.OPPOSITE) && tree.child(0)
            .label()
            .equals(Element.Operator.OPPOSITE)) {
          return simplify(tree.child(0).child(0), types);
        }
        // exp log (x) -> x
        if (operator.equals(Element.Operator.EXP) && (tree.child(0)
            .label()
            .equals(Element.Operator.LOG) || tree.child(0)
                .label()
                .equals(Element.Operator.PROT_LOG))) {
          return simplify(tree.child(0).child(0), types);
        }
        // log exp (x) -> x
        if ((operator.equals(Element.Operator.LOG) || operator.equals(Element.Operator.PROT_LOG)) && tree.child(0)
            .label()
            .equals(Element.Operator.EXP)) {
          return simplify(tree.child(0).child(0), types);
        }
        // sq sqrt (x) -> x
        if ((operator.equals(Element.Operator.SQ)) && tree.child(0)
            .label()
            .equals(Operator.SQRT)) {
          return simplify(tree.child(0).child(0), types);
        }
        // ternary (pos; x; y) -> x, ternary (pos; x; neg) -> y
        if (operator.equals(Operator.TERNARY)) {
          if (isPositive(tree.child(0))) {
            return simplify(tree.child(1), types);
          }
          if (isNegative(tree.child(0))) {
            return simplify(tree.child(2), types);
          }
        }
        // sqrt sq (x) -> x
        if ((operator.equals(Element.Operator.SQRT)) && tree.child(0)
            .label()
            .equals(Operator.SQ)) {
          return simplify(tree.child(0).child(0), types);
        }
        // x + x -> 2 x
        if (operator.equals(Element.Operator.ADDITION) && tree.children().size() == 2) {
          if (commutationEquals(tree.child(0), tree.child(1))) {
            return simplify(
                new Tree<>(
                    Element.Operator.MULTIPLICATION,
                    List.of(new Tree<>(new Element.Constant(2d)), tree.child(0))
                ),
                types
            );
          }
        }
        // (x + y) - x -> y; (y + x) - x -> y
        if (operator.equals(Element.Operator.SUBTRACTION)) {
          if (tree.child(0).label().equals(Element.Operator.ADDITION) && tree.child(0).children().size() == 2) {
            if (commutationEquals(tree.child(0).child(0), tree.child(1))) {
              return simplify(tree.child(0).child(1), types);
            }
            if (commutationEquals(tree.child(0).child(1), tree.child(1))) {
              return simplify(tree.child(0).child(0), types);
            }
          }
        }
        // x - (- y) -> x + y
        if (operator.equals(Element.Operator.SUBTRACTION)) {
          if (tree.child(1).label().equals(Operator.OPPOSITE)) {
            return simplify(
                new Tree<>(Operator.ADDITION, List.of(tree.child(0), tree.child(1).child(0))),
                types
            );
          }
        }
        // x + (y - x) -> y; (y - x) + x -> y
        if (operator.equals(Operator.ADDITION) && tree.children().size() == 2) {
          if (tree.child(1).label().equals(Operator.SUBTRACTION)) {
            if (commutationEquals(tree.child(1).child(1), tree.child(0))) {
              return simplify(tree.child(1).child(0), types);
            }
          }
          if (tree.child(0).label().equals(Operator.SUBTRACTION)) {
            if (commutationEquals(tree.child(0).child(1), tree.child(0))) {
              return simplify(tree.child(0).child(0), types);
            }
          }
        }
        // (x * y) / x -> y; (y * x) / x -> y
        if (operator.equals(Element.Operator.DIVISION) || operator.equals(
            Element.Operator.PROT_DIVISION
        )) {
          if (tree.child(0).label().equals(Element.Operator.MULTIPLICATION) && tree.child(0).children().size() == 2) {
            if (commutationEquals(tree.child(0).child(0), tree.child(1))) {
              return simplify(tree.child(0).child(1), types);
            }
            if (commutationEquals(tree.child(0).child(1), tree.child(1))) {
              return simplify(tree.child(0).child(0), types);
            }
          }
        }
        // (k * x) + x -> (1 + k) * x; (x * k) + x -> (1 + k) * x (and commuted)
        if (operator.equals(Element.Operator.ADDITION) && tree.children().size() == 2) {
          if (tree.child(0).label().equals(Element.Operator.MULTIPLICATION) && tree.child(0).children().size() == 2) {
            if (commutationEquals(tree.child(0).child(1), tree.child(1))) {
              return simplify(
                  new Tree<>(
                      Element.Operator.MULTIPLICATION,
                      List.of(
                          new Tree<>(
                              Element.Operator.ADDITION,
                              List.of(new Tree<>(new Element.Constant(1)), tree.child(0).child(0))
                          ),
                          tree.child(1)
                      )
                  ),
                  types
              );
            }
            if (commutationEquals(tree.child(0).child(0), tree.child(1))) {
              return simplify(
                  new Tree<>(
                      Element.Operator.MULTIPLICATION,
                      List.of(
                          new Tree<>(
                              Element.Operator.ADDITION,
                              List.of(new Tree<>(new Element.Constant(1)), tree.child(0).child(1))
                          ),
                          tree.child(1)
                      )
                  ),
                  types
              );
            }
          }
          if (tree.child(1).label().equals(Element.Operator.MULTIPLICATION) && tree.child(1).children().size() == 2) {
            if (commutationEquals(tree.child(1).child(1), tree.child(0))) {
              return simplify(
                  new Tree<>(
                      Element.Operator.MULTIPLICATION,
                      List.of(
                          new Tree<>(
                              Element.Operator.ADDITION,
                              List.of(new Tree<>(new Element.Constant(1)), tree.child(1).child(0))
                          ),
                          tree.child(0)
                      )
                  ),
                  types
              );
            }
            if (commutationEquals(tree.child(1).child(0), tree.child(0))) {
              return simplify(
                  new Tree<>(
                      Element.Operator.MULTIPLICATION,
                      List.of(
                          new Tree<>(
                              Element.Operator.ADDITION,
                              List.of(new Tree<>(new Element.Constant(1)), tree.child(1).child(1))
                          ),
                          tree.child(0)
                      )
                  ),
                  types
              );
            }
          }
        }
      }
    }
    // here structural
    if (types.contains(SimplificationType.STRUCTURAL)) {
      if (tree.label() instanceof Operator o) {
        if (o.equals(Operator.ADDITION) || o.equals(Operator.MULTIPLICATION)) {
          List<Tree<Element>> reducedChildren = operatorDescendants(o, tree);
          if (reducedChildren.size() != tree.children().size()) {
            return simplify(new Tree<>(o, reducedChildren), types);
          }
        }
      }
    }
    while (true) {
      Tree<Element> simplified = new Tree<>(
          tree.label(),
          tree.children().stream().map((Tree<Element> t) -> simplify(t, types)).toList()
      );
      if (simplified.equals(tree)) {
        return simplified;
      }
      tree = simplify(simplified, types);
    }
  }

}