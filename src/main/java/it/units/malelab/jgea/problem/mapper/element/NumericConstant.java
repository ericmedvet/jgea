/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.mapper.element;

/**
 *
 * @author eric
 */
public class NumericConstant implements Element {
  
  private final double value;

  public NumericConstant(double value) {
    this.value = value;
  }

  public double getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.format("%f", value);
  }

}
