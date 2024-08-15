/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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

package io.github.ericmedvet.jgea.experimenter;

import io.github.ericmedvet.jgea.core.listener.Listener;
import io.github.ericmedvet.jnb.core.Interpolator;
import io.github.ericmedvet.jnb.core.MapNamedParamMap;
import io.github.ericmedvet.jnb.core.ParamMap;
import io.github.ericmedvet.jnb.datastructure.TriConsumer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.SortedMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class Utils {

  protected static final Logger L = Logger.getLogger(Utils.class.getName());

  private Utils() {}

  public static String interpolate(String format, Run<?, ?, ?, ?> run) {
    ParamMap map = run.map();
    if (run.map() instanceof MapNamedParamMap mnpm) {
      SortedMap<MapNamedParamMap.TypedKey, Object> values = mnpm.getValues();
      values.put(new MapNamedParamMap.TypedKey("index", ParamMap.Type.INT), run.index());
      map = new MapNamedParamMap(mnpm.getName(), mnpm.getValues());
    }
    return Interpolator.interpolate(format, map, "_");
  }

  public static <I1, I2, I3> TriConsumer<I1, I2, I3> named(String name, TriConsumer<I1, I2, I3> consumer) {
    return new TriConsumer<>() {
      @Override
      public void accept(I1 i1, I2 i2, I3 i3) {
        consumer.accept(i1, i2, i3);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  public static <I1, I2> BiConsumer<I1, I2> named(String name, BiConsumer<I1, I2> consumer) {
    return new BiConsumer<>() {
      @Override
      public void accept(I1 i1, I2 i2) {
        consumer.accept(i1, i2);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  public static <I> Consumer<I> named(String name, Consumer<I> consumer) {
    return new Consumer<>() {
      @Override
      public void accept(I i) {
        consumer.accept(i);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  public static <E> Listener<E> named(String name, Listener<E> consumer) {
    return new Listener<>() {
      @Override
      public void listen(E e) {
        consumer.listen(e);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  public static String getCredentialFromFile(File credentialFile) {
    if (credentialFile == null) {
      throw new IllegalArgumentException("Credential file not provided");
    }
    try {
      String content = Files.readString(credentialFile.toPath());
      if (content.isEmpty()) {
        throw new IllegalArgumentException("Invalid credential file: empty");
      }
      if (content.lines().count() != 1) {
        throw new IllegalArgumentException("Invalid credential file: %d lines"
            .formatted(content.lines().count()));
      }
      String[] pieces = content.split("\\s");
      String credential = pieces[0];
      L.config(String.format("Using provided credential: %s", credentialFile));
      return credential;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
