package it.units.malelab.jgea.core.order;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public interface TotallyOrderedCollection<T> extends PartiallyOrderedCollection<T> {

  List<T> allOrdered();

  static <T> TotallyOrderedCollection<T> from(PartiallyOrderedCollection<T> partiallyOrderedCollection, Comparator<T> comparator) {
    return new TotallyOrderedCollection<>() {

      @Override
      public List<T> allOrdered() {
        return partiallyOrderedCollection.sorted(comparator);
      }

      @Override
      public void add(T t) {
        partiallyOrderedCollection.add(t);
      }

      @Override
      public Collection<T> all() {
        return partiallyOrderedCollection.all();
      }

      @Override
      public Collection<T> firsts() {
        return partiallyOrderedCollection.firsts();
      }

      @Override
      public Collection<T> lasts() {
        return partiallyOrderedCollection.lasts();
      }

      @Override
      public boolean remove(T t) {
        return partiallyOrderedCollection.remove(t);
      }
    };
  }

}
