/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
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

package io.github.ericmedvet.jgea.experimenter.builders;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Base64;
import java.util.function.Function;
import java.util.logging.Logger;

public class Functions {

  private static final Logger L = Logger.getLogger(Functions.class.getName());

  private Functions() {}

  @SuppressWarnings("unused")
  public static Function<String, Object> fromBase64() {
    return s -> {
      try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(s));
          ObjectInputStream ois = new ObjectInputStream(bais)) {
        return ois.readObject();
      } catch (Throwable t) {
        L.warning("Cannot deserialize: %s".formatted(t));
        return null;
      }
    };
  }

  @SuppressWarnings("unused")
  public static <T> Function<T, T> identity() {
    return t -> t;
  }
}
