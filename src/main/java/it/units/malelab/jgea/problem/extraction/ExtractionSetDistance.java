/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.extraction;

import com.google.common.collect.Range;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.distance.Distance;
import java.util.Set;

/**
 *
 * @author eric
 */
public class ExtractionSetDistance implements Distance<Set<Range<Integer>>> {
  
  private final int length;
  private final int bins;

  public ExtractionSetDistance(int length, int bins) {
    this.length = length;
    this.bins = bins;
  }

  @Override
  public Double apply(Set<Range<Integer>> ranges1, Set<Range<Integer>> ranges2, Listener listener) throws FunctionException {
    boolean[] mask1 = new boolean[bins+1];
    boolean[] mask2 = new boolean[bins+1];
    for (Range<Integer> range : ranges1) {
      mask1[(int)Math.floor((double)range.lowerEndpoint()/(double)length*(double)bins)] = true;
      mask1[(int)Math.floor((double)range.upperEndpoint()/(double)length*(double)bins)] = true;
    }
    for (Range<Integer> range : ranges2) {
      mask2[(int)Math.floor((double)range.lowerEndpoint()/(double)length*(double)bins)] = true;
      mask2[(int)Math.floor((double)range.upperEndpoint()/(double)length*(double)bins)] = true;
    }
    double count = 0;
    for (int i = 0; i<bins; i++) {
      count = count+((mask1[i]==mask2[i])?1:0);
    }
    return ((double)length-count)/(double)length;
  }
  
}
