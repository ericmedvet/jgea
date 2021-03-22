/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.malelab.jgea.problem.mapper;

import com.google.common.collect.Range;
import it.units.malelab.jgea.problem.mapper.element.Element;
import it.units.malelab.jgea.problem.mapper.element.MapperFunction;
import it.units.malelab.jgea.problem.mapper.element.NumericConstant;
import it.units.malelab.jgea.problem.mapper.element.Variable;
import it.units.malelab.jgea.representation.grammar.ge.HierarchicalMapper;
import it.units.malelab.jgea.representation.sequence.bit.BitString;
import it.units.malelab.jgea.representation.tree.Tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author eric
 */
public class MapperUtils {

  public static Tree<Element> transform(Tree<String> stringTree) {
    if (stringTree.isLeaf()) {
      Element element = fromString(stringTree.content());
      if (element == null) {
        return null;
      }
      return Tree.of(element);
    }
    if (stringTree.nChildren() == 1) {
      return transform(stringTree.child(0));
    }
    Tree<Element> tree = transform(stringTree.child(0));
    for (int i = 1; i < stringTree.nChildren(); i++) {
      Tree<Element> child = transform(stringTree.child(i));
      if (child != null) { //discard decorations
        tree.addChild(child);
      }
    }
    return tree;
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
    for (MapperFunction function : MapperFunction.values()) {
      if (function.getGrammarName().equals(string)) {
        return function;
      }
    }
    return null;
  }

  public static Object compute(Tree<Element> tree, BitString g, List<Double> values, int depth, AtomicInteger globalCounter) {
    Object result = null;
    if (tree.content() instanceof Variable) {
      switch (((Variable) tree.content())) {
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
    } else if (tree.content() instanceof MapperFunction) {
      switch (((MapperFunction) tree.content())) {
        case SIZE:
          result = (double) ((BitString) compute(tree.child(0), g, values, depth, globalCounter)).size();
          break;
        case WEIGHT:
          result = (double) ((BitString) compute(tree.child(0), g, values, depth, globalCounter)).count();
          break;
        case WEIGHT_R:
          BitString bitsGenotype = (BitString) compute(tree.child(0), g, values, depth, globalCounter);
          result = (double) bitsGenotype.count() / (double) bitsGenotype.size();
          break;
        case INT:
          result = (double) ((BitString) compute(tree.child(0), g, values, depth, globalCounter)).toInt();
          break;
        case ADD:
          result = ((Double) compute(tree.child(0), g, values, depth, globalCounter)
              + (Double) compute(tree.child(1), g, values, depth, globalCounter));
          break;
        case SUBTRACT:
          result = ((Double) compute(tree.child(0), g, values, depth, globalCounter)
              - (Double) compute(tree.child(1), g, values, depth, globalCounter));
          break;
        case MULT:
          result = ((Double) compute(tree.child(0), g, values, depth, globalCounter)
              * (Double) compute(tree.child(1), g, values, depth, globalCounter));
          break;
        case DIVIDE:
          result = protectedDivision(
              (Double) compute(tree.child(0), g, values, depth, globalCounter),
              (Double) compute(tree.child(1), g, values, depth, globalCounter)
          );
          break;
        case REMAINDER:
          result = protectedRemainder(
              (Double) compute(tree.child(0), g, values, depth, globalCounter),
              (Double) compute(tree.child(1), g, values, depth, globalCounter)
          );
          break;
        case LENGTH:
          result = (double) ((List) compute(tree.child(0), g, values, depth, globalCounter)).size();
          break;
        case MAX_INDEX:
          result = (double) maxIndex((List<Double>) compute(tree.child(0), g, values, depth, globalCounter), 1d);
          break;
        case MIN_INDEX:
          result = (double) maxIndex((List<Double>) compute(tree.child(0), g, values, depth, globalCounter), -1d);
          break;
        case GET:
          result = getFromList(
              (List) compute(tree.child(0), g, values, depth, globalCounter),
              ((Double) compute(tree.child(1), g, values, depth, globalCounter)).intValue()
          );
          break;
        case SEQ:
          result = seq(
              ((Double) compute(tree.child(0), g, values, depth, globalCounter)).intValue(),
              values.size()
          );
          break;
        case REPEAT:
          result = repeat(
              compute(tree.child(0), g, values, depth, globalCounter),
              ((Double) compute(tree.child(1), g, values, depth, globalCounter)).intValue(),
              values.size()
          );
          break;
        case ROTATE_SX:
          result = rotateSx(
              (BitString) compute(tree.child(0), g, values, depth, globalCounter),
              ((Double) compute(tree.child(1), g, values, depth, globalCounter)).intValue()
          );
          break;
        case ROTATE_DX:
          result = rotateDx(
              (BitString) compute(tree.child(0), g, values, depth, globalCounter),
              ((Double) compute(tree.child(1), g, values, depth, globalCounter)).intValue()
          );
          break;
        case SUBSTRING:
          result = substring(
              (BitString) compute(tree.child(0), g, values, depth, globalCounter),
              ((Double) compute(tree.child(1), g, values, depth, globalCounter)).intValue()
          );
          break;
        case SPLIT:
          result = split(
              (BitString) compute(tree.child(0), g, values, depth, globalCounter),
              ((Double) compute(tree.child(1), g, values, depth, globalCounter)).intValue(),
              values.size()
          );
          break;
        case SPLIT_W:
          result = splitWeighted(
              (BitString) compute(tree.child(0), g, values, depth, globalCounter),
              (List<Double>) compute(tree.child(1), g, values, depth, globalCounter),
              values.size()
          );
          break;
        case APPLY:
          result = apply(
              (MapperFunction) tree.child(0).content(),
              ((List) compute(tree.child(1), g, values, depth, globalCounter)),
              (tree.nChildren() >= 3) ? compute(tree.child(2), g, values, depth, globalCounter) : null
          );
          break;
      }
    } else if (tree.content() instanceof NumericConstant) {
      result = ((NumericConstant) tree.content()).getValue();
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

  private static List apply(MapperFunction function, List inputList, Object arg) {
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

  private static <T> Tree<T> node(T content, Tree<T>... children) {
    Tree<T> tree = Tree.of(content);
    for (Tree<T> child : children) {
      tree.addChild(child);
    }
    return tree;
  }

  public static Tree<String> getGERawTree(int codonLength) {
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

  public static Tree<String> getWHGERawTree() {
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

  public static Tree<String> getHGERawTree() {
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
