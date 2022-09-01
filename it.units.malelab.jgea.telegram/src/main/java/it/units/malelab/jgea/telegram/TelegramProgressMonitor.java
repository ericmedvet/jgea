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

import it.units.malelab.jgea.core.listener.ProgressMonitor;
import it.units.malelab.jgea.core.util.TextPlotter;

public class TelegramProgressMonitor extends TelegramClient implements ProgressMonitor {

  public TelegramProgressMonitor(String botToken, long chatId) {
    super(botToken, chatId);
  }

  @Override
  public void notify(double progress, String message) {
    sendText(String.format(
        "%s - progress %s %s",
        getMachineName(),
        TextPlotter.horizontalBar(progress, 0, 1, 8),
        message
    ));
  }

  @Override
  public void notify(double progress) {
    sendText(String.format("%s - progress %s", getMachineName(), TextPlotter.horizontalBar(progress, 0, 1, 8)));
  }
}
