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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.symbolicregression.element;

import it.units.malelab.jgea.problem.symbolicregression.RealFunction;

import java.util.function.ToIntFunction;

/**
 * @author eric
 */
public enum Operator implements Element, RealFunction {

  ADDITION("+", x -> x[0] + x[1], 2),
  SUBTRACTION("-", x -> x[0] - x[1], 2),
  DIVISION("/", x -> x[0] / x[1], 2),
  PROT_DIVISION("p/", x -> (x[1] != 0d) ? (x[0] / x[1]) : 1, 2),
  MULTIPLICATION("*", x -> x[0] * x[1], 2),
  LOG("log", x -> Math.log(x[0]), 1),
  PROT_LOG("plog", x -> (x[0] > 0d) ? Math.log(x[0]) : 0d, 1),
  EXP("exp", x -> Math.exp(x[0]), 1),
  SIN("sin", x -> Math.sin(x[0]), 1),
  COS("cos", x -> Math.cos(x[0]), 1),
  INVERSE("1/", x -> 1d / x[0], 1),
  OPPOSITE("_", x -> 0d - x[0], 1),
  SQRT("√", x -> Math.sqrt(x[0]), 1),
  SQ("²", x -> Math.pow(x[0], 2d), 1);

  private final String string;
  private final RealFunction function;
  private final int arity;

  Operator(String string, RealFunction function, int arity) {
    this.string = string;
    this.function = function;
    this.arity = arity;
  }

  @Override
  public String toString() {
    return string;
  }

  public int arity() {
    return arity;
  }

  @Override
  public double apply(double... input) {
    return function.apply(input);
  }

  public static ToIntFunction<Element> arityFunction() {
    return e -> (e instanceof Operator) ? ((Operator) e).arity : 0;
  }
}
