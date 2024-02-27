/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.jgea.core.representation.tree.booleanfunction;

import java.io.Serializable;

public interface Element {

  enum Operator implements Element, Serializable {
    AND(".and"),
    AND1NOT(".and1not"),
    OR(".or"),
    XOR(".xor"),
    NOT(".not"),
    IF(".if");

    private final String string;

    Operator(String string) {
      this.string = string;
    }

    @Override
    public String toString() {
      return string;
    }
  }

  record Constant(boolean value) implements Element, Serializable {

    @Override
    public String toString() {
      return Boolean.toString(value);
    }
  }

  record Decoration(String string) implements Element, Serializable {}

  record Variable(String name) implements Element, Serializable {}

  String toString();
}
