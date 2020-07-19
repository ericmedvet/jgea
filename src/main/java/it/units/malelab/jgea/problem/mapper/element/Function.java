/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.problem.mapper.element;

/**
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

  Function(String grammarName) {
    this.grammarName = grammarName;
  }

  public String getGrammarName() {
    return grammarName;
  }

}
