/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;

/**
 *
 * @author eric
 */
public class DoubleArrayPrinter implements Function<double[], String> {
  
  private final String[] formats;

  public DoubleArrayPrinter(String... formats) {
    this.formats = formats;
    if (formats.length==0) {
      formats = new String[]{"%s"};
    }
  }

  @Override
  public String apply(double[] a, Listener listener) throws FunctionException {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i<a.length; i++) {
      sb.append(String.format(formats[i%formats.length], a[i]));
      if (i<a.length-1) {
        sb.append(";");
      }
    }
    sb.append("]");
    return sb.toString();
  }

}
