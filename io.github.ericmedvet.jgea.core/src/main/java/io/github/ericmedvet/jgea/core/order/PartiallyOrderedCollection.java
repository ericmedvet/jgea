
package io.github.ericmedvet.jgea.core.order;

import io.github.ericmedvet.jgea.core.util.Copyable;
import io.github.ericmedvet.jgea.core.util.Sized;

import java.util.Collection;
import java.util.List;
public interface PartiallyOrderedCollection<T> extends Sized, Copyable {
  void add(T t);

  Collection<T> all();

  Collection<T> firsts();

  Collection<T> lasts();

  boolean remove(T t);

  @Override
  default PartiallyOrderedCollection<T> immutableCopy() {
    final PartiallyOrderedCollection<T> inner = this;
    return new PartiallyOrderedCollection<T>() {
      final Collection<T> all = List.copyOf(inner.all());
      final Collection<T> firsts = List.copyOf(inner.firsts());
      final Collection<T> lasts = List.copyOf(inner.lasts());

      @Override
      public void add(T t) {
        throw new UnsupportedOperationException("Read-only instance");
      }

      @Override
      public Collection<T> all() {
        return all;
      }

      @Override
      public Collection<T> firsts() {
        return firsts;
      }

      @Override
      public Collection<T> lasts() {
        return lasts;
      }

      @Override
      public boolean remove(T t) {
        throw new UnsupportedOperationException("Read-only instance");
      }
    };
  }

  @Override
  default int size() {
    return all().size();
  }
}
