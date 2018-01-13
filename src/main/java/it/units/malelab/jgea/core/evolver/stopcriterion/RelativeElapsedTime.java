/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver.stopcriterion;

/**
 *
 * @author eric
 */
public class RelativeElapsedTime {
  
  private final double r;

  public RelativeElapsedTime(double r) {
    this.r = r;
  }

  public double getR() {
    return r;
  }
  
}
