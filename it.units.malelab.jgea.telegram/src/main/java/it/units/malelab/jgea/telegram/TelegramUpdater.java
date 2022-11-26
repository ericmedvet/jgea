/*
 * Copyright 2022 eric
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

package it.units.malelab.jgea.telegram;

import it.units.malelab.jgea.core.listener.*;
import it.units.malelab.jgea.core.util.StringUtils;
import it.units.malelab.jgea.core.util.TextPlotter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author eric on 2021/01/03 for jgea
 */
public class TelegramUpdater<E, K> extends TelegramClient implements ListenerFactory<E, K>, ProgressMonitor {

  private final List<AccumulatorFactory<E, ?, K>> factories;

  public TelegramUpdater(
      List<AccumulatorFactory<E, ?, K>> factories, String botToken, long chatId
  ) {
    super(botToken, chatId);
    this.factories = factories;
    sendText(String.format(
        "%s started on %s: will send updates with %d accumulators",
        TelegramUpdater.class.getSimpleName(),
        StringUtils.getMachineName(),
        factories.size()
    ));
  }

  @Override
  public Listener<E> build(K k) {
    return new Listener<>() {
      private final List<? extends Accumulator<E, ?>> accumulators = factories.stream().map(f -> f.build(k)).toList();

      @Override
      public void listen(E e) {
        accumulators.forEach(a -> a.listen(e));
      }

      @Override
      public void done() {
        List<Object> outcomes = new ArrayList<>();
        sendText(String.format("done() on %s", StringUtils.getMachineName()));
        //consume accumulators
        for (Accumulator<E, ?> accumulator : accumulators) {
          try {
            outcomes.add(accumulator.get());
          } catch (Throwable e) {
            L.warning(String.format(
                "Cannot get outcome of accumulator %s: %s",
                accumulator.getClass().getSimpleName(),
                e
            ));
          }
        }
        //consume outcomes
        for (Object outcome : outcomes) {
          if (outcome instanceof String string) {
            sendText(string);
          } else if (outcome instanceof BufferedImage image) {
            sendImage(image);
          } else if (outcome instanceof File file) {
            if (!file.exists()) {
              L.info(String.format("File %s does not exist, cannot send", file));
            } else {
              if (VIDEO_FILE_EXTENSIONS.stream().anyMatch(e -> file.getPath().endsWith("." + e))) {
                sendVideo(file);
              } else {
                sendDocument(file);
              }
            }
          } else {
            L.info(String.format(
                "Skip outcome of accumulator: do not know how to handle %s",
                outcome.getClass().getSimpleName()
            ));
          }
        }
      }
    };
  }

  @Override
  public void shutdown() {
    sendText(String.format(
        "%s shutting down on %s",
        TelegramUpdater.class.getSimpleName(),
        StringUtils.getMachineName()
    ));
  }

  @Override
  public void notify(double progress, String message) {
    sendText(String.format(
        "%s - progress %s %s",
        StringUtils.getMachineName(),
        TextPlotter.horizontalBar(progress, 0, 1, 8),
        message
    ));
  }

  @Override
  public void notify(double progress) {
    sendText(String.format(
        "%s - progress %s",
        StringUtils.getMachineName(),
        TextPlotter.horizontalBar(progress, 0, 1, 8)
    ));
  }

}
