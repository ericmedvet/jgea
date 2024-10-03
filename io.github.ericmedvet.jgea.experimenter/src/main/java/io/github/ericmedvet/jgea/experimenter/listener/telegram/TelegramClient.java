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

package io.github.ericmedvet.jgea.experimenter.listener.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.GetChatMemberCountResponse;
import com.pengrad.telegrambot.response.GetChatResponse;
import com.pengrad.telegrambot.response.SendResponse;
import io.github.ericmedvet.jgea.core.util.StringUtils;
import io.github.ericmedvet.jgea.experimenter.Utils;
import io.github.ericmedvet.jnb.core.MapNamedParamMap;
import io.github.ericmedvet.jnb.core.NamedParamMap;
import io.github.ericmedvet.jviz.core.drawer.Video;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class TelegramClient {

  protected static final Logger L = Logger.getLogger(TelegramClient.class.getName());
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

  public TelegramClient(File credentialFile, long chatId) {
    this(Utils.getCredentialFromFile(credentialFile), chatId);
  }

  public String getChatInfo() {
    GetChatResponse chatResponse = bot.execute(new GetChat(chatId));
    GetChatMemberCountResponse chatMemberCountResponse = bot.execute(new GetChatMemberCount(chatId));
    String title = chatResponse.chat().title();
    if (title == null) {
      title = chatResponse.chat().firstName() + " " + chatResponse.chat().lastName();
    }
    return "%s (%d members)".formatted(title, chatMemberCountResponse.count());
  }

  public void send(String title, Object o) {
    try {
      switch (o) {
        case BufferedImage image -> {
          if (!title.isEmpty()) {
            sendMarkdownText("Image from: %s\n`%s`".formatted(StringUtils.getUserMachineName(), title));
          }
          sendImage(image);
        }
        case String s -> {
          if (!title.isEmpty()) {
            sendMarkdownText("Text from: %s\n`%s`".formatted(StringUtils.getUserMachineName(), title));
          }
          sendText(s);
        }
        case Video video -> {
          if (!title.isEmpty()) {
            sendMarkdownText("Video from: %s\n`%s`".formatted(StringUtils.getUserMachineName(), title));
          }
          sendVideo(video);
        }
        case NamedParamMap npm -> {
          if (!title.isEmpty()) {
            sendMarkdownText(
                "NamedParamMap from: %s\n`%s`".formatted(StringUtils.getUserMachineName(), title));
          }
          sendText(MapNamedParamMap.prettyToString(npm));
        }
        case null -> throw new IllegalArgumentException("Cannot send null data of type %s");
        default -> throw new IllegalArgumentException(
            "Cannot send data of type %s".formatted(o.getClass().getSimpleName()));
      }
    } catch (IOException e) {
      throw new RuntimeException("Cannot send '%s'".formatted(title), e);
    }
  }

  public void sendFile(File file) {
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

  public void sendMarkdownText(String string) {
    try {
      SendResponse response = bot.execute(new SendMessage(chatId, string).parseMode(ParseMode.Markdown));
      if (!response.isOk()) {
        L.warning(String.format("Response is not ok: %s", response));
      }
    } catch (Throwable t) {
      L.warning(String.format("Cannot send text: %s", t));
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

  public void sendVideo(Video video) throws IOException {
    try {
      SendResponse response = bot.execute(new SendVideo(chatId, video.data()));
      if (!response.isOk()) {
        L.warning(String.format("Response is not ok: %s", response));
      }
    } catch (Throwable t) {
      L.warning(String.format("Cannot send video: %s", t));
    }
  }
}
