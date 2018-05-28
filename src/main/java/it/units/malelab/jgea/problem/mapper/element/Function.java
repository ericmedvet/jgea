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
public enum Function implements Element {

  SIZE("size"), WEIGHT("weight"), WEIGHT_R("weight_r"), INT("int"),
  ADD("+"), SUBTRACT("-"), MULT("*"), DIVIDE("/"), REMAINDER("%"),
  LENGTH("length"), MAX_INDEX("max_index"), MIN_INDEX("min_index"),
  GET("get"),
  SEQ("seq"),
  REPEAT("repeat"),
  ROTATE_DX("rotate_dx"), ROTATE_SX("rotate_sx"), SUBSTRING("substring"),
  SPLIT("split"),
  SPLIT_W("split_w"),
  APPLY("apply");

  private final String grammarName;

  private Function(String grammarName) {
    this.grammarName = grammarName;
  }

  public String getGrammarName() {
    return grammarName;
  }

}
