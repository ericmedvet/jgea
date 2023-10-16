
package io.github.ericmedvet.jgea.problem;

import java.util.List;
import java.util.stream.IntStream;
public class DataUtils {

  public static <E> List<E> fold(List<E> items, int fold, int n) {
    return folds(items, List.of(fold), n);
  }

  public static <E> List<E> folds(List<E> items, List<Integer> folds, int n) {
    return IntStream.range(0, items.size())
        .filter(i -> folds.contains(i % n))
        .mapToObj(items::get)
        .toList();
  }

}
