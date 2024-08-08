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
package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.experimenter.Experiment;
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jnb.core.*;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jnb.datastructure.TriConsumer;
import io.github.ericmedvet.jviz.core.drawer.VideoBuilder;
import io.github.ericmedvet.jviz.core.util.VideoUtils;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.SortedMap;
import java.util.function.Function;
import javax.imageio.ImageIO;

@Discoverable(prefixTemplate = "ea.consumer|c")
public class Consumers {

  private Consumers() {}

  private static <I1, I2, I3> TriConsumer<I1, I2, I3> named(String name, TriConsumer<I1, I2, I3> consumer) {
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

  @SuppressWarnings("unused")
  public static TriConsumer<?, ?, ?> deaf() {
    return named("deaf", (i1, i2, i3) -> {});
  }

  @SuppressWarnings("unused")
  public static <X, O> TriConsumer<X, Run<?, ?, ?, ?>, Experiment> saver(
      @Param(value = "of", dNPM = "f.identity()") Function<X, O> f,
      @Param(value = "path", dS = "run-{run.index:%04d}") String filePathTemplate) {
    return named(
        "saver[%s]".formatted(NamedFunction.name(f)),
        (x, run, experiment) -> save(
            f.apply(x),
            Interpolator.interpolate(
                filePathTemplate,
                run == null
                    ? experiment.map()
                    : augment(
                        experiment.map(),
                        "run",
                        ParamMap.Type.NAMED_PARAM_MAP,
                        augment(run.map(), "index", ParamMap.Type.INT, run.index())),
                "_")));
  }

  private static ParamMap augment(ParamMap outer, String name, ParamMap.Type type, Object value) {
    if (outer instanceof MapNamedParamMap mnpm) {
      SortedMap<MapNamedParamMap.TypedKey, Object> values = mnpm.getValues();
      values.put(new MapNamedParamMap.TypedKey(name, type), value);
      outer = new MapNamedParamMap(mnpm.getName(), mnpm.getValues());
    }
    return outer;
  }

  private static void save(Object o, String filePath) {
    File file = null;
    try {
      if (o instanceof BufferedImage image) {
        file = Misc.checkExistenceAndChangeName(new File(filePath + ".png"));
        ImageIO.write(image, "png", file);
      } else if (o instanceof String s) {
        file = Misc.checkExistenceAndChangeName(new File(filePath + ".txt"));
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
          for (String l : s.lines().toList()) {
            bw.append(l);
            bw.newLine();
          }
        }
      } else if (o instanceof VideoBuilder.Video video) {
        file = Misc.checkExistenceAndChangeName(new File(filePath + ".mp4"));
        VideoUtils.encodeAndSave(video.images(), video.frameRate(), file);
      } else if (o instanceof byte[] data) {
        file = Misc.checkExistenceAndChangeName(new File(filePath + ".bin"));
        try (OutputStream os = new FileOutputStream(file)) {
          os.write(data);
        }
      } else if (o instanceof NamedParamMap npm) {
        save(MapNamedParamMap.prettyToString(npm), filePath);
      } else {
        throw new IllegalArgumentException(
            "Cannot save data of type %s".formatted(o.getClass().getSimpleName()));
      }
    } catch (IOException e) {
      throw new RuntimeException("Cannot save '%s'".formatted(file.getPath()), e);
    }
  }
}
