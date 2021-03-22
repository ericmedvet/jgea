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

package it.units.malelab.jgea.problem.mapper.element;

/**
 * @author eric
 */
public enum MapperFunction implements Element {

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

  MapperFunction(String grammarName) {
    this.grammarName = grammarName;
  }

  public String getGrammarName() {
    return grammarName;
  }

}
