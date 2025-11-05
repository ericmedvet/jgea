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
package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.util.Naming;
import io.github.ericmedvet.jgea.experimenter.Experiment;
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jgea.experimenter.Utils;
import io.github.ericmedvet.jgea.experimenter.listener.telegram.TelegramClient;
import io.github.ericmedvet.jnb.core.*;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jnb.datastructure.TriConsumer;
import java.io.File;
import java.util.function.Function;

@Discoverable(prefixTemplate = "ea.consumer|c")
public class Consumers {

  private Consumers() {
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, Y, O> TriConsumer<X, Run<?, ?, ?, ?>, Experiment> composed(
      @Param(value = "of", dNPM = "f.identity()") Function<X, Y> f,
      @Param(value = "f", dNPM = "f.identity()") Function<Y, O> innerF,
      @Param(value = "c") TriConsumer<O, Run<?, ?, ?, ?>, Experiment> consumer
  ) {
    return Naming.named(
        "%s[f=%s]".formatted(consumer, NamedFunction.name(f)),
        (TriConsumer<X, Run<?, ?, ?, ?>, Experiment>) (x, run, experiment) -> consumer.accept(
            innerF.apply(f.apply(x)),
            run,
            experiment
        )
    );
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static TriConsumer<?, ?, ?> deaf() {
    return Naming.named("deaf", (i1, i2, i3) -> {
    });
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, O> TriConsumer<X, Run<?, ?, ?, ?>, Experiment> saver(
      @Param(value = "of", dNPM = "f.identity()") Function<X, O> f,
      @Param(value = "overwrite") boolean overwrite,
      @Param(value = "path", dS = "run-{run.index:%04d}") String filePathTemplate,
      @Param(value = "suffix", dS = "") String suffix
  ) {
    return Naming.named(
        "saver[%s;%s]".formatted(
            NamedFunction.name(f),
            filePathTemplate + (overwrite ? "(*)" : "")
        ),
        (TriConsumer<X, Run<?, ?, ?, ?>, Experiment>) (x, run, experiment) -> Utils.save(
            f.apply(x),
            Utils.interpolate(filePathTemplate, experiment, run) + suffix,
            overwrite
        )
    );
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, O> TriConsumer<X, Run<?, ?, ?, ?>, Experiment> telegram(
      @Param(value = "of", dNPM = "f.identity()") Function<X, O> f,
      @Param(
          value = "title", dS = // spotless:off
          """
              Experiment:
              \t{name}
              Run {run.index}:
              \tSolver: {run.solver\
              .name}
              \tProblem: {run.problem.name}
              \tSeed: {run.randomGenerator.seed}""" // spotless:on
      ) String titleTemplate,
      @Param("chatId") String chatId,
      @Param("botIdFilePath") String botIdFilePath
  ) {
    TelegramClient client = new TelegramClient(new File(botIdFilePath), Long.parseLong(chatId));
    return Naming.named(
        "telegram[%s→to:%s]".formatted(NamedFunction.name(f), chatId),
        (TriConsumer<X, Run<?, ?, ?, ?>, Experiment>) (x, run, experiment) -> client.send(
            Utils.interpolate(titleTemplate, experiment, run),
            f.apply(x)
        )
    );
  }
}
