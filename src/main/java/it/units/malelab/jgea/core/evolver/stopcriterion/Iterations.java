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
public class Iterations implements StoppingCondition {
  
  private final int n;

  public Iterations(int n) {
    this.n = n;
  }

  public int getN() {
    return n;
  }
  
}
