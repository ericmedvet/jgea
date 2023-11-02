/*-
 * ========================LICENSE_START=================================
 * jgea-telegram
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

package io.github.ericmedvet.jgea.experimenter.listener.telegram;

import io.github.ericmedvet.jgea.core.listener.*;
import io.github.ericmedvet.jgea.core.util.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class TelegramUpdater<E, K> extends TelegramClient implements ListenerFactory<E, K>, ProgressMonitor {

  private final List<AccumulatorFactory<E, ?, K>> factories;

  public TelegramUpdater(List<AccumulatorFactory<E, ?, K>> factories, String botToken, long chatId) {
    super(botToken, chatId);
    this.factories = factories;
    sendText(String.format(
        "%s started on %s: will send updates with %d accumulators",
        TelegramUpdater.class.getSimpleName(), StringUtils.getUserMachineName(), factories.size()));
  }

  @Override
  public Listener<E> build(K k) {
    return new Listener<>() {
      private final List<? extends Accumulator<E, ?>> accumulators =
          factories.stream().map(f -> f.build(k)).toList();

      @Override
      public void listen(E e) {
        accumulators.forEach(a -> a.listen(e));
      }

      @Override
      public void done() {
        sendText(String.format("done() on %s", StringUtils.getUserMachineName()));
        // consume accumulators
        for (int i = 0; i < factories.size(); i++) {
          try {
            Object outcome = accumulators.get(i).get();
            if (outcome instanceof String string) {
              sendText(string);
            } else if (outcome instanceof BufferedImage image) {
              sendImage(image);
            } else if (outcome instanceof File file) {
              if (!file.exists()) {
                L.info(String.format("File %s does not exist, cannot send", file));
              } else {
                if (VIDEO_FILE_EXTENSIONS.stream()
                    .anyMatch(e -> file.getPath().endsWith("." + e))) {
                  sendVideo(file);
                } else {
                  sendDocument(file);
                }
              }
            } else if (outcome instanceof Table<?>) {
              if (factories.get(i) instanceof XYPlotTableBuilder<?> plotBuilder) {
                //noinspection unchecked
                BufferedImage plot = ImagePlotters.xyLines(
                        plotBuilder.getWidth(), plotBuilder.getHeight())
                    .apply((Table<? extends Number>) outcome);
                sendImage(plot);
              } else {
                L.info(String.format(
                    "Skip table outcome of accumulator: do not know how to handle %s",
                    outcome.getClass().getSimpleName()));
              }
            } else {
              L.info(String.format(
                  "Skip outcome of accumulator: do not know how to handle %s",
                  outcome.getClass().getSimpleName()));
            }
          } catch (Throwable e) {
            L.warning(String.format(
                "Cannot get outcome of accumulator %s: %s",
                accumulators.get(i).getClass().getSimpleName(), e));
          }
        }
      }
    };
  }

  @Override
  public void shutdown() {
    sendText(String.format(
        "%s shutting down on %s", TelegramUpdater.class.getSimpleName(), StringUtils.getUserMachineName()));
  }

  @Override
  public void notify(Progress progress, String message) {
    sendText(String.format(
        "%s - progress %s %s",
        StringUtils.getUserMachineName(), TextPlotter.horizontalBar(progress.rate(), 0, 1, 8), message));
  }
}
