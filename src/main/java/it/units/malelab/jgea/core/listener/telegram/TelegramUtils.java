package it.units.malelab.jgea.core.listener.telegram;

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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.logging.Logger;

public class TelegramUtils {

  protected static final Logger L = Logger.getLogger(TelegramUpdater.class.getName());
  protected static final Set<String> VIDEO_FILE_EXTENSIONS = Set.of("mpg", "avi", "mp4");

  protected TelegramBot bot;
  protected final long chatId;

  public TelegramUtils(String botToken, long chatId) {
    this.chatId = chatId;
    try {
      bot = new TelegramBot(botToken);
    } catch (RuntimeException e) {
      L.severe(String.format("Cannot create bot: %s", e));
    }
  }

  protected void sendText(String string) {
    try {
      SendResponse response = bot.execute(new SendMessage(
          chatId,
          string
      ));
      if (!response.isOk()) {
        L.warning(String.format("Response is not ok: %s", response.toString()));
      }
    } catch (Throwable t) {
      L.warning(String.format("Cannot send text: %s", t));
    }
  }

  protected void sendImage(BufferedImage image) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(image, "png", baos);
      baos.close();
      SendResponse response = bot.execute(new SendPhoto(
          chatId,
          baos.toByteArray()
      ));
      if (!response.isOk()) {
        L.warning(String.format("Response is not ok: %s", response.toString()));
      }
    } catch (Throwable t) {
      L.warning(String.format("Cannot send image: %s", t));
    }
  }

  protected void sendVideo(File file) {
    try {
      SendResponse response = bot.execute(new SendVideo(
          chatId,
          file
      ));
      if (!response.isOk()) {
        L.warning(String.format("Response is not ok: %s", response.toString()));
      }
    } catch (Throwable t) {
      L.warning(String.format("Cannot send video: %s", t));
    }
  }

  protected void sendDocument(File file) {
    try {
      SendResponse response = bot.execute(new SendDocument(
          chatId,
          file
      ));
      if (!response.isOk()) {
        L.warning(String.format("Response is not ok: %s", response.toString()));
      }
    } catch (Throwable t) {
      L.warning(String.format("Cannot send document: %s", t));
    }
  }

  protected static String getMachineName() {
    String user = System.getProperty("user.name");
    String hostName = "unknown";
    try {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      //ignore
    }
    return user + "@" + hostName;
  }


}
