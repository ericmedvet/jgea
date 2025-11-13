/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2025 Eric Medvet
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

import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jnb.core.Interpolator;
import io.github.ericmedvet.jnb.core.MapNamedParamMap;
import io.github.ericmedvet.jnb.core.NamedParamMap;
import io.github.ericmedvet.jnb.core.ParamMap;
import io.github.ericmedvet.jviz.core.drawer.Video;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class Utils {

  protected static final Logger L = Logger.getLogger(Utils.class.getName());

  private Utils() {
  }

  public static void save(Object o, String filePath, boolean overwrite) {
    File file = null;
    try {
      file = Misc.robustGetFile(filePath, overwrite);
      switch (o) {
        case BufferedImage image -> {
          ImageIO.write(image, "png", file);
        }
        case String s -> {
          Files.writeString(
              file.toPath(),
              s,
              StandardOpenOption.WRITE,
              StandardOpenOption.CREATE,
              StandardOpenOption.TRUNCATE_EXISTING
          );
        }
        case Video video -> {
          Files.write(
              file.toPath(),
              video.data(),
              StandardOpenOption.WRITE,
              StandardOpenOption.CREATE,
              StandardOpenOption.TRUNCATE_EXISTING
          );
        }
        case byte[] data -> {
          try (OutputStream os = new FileOutputStream(file)) {
            os.write(data);
          }
        }
        case NamedParamMap npm -> {
          Files.writeString(
              file.toPath(),
              MapNamedParamMap.prettyToString(npm),
              StandardOpenOption.WRITE,
              StandardOpenOption.CREATE,
              StandardOpenOption.TRUNCATE_EXISTING
          );
        }
        case null -> throw new IllegalArgumentException("Cannot save null data of type %s");
        default -> throw new IllegalArgumentException(
            "Cannot save data of type %s".formatted(o.getClass().getSimpleName())
        );
      }
    } catch (IOException e) {
      throw new RuntimeException(
          "Cannot save '%s'".formatted(Objects.isNull(file) ? filePath : file.getPath()),
          e
      );
    }
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
        throw new IllegalArgumentException(
            "Invalid credential file: %d lines"
                .formatted(content.lines().count())
        );
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
          "run",
          run.map().with("index", run.index())
      );
    }
    return Interpolator.interpolate(format, map, "_");
  }
}
