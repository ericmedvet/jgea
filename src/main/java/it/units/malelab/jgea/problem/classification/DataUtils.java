/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.classification;

import it.units.malelab.jgea.core.util.Pair;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author eric
 */
public class DataUtils {

  public static <O, L> List<Pair<O, L>> fold(List<Pair<O, L>> data, int i, int n, Random random) {
    List<Pair<O, L>> subset = new ArrayList<>();
    Set<L> labels = new LinkedHashSet<>();
    for (Pair<O, L> pair : data) {
      labels.add(pair.getSecond());
    }
    for (L label : labels) {
      
    }
    return subset;
  }

}
