/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.symbolicregression.element;

import it.units.malelab.jgea.problem.symbolicregression.RealFunction;

/**
 * @author eric
 */
public enum Operator implements Element, RealFunction {

  ADDITION("+", x -> x[0] + x[1]),
  SUBTRACTION("-", x -> x[0] - x[1]),
  DIVISION("/", x -> x[0] / x[1]),
  PROT_DIVISION("p/", x -> (x[1] != 0d) ? (x[0] / x[1]) : 1),
  MULTIPLICATION("*", x -> x[0] * x[1]),
  LOG("log", x -> Math.log(x[0])),
  PROT_LOG("plog", x -> (x[0] > 0d) ? Math.log(x[0]) : 0d),
  EXP("exp", x -> Math.exp(x[0])),
  SIN("sin", x -> Math.sin(x[0])),
  COS("cos", x -> Math.cos(x[0])),
  INVERSE("1/", x -> 1d / x[0]),
  OPPOSITE("_", x -> 0d - x[0]),
  SQRT("√", x -> Math.sqrt(x[0])),
  SQ("²", x -> Math.pow(x[0], 2d));

  private final String string;
  private final RealFunction function;

  Operator(String string, RealFunction function) {
    this.string = string;
    this.function = function;
  }

  @Override
  public String toString() {
    return string;
  }


  @Override
  public double apply(double... input) {
    return function.apply(input);
  }
}
