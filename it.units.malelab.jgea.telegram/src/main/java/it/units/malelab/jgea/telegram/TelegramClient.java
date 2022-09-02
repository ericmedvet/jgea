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

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendVideo;
import com.pengrad.telegrambot.response.SendResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Set;
import java.util.logging.Logger;

public class TelegramClient {

  protected static final Logger L = Logger.getLogger(TelegramClient.class.getName());
  protected static final Set<String> VIDEO_FILE_EXTENSIONS = Set.of("mpg", "avi", "mp4");
  protected final long chatId;
  protected TelegramBot bot;

  public TelegramClient(String botToken, long chatId) {
    this.chatId = chatId;
    try {
      bot = new TelegramBot(botToken);
    } catch (RuntimeException e) {
      L.severe(String.format("Cannot create bot: %s", e));
    }
  }

  public void sendDocument(File file) {
    try {
      SendResponse response = bot.execute(new SendDocument(chatId, file));
      if (!response.isOk()) {
        L.warning(String.format("Response is not ok: %s", response));
      }
    } catch (Throwable t) {
      L.warning(String.format("Cannot send document: %s", t));
    }
  }

  public void sendImage(BufferedImage image) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(image, "png", baos);
      baos.close();
      SendResponse response = bot.execute(new SendPhoto(chatId, baos.toByteArray()));
      if (!response.isOk()) {
        L.warning(String.format("Response is not ok: %s", response));
      }
    } catch (Throwable t) {
      L.warning(String.format("Cannot send image: %s", t));
    }
  }

  public void sendText(String string) {
    try {
      SendResponse response = bot.execute(new SendMessage(chatId, string));
      if (!response.isOk()) {
        L.warning(String.format("Response is not ok: %s", response));
      }
    } catch (Throwable t) {
      L.warning(String.format("Cannot send text: %s", t));
    }
  }

  public void sendVideo(File file) {
    try {
      SendResponse response = bot.execute(new SendVideo(chatId, file));
      if (!response.isOk()) {
        L.warning(String.format("Response is not ok: %s", response));
      }
    } catch (Throwable t) {
      L.warning(String.format("Cannot send video: %s", t));
    }
  }


}
