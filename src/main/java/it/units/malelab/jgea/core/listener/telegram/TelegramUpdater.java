package it.units.malelab.jgea.core.listener.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendVideo;
import com.pengrad.telegrambot.response.SendResponse;
import it.units.malelab.jgea.core.listener.Accumulator;
import it.units.malelab.jgea.core.listener.Listener;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author eric on 2021/01/03 for jgea
 */
public class TelegramUpdater<E> implements Listener.Factory<E> {

  private static final Logger L = Logger.getLogger(TelegramUpdater.class.getName());
  private static final Set<String> VIDEO_FILE_EXTENSIONS = Set.of("mpg", "avi", "mp4");

  private final long chatId;
  private final List<Accumulator.Factory<E, ?>> factories;

  private TelegramBot bot;

  public TelegramUpdater(
      List<Accumulator.Factory<E, ?>> factories,
      String botToken,
      long chatId
  ) {
    this.chatId = chatId;
    this.factories = factories;
    try {
      bot = new TelegramBot(botToken);
    } catch (RuntimeException e) {
      L.severe(String.format("Cannot create bot: %s", e));
    }
    sendText(String.format(
        "%s started on %s: will send updates with %d accumulators",
        TelegramUpdater.class.getSimpleName(),
        getMachineName(),
        factories.size()
    ));
  }

  @Override
  public void shutdown() {
    sendText(String.format(
        "%s shutting down on %s",
        TelegramUpdater.class.getSimpleName(),
        getMachineName()
    ));
  }

  @Override
  public Listener<E> build() {
    return new Listener<>() {
      private final List<Accumulator<E, ?>> accumulators = factories.stream().map(Accumulator.Factory::build).collect(Collectors.toList());

      @Override
      public void listen(E e) {
        accumulators.forEach(a -> a.listen(e));
      }

      @Override
      public void done() {
        List<Object> outcomes = new ArrayList<>();
        sendText(String.format(
            "done() on %s",
            getMachineName()
        ));
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
          if (outcome instanceof String) {
            sendText((String) outcome);
          } else if (outcome instanceof BufferedImage) {
            sendImage((BufferedImage) outcome);
          } else if (outcome instanceof File) {
            File file = (File) outcome;
            if (!file.exists()) {
              L.info(String.format(
                  "File %s does not exist, cannot send",
                  file
              ));
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

  private void sendText(String string) {
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

  private void sendImage(BufferedImage image) {
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

  private void sendVideo(File file) {
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

  private void sendDocument(File file) {
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

  private static String getMachineName() {
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
