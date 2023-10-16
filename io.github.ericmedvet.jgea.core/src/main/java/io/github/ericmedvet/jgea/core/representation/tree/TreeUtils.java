
package io.github.ericmedvet.jgea.core.representation.tree;
public class TreeUtils {

  private TreeUtils() {
  }

  public static <N> Tree<N> replaceAll(Tree<N> t, Tree<N> oldT, Tree<N> newT) {
    if (t.equals(oldT)) {
      return Tree.copyOf(newT);
    }
    Tree<N> rebuilt = Tree.of(t.content());
    t.childStream().map(c -> replaceAll(c, oldT, newT)).forEach(rebuilt::addChild);
    return rebuilt;
  }

  public static <N> Tree<N> replaceFirst(Tree<N> t, Tree<N> oldT, Tree<N> newT) {
    if (t.equals(oldT)) {
      return Tree.copyOf(newT);
    }
    Tree<N> rebuilt = Tree.of(t.content());
    boolean replaced = false;
    for (Tree<N> c : t) {
      Tree<N> newC = replaced ? c : replaceFirst(c, oldT, newT);
      replaced = replaced || !newC.equals(c);
      rebuilt.addChild(newC);
    }
    return rebuilt;
  }
}
