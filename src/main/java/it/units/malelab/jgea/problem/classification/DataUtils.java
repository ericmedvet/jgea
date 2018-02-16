/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.classification;

import it.units.malelab.jgea.core.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class DataUtils {

  public static <O, L> List<Pair<O, L>> fold(List<Pair<O, L>> data, int i, int n) {
    List<Pair<O, L>> subset = new ArrayList<>();
    data.stream().map(Pair::second).distinct().forEach((L l) -> {
      List<Pair<O, L>> currentSubset = data.stream()
              .filter((Pair<O, L> pair) -> (pair.second().equals(l)))
              .collect(Collectors.toList());
      subset.addAll(
              currentSubset.stream()
              .skip(currentSubset.size() / n * i)
              .limit(currentSubset.size() / n).collect(Collectors.toList()));
    });
    return subset;
  }

}
