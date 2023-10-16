
package io.github.ericmedvet.jgea.core.representation.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
public class IndexedNode<C> implements Serializable {

  private final int index;
  private final C content;

  public IndexedNode(int index, C content) {
    this.index = index;
    this.content = content;
  }

  public static <H, K extends H> Function<K, IndexedNode<H>> hashMapper(Class<H> c) {
    return k -> new IndexedNode<>(Objects.hash(k.getClass(), k), k);
  }

  public static <H, K extends H> Function<K, IndexedNode<H>> incrementerMapper(Class<H> c) {
    return new Function<>() {
      private final List<K> nodes = new ArrayList<>();

      @Override
      public synchronized IndexedNode<H> apply(K k) {
        int index = nodes.indexOf(k);
        if (index == -1) {
          nodes.add(k);
          index = nodes.size() - 1;
        }
        return new IndexedNode<>(index, k);
      }
    };
  }

  public C content() {
    return content;
  }

  @Override
  public int hashCode() {
    return Objects.hash(index);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    IndexedNode<?> that = (IndexedNode<?>) o;
    return index == that.index;
  }

  @Override
  public String toString() {
    return content + "[" + index + "]";
  }

  public int index() {
    return index;
  }
}
