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

import io.github.ericmedvet.jnb.core.Interpolator;
import io.github.ericmedvet.jnb.core.MapNamedParamMap;
import io.github.ericmedvet.jnb.core.ParamMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.logging.Logger;

public class Utils {

  protected static final Logger L = Logger.getLogger(Utils.class.getName());

  private Utils() {}

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

  public static String interpolate(String format, Experiment experiment, Run<?, ?, ?, ?> run) {
    ParamMap map = new MapNamedParamMap("experiment", Map.of());
    if (experiment != null) {
      map = experiment.map();
    }
    if (run != null) {
      map = map.with(
          "run", ParamMap.Type.NAMED_PARAM_MAP, run.map().with("index", ParamMap.Type.INT, run.index()));
    }
    return Interpolator.interpolate(format, map, "_");
  }
}
