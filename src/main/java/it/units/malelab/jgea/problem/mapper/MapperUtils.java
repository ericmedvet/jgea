/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.mapper;

import com.google.common.collect.Range;
import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.representation.sequence.bit.BitString;
import it.units.malelab.jgea.representation.grammar.ge.HierarchicalMapper;
import it.units.malelab.jgea.problem.mapper.element.Element;
import it.units.malelab.jgea.problem.mapper.element.Function;
import it.units.malelab.jgea.problem.mapper.element.NumericConstant;
import it.units.malelab.jgea.problem.mapper.element.Variable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author eric
 */
public class MapperUtils {

  public static Node<Element> transform(Node<String> stringNode) {
    if (stringNode.getChildren().isEmpty()) {
      Element element = fromString(stringNode.getContent());
      if (element == null) {
        return null;
      }
      return new Node<>(element);
    }
    if (stringNode.getChildren().size() == 1) {
      return transform(stringNode.getChildren().get(0));
    }
    Node<Element> node = transform(stringNode.getChildren().get(0));
    for (int i = 1; i < stringNode.getChildren().size(); i++) {
      Node<Element> child = transform(stringNode.getChildren().get(i));
      if (child != null) { //discard decorations
        node.getChildren().add(child);
      }
    }
    return node;
  }

  private static Element fromString(String string) {
    try {
      double value = Double.parseDouble(string);
      return new NumericConstant(value);
    } catch (NumberFormatException ex) {
      //just ignore
    }
    for (Variable variable : Variable.values()) {
      if (variable.getGrammarName().equals(string)) {
        return variable;
      }
    }
    for (Function function : Function.values()) {
      if (function.getGrammarName().equals(string)) {
        return function;
      }
    }
    return null;
  }

  public static Object compute(Node<Element> node, BitString g, List<Double> values, int depth, AtomicInteger globalCounter) {
    Object result = null;
    if (node.getContent() instanceof Variable) {
      switch (((Variable) node.getContent())) {
        case GENOTYPE:
          result = g;
          break;
        case LIST_N:
          result = values;
          break;
        case DEPTH:
          result = (double) depth;
          break;
        case GL_COUNT_R:
          result = (double) globalCounter.get();
          break;
        case GL_COUNT_RW:
          result = (double) globalCounter.getAndIncrement();
          break;
      }
    } else if (node.getContent() instanceof Function) {
      switch (((Function) node.getContent())) {
        case SIZE:
          result = (double) ((BitString) compute(node.getChildren().get(0), g, values, depth, globalCounter)).size();
          break;
        case WEIGHT:
          result = (double) ((BitString) compute(node.getChildren().get(0), g, values, depth, globalCounter)).count();
          break;
        case WEIGHT_R:
          BitString bitsGenotype = (BitString) compute(node.getChildren().get(0), g, values, depth, globalCounter);
          result = (double) bitsGenotype.count() / (double) bitsGenotype.size();
          break;
        case INT:
          result = (double) ((BitString) compute(node.getChildren().get(0), g, values, depth, globalCounter)).toInt();
          break;
        case ADD:
          result = ((Double) compute(node.getChildren().get(0), g, values, depth, globalCounter)
                  + (Double) compute(node.getChildren().get(1), g, values, depth, globalCounter));
          break;
        case SUBTRACT:
          result = ((Double) compute(node.getChildren().get(0), g, values, depth, globalCounter)
                  - (Double) compute(node.getChildren().get(1), g, values, depth, globalCounter));
          break;
        case MULT:
          result = ((Double) compute(node.getChildren().get(0), g, values, depth, globalCounter)
                  * (Double) compute(node.getChildren().get(1), g, values, depth, globalCounter));
          break;
        case DIVIDE:
          result = protectedDivision(
                  (Double) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  (Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)
          );
          break;
        case REMAINDER:
          result = protectedRemainder(
                  (Double) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  (Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)
          );
          break;
        case LENGTH:
          result = (double) ((List) compute(node.getChildren().get(0), g, values, depth, globalCounter)).size();
          break;
        case MAX_INDEX:
          result = (double) maxIndex((List<Double>) compute(node.getChildren().get(0), g, values, depth, globalCounter), 1d);
          break;
        case MIN_INDEX:
          result = (double) maxIndex((List<Double>) compute(node.getChildren().get(0), g, values, depth, globalCounter), -1d);
          break;
        case GET:
          result = getFromList(
                  (List) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  ((Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)).intValue()
          );
          break;
        case SEQ:
          result = seq(
                  ((Double) compute(node.getChildren().get(0), g, values, depth, globalCounter)).intValue(),
                  values.size()
          );
          break;
        case REPEAT:
          result = repeat(
                  compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  ((Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)).intValue(),
                  values.size()
          );
          break;
        case ROTATE_SX:
          result = rotateSx(
                  (BitString) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  ((Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)).intValue()
          );
          break;
        case ROTATE_DX:
          result = rotateDx(
                  (BitString) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  ((Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)).intValue()
          );
          break;
        case SUBSTRING:
          result = substring(
                  (BitString) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  ((Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)).intValue()
          );
          break;
        case SPLIT:
          result = split(
                  (BitString) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  ((Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)).intValue(),
                  values.size()
          );
          break;
        case SPLIT_W:
          result = splitWeighted(
                  (BitString) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  (List<Double>) compute(node.getChildren().get(1), g, values, depth, globalCounter),
                  values.size()
          );
          break;
        case APPLY:
          result = apply(
                  (Function) node.getChildren().get(0).getContent(),
                  ((List) compute(node.getChildren().get(1), g, values, depth, globalCounter)),
                  (node.getChildren().size() >= 3) ? compute(node.getChildren().get(2), g, values, depth, globalCounter) : null
          );
          break;
      }
    } else if (node.getContent() instanceof NumericConstant) {
      result = ((NumericConstant) node.getContent()).getValue();
    }
    return result;
  }

  private static double protectedDivision(double d1, double d2) {
    if (d2 == 0) {
      return 0d;
    }
    return d1 / d2;
  }

  private static double protectedRemainder(double d1, double d2) {
    if (d2 == 0) {
      return 0d;
    }
    return d1 % d2;
  }

  private static BitString rotateDx(BitString g, int n) {
    if (g.size() == 0) {
      return g;
    }
    n = n % g.size();
    if (n <= 0) {
      return g;
    }
    BitString copy = new BitString(g.size());
    copy.set(0, g.slice(g.size() - n, g.size()));
    copy.set(n, g.slice(0, g.size() - n));
    return copy;
  }

  private static BitString rotateSx(BitString g, int n) {
    if (g.size() == 0) {
      return g;
    }
    n = n % g.size();
    if (n <= 0) {
      return g;
    }
    BitString copy = new BitString(g.size());
    copy.set(0, g.slice(n, g.size()));
    copy.set(g.size() - n, g.slice(0, n));
    return copy;
  }

  private static BitString substring(BitString g, int to) {
    if (to <= 0) {
      return new BitString(0);
    }
    if (g.size() == 0) {
      return g;
    }
    return g.slice(0, Math.min(to, g.size()));
  }

  private static List<BitString> split(BitString g, int n, int maxN) {
    if (n <= 0) {
      return Collections.singletonList(g);
    }
    if (n > maxN) {
      n = maxN;
    }
    if (g.size() == 0) {
      return Collections.nCopies(n, new BitString(0));
    }
    n = Math.max(1, n);
    n = Math.min(n, g.size());
    List<Range<Integer>> ranges = HierarchicalMapper.slices(Range.closedOpen(0, g.size()), n);
    return g.slices(ranges);
  }

  private static List<BitString> splitWeighted(BitString g, List<Double> weights, int maxN) {
    if (weights.isEmpty()) {
      return Collections.singletonList(g);
    }
    if (g.size() == 0) {
      return Collections.nCopies(weights.size(), new BitString(0));
    }
    double minWeight = Double.POSITIVE_INFINITY;
    for (double w : weights) {
      if ((w < minWeight) && (w > 0)) {
        minWeight = w;
      }
    }
    if (Double.isInfinite(minWeight)) {
      //all zero
      return split(g, weights.size(), maxN);
    }
    List<Integer> intWeights = new ArrayList<>(weights.size());
    for (double w : weights) {
      intWeights.add((int) Math.max(Math.round(w / minWeight), 0d));
    }
    List<Range<Integer>> ranges = HierarchicalMapper.slices(Range.closedOpen(0, g.size()), intWeights);
    return g.slices(ranges);
  }

  private static List list(Object item) {
    List l = new ArrayList(1);
    l.add(item);
    return l;
  }

  private static List concat(List l1, List l2) {
    List l = new ArrayList(l1);
    l.addAll(l2);
    return l;
  }

  private static List apply(Function function, List inputList, Object arg) {
    List outputList = new ArrayList(inputList.size());
    for (Object repeatedArg : inputList) {
      switch (function) {
        case SIZE:
          outputList.add((double) ((BitString) repeatedArg).size());
          break;
        case WEIGHT:
          outputList.add((double) ((BitString) repeatedArg).count());
          break;
        case WEIGHT_R:
          outputList.add((double) ((BitString) repeatedArg).count() / (double) ((BitString) repeatedArg).size());
          break;
        case INT:
          outputList.add((double) ((BitString) repeatedArg).toInt());
          break;
        case ROTATE_SX:
          outputList.add(rotateSx((BitString) arg, ((Double) repeatedArg).intValue()));
          break;
        case ROTATE_DX:
          outputList.add(rotateDx((BitString) arg, ((Double) repeatedArg).intValue()));
          break;
        case SUBSTRING:
          outputList.add(substring((BitString) arg, ((Double) repeatedArg).intValue()));
          break;
      }
    }
    return outputList;
  }

  private static <T> List<T> repeat(T element, int n, int maxN) {
    if (n <= 0) {
      return Collections.singletonList(element);
    }
    if (n > maxN) {
      n = maxN;
    }
    List<T> list = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      list.add(element);
    }
    return list;
  }

  private static <T> T getFromList(List<T> list, int n) {
    n = Math.min(n, list.size() - 1);
    n = Math.max(0, n);
    return list.get(n);
  }

  private static int maxIndex(List<Double> list, double mult) {
    if (list.isEmpty()) {
      return 0;
    }
    int index = 0;
    for (int i = 1; i < list.size(); i++) {
      if (mult * list.get(i) > mult * list.get(index)) {
        index = i;
      }
    }
    return index;
  }

  private static List<Double> seq(int n, int maxN) {
    if (n > maxN) {
      n = maxN;
    }
    if (n < 1) {
      n = 1;
    }
    List<Double> list = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      list.add((double) i);
    }
    return list;
  }
  
  private static <T> Node<T> node(T content, Node<T>... children) {
    Node<T> node = new Node<>(content);
    for (Node<T> child : children) {
      node.getChildren().add(child);
    }
    return node;
  }

  public static Node<String> getGERawTree(int codonLength) {
    return node("<mapper>",
            node("<n>",
                    node("<fun_n_g>",
                            node("int")
                    ),
                    node("("),
                    node("<g>",
                            node("<fun_g_g,n>",
                                    node("substring")
                            ),
                            node("("),
                            node("<g>",
                                    node("<fun_g_g,n>",
                                            node("rotate_sx")
                                    ),
                                    node("("),
                                    node("<g>",
                                            node("<var_g>",
                                                    node("g")
                                            )
                                    ),
                                    node(","),
                                    node("<n>",
                                            node("<fun_n_n,n>",
                                                    node("*")
                                            ),
                                            node("("),
                                            node("<n>",
                                                    node("<var_n>",
                                                            node("g_count_rw")
                                                    )),
                                            node(","),
                                            node("<n>",
                                                    node("<const_n>",
                                                            node(Integer.toString(codonLength))
                                                    )),
                                            node(")")
                                    ),
                                    node(")")
                            ),
                            node(","),
                            node("<n>",
                                    node("<const_n>",
                                            node(Integer.toString(codonLength))
                                    )
                            ),
                            node(")")
                    ),
                    node(")")
            ),
            node("<lg>",
                    node("<fun_lg_g,n>",
                            node("repeat")
                    ),
                    node("("),
                    node("<g>",
                            node("<var_g>",
                                    node("g")
                            )
                    ),
                    node(","),
                    node("<n>",
                            node("<fun_n_ln>",
                                    node("length")
                            ),
                            node("("),
                            node("<ln>",
                                    node("<var_ln>",
                                            node("ln")
                                    )
                            ),
                            node(")")
                    ),
                    node(")")
            )
    );
  }

  public static Node<String> getWHGERawTree() {
    return node("<mapper>",
            node("<n>",
                    node("<fun_n_ln>",
                            node("max_index")
                    ),
                    node("("),
                    node("<ln>",
                            node("apply"),
                            node("("),
                            node("<fun_n_g>",
                                    node("weight_r")),
                            node(","),
                            node("<lg>",
                                    node("<fun_lg_g,n>",
                                            node("split")
                                    ),
                                    node("("),
                                    node("<g>",
                                            node("<var_g>",
                                                    node("g")
                                            )
                                    ),
                                    node(","),
                                    node("<n>",
                                            node("<fun_n_ln>",
                                                    node("length")
                                            ),
                                            node("("),
                                            node("<ln>",
                                                    node("<var_ln>",
                                                            node("ln")
                                                    )
                                            ),
                                            node(")")
                                    ),
                                    node(")")
                            ),
                            node(")")
                    ),
                    node(")")
            ),
            node("<lg>",
                    node("<fun_lg_g,ln>",
                            node("split_w")
                    ),
                    node("("),
                    node("<g>",
                            node("<var_g>",
                                    node("g")
                            )
                    ),
                    node(","),
                    node("<ln>",
                            node("<var_ln>",
                                    node("ln")
                            )
                    ),
                    node(")")
            )
    );
  }

  public static Node<String> getHGERawTree() {
    return node("<mapper>",
            node("<n>",
                    node("<fun_n_ln>",
                            node("max_index")
                    ),
                    node("("),
                    node("<ln>",
                            node("apply"),
                            node("("),
                            node("<fun_n_g>",
                                    node("weight_r")),
                            node(","),
                            node("<lg>",
                                    node("<fun_lg_g,n>",
                                            node("split")
                                    ),
                                    node("("),
                                    node("<g>",
                                            node("<var_g>",
                                                    node("g")
                                            )
                                    ),
                                    node(","),
                                    node("<n>",
                                            node("<fun_n_ln>",
                                                    node("length")
                                            ),
                                            node("("),
                                            node("<ln>",
                                                    node("<var_ln>",
                                                            node("ln")
                                                    )
                                            ),
                                            node(")")
                                    ),
                                    node(")")
                            ),
                            node(")")
                    ),
                    node(")")
            ),
            node("<lg>",
                    node("<fun_lg_g,n>",
                            node("split")
                    ),
                    node("("),
                    node("<g>",
                            node("<var_g>",
                                    node("g")
                            )
                    ),
                    node(","),
                    node("<n>",
                            node("<fun_n_ln>",
                                    node("length")
                            ),
                            node("("),
                            node("<ln>",
                                    node("<var_ln>",
                                            node("ln")
                                    )
                            ),
                            node(")")
                    ),
                    node(")")
            )
    );
  }

}
