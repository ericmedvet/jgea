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

package it.units.malelab.jgea.representation.sequence;

import java.util.*;

/**
 * @author eric
 */
public interface ThinList<T> extends List<T> {

  @Override
  default Iterator<T> iterator() {
    return new Iterator<T>() {
      int i = 0;

      @Override
      public boolean hasNext() {
        return i < size();
      }

      @Override
      public T next() {
        i = i + 1;
        return get(i - 1);
      }
    };
  }

  @Override
  default boolean isEmpty() {
    return size() == 0;
  }

  @Override
  default boolean contains(Object o) {
    for (T t : this) {
      if (t.equals(o)) {
        return true;
      }
    }
    return false;
  }

  @Override
  default Object[] toArray() {
    Object[] array = new Object[size()];
    for (int i = 0; i < array.length; i++) {
      array[i] = get(i);
    }
    return array;
  }

  @Override
  @SuppressWarnings("unchecked")
  default <T1> T1[] toArray(T1[] a) {
    return (T1[]) toArray();
  }

  @Override
  default boolean remove(Object o) {
    return remove(indexOf(o)) != null;
  }

  @Override
  default boolean containsAll(Collection<?> c) {
    for (Object o : c) {
      if (!contains(o)) {
        return false;
      }
    }
    return true;
  }

  @Override
  default boolean addAll(Collection<? extends T> c) {
    int initialSize = size();
    for (T t : c) {
      add(t);
    }
    return size() != initialSize;
  }

  @Override
  default boolean addAll(int index, Collection<? extends T> c) {
    List<T> tail = subList(index, size() - 1);
    for (int i = index; i < size(); i++) {
      remove(index);
    }
    boolean changed = addAll(c);
    addAll(tail);
    return changed;
  }

  @Override
  default boolean removeAll(Collection<?> c) {
    boolean changed = false;
    for (Object o : c) {
      changed = changed || remove(o);
    }
    return changed;
  }

  @Override
  default boolean retainAll(Collection<?> c) {
    List<T> toRemove = new ArrayList<>();
    for (T t : this) {
      if (!c.contains(t)) {
        toRemove.add(t);
      }
    }
    return removeAll(toRemove);
  }

  @Override
  default void clear() {
    removeAll(this);
  }

  @Override
  default void add(int index, T element) {
    addAll(index, Collections.singleton(element));
  }

  @Override
  default int indexOf(Object o) {
    for (int i = 0; i < size(); i++) {
      if (get(i).equals(o)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  default int lastIndexOf(Object o) {
    for (int i = size() - 1; i >= 0; i--) {
      if (get(i).equals(o)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  default ListIterator<T> listIterator() {
    return new ArrayList<>(this).listIterator();
  }

  @Override
  default ListIterator<T> listIterator(int index) {
    return subList(index, size()).listIterator();
  }

  @Override
  default List<T> subList(int fromIndex, int toIndex) {
    List<T> sub = new ArrayList<>(toIndex - fromIndex);
    for (int i = fromIndex; i < toIndex; i++) {
      sub.add(get(i));
    }
    return sub;
  }
}
