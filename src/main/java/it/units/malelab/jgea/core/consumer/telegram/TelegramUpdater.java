package it.units.malelab.jgea.core.consumer.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import it.units.malelab.jgea.core.consumer.Consumer;
import it.units.malelab.jgea.core.consumer.Event;
import okhttp3.OkHttpClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author eric on 2021/01/03 for jgea
 */
public class TelegramUpdater<G, S, F> implements Consumer.Factory<G, S, F, Void> {

  private static final Logger L = Logger.getLogger(TelegramUpdater.class.getName());

  private final long chatId;
  private final List<Consumer.Factory<G, S, F, ?>> factories;
  private final List<Function<Collection<? extends S>, ?>> functions;

  private final OkHttpClient client;
  private TelegramBot bot;

  public TelegramUpdater(
      String botToken,
      long chatId,
      List<Consumer.Factory<G, S, F, ?>> factories,
      List<Function<Collection<? extends S>, ?>> functions
  ) {
    this.chatId = chatId;
    this.factories = factories;
    this.functions = functions;
    client = new OkHttpClient();
    try {
      bot = new TelegramBot.Builder(botToken).okHttpClient(client).build();
    } catch (RuntimeException e) {
      L.severe(String.format("Cannot create bot: %s", e));
    }
  }

  @Override
  public Consumer<G, S, F, Void> build() {
    return new Consumer<>() {
      private final List<Consumer<G, S, F, ?>> consumers = factories.stream().map(Factory::build).collect(Collectors.toList());

      @Override
      public void consume(Event<? extends G, ? extends S, ? extends F> event) {
        consumers.forEach(consumer -> consumer.consume(event));
      }

      @Override
      public void consume(Collection<? extends S> solutions) {
        List<Object> outcomes = new ArrayList<>();
        //consume accumulators
        for (Consumer<G, S, F, ?> consumer : consumers) {
          try {
            outcomes.add(consumer.produce());
          } catch (Throwable e) {
            L.warning(String.format(
                "Cannot get outcome of accumulator %s: %s",
                consumer.getClass().getSimpleName(),
                e
            ));
          }
        }
        for (Function<Collection<? extends S>, ?> processor : functions) {
          try {
            processor.apply(solutions);
          } catch (Throwable e) {
            L.warning(String.format(
                "Cannot processo solutions with %s: %s",
                processor.getClass().getSimpleName(),
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
      bot.execute(new SendMessage(
          chatId,
          string
      ));
    } catch (Throwable t) {
      L.warning(String.format("Cannot send text: %s", t));
    }
  }

  private void sendImage(BufferedImage image) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(image, "png", baos);
      baos.close();
      bot.execute(new SendPhoto(
          chatId,
          baos.toByteArray()
      ));
    } catch (Throwable t) {
      L.warning(String.format("Cannot send image: %s", t));
    }
  }

}
