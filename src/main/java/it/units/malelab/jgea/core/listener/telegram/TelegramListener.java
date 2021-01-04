package it.units.malelab.jgea.core.listener.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import it.units.malelab.jgea.core.listener.Accumulator;
import it.units.malelab.jgea.core.listener.Event;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.NamedFunction;
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
public class TelegramListener<G, S, F> implements Listener<G, S, F> {

  public static <G, S, F> Accumulator<G, S, F, String> lastEventPrinter(List<NamedFunction<Event<? extends G, ? extends S, ? extends F>, ?>> functions) {
    return new Accumulator<>() {

      private Event<? extends G, ? extends S, ? extends F> lastEvent;

      @Override
      public void clear() {
      }

      @Override
      public String get() {
        return functions.stream()
            .map(f -> String.format(
                "%s : " + f.getFormat() + "",
                f.getName(),
                f.apply(lastEvent)
            ))
            .collect(Collectors.joining("\n"));
      }

      @Override
      public void listen(Event<? extends G, ? extends S, ? extends F> event) {
        lastEvent = event;
      }
    };
  }

  private static final Logger L = Logger.getLogger(TelegramListener.class.getName());

  private final long chatId;
  private final List<Accumulator<G, S, F, ?>> accumulators;
  private final List<Function<Collection<? extends S>, ?>> solutionsProcessors;

  private final OkHttpClient client;
  private TelegramBot bot;

  public TelegramListener(
      String botToken,
      long chatId,
      List<Accumulator<G, S, F, ?>> accumulators,
      List<Function<Collection<? extends S>, ?>> solutionsProcessors
  ) {
    this.chatId = chatId;
    this.accumulators = accumulators;
    this.solutionsProcessors = solutionsProcessors;
    client = new OkHttpClient();
    try {
      bot = new TelegramBot.Builder(botToken).okHttpClient(client).build();
    } catch (RuntimeException e) {
      L.severe(String.format("Cannot create bot: %s", e));
    }
  }

  @Override
  public void listen(Event<? extends G, ? extends S, ? extends F> event) {
    accumulators.forEach(a -> a.listen(event));
  }

  @Override
  public void listenSolutions(Collection<? extends S> solutions) {
    List<Object> outcomes = new ArrayList<>();
    //consume accumulators
    for (Accumulator<G, S, F, ?> accumulator : accumulators) {
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
    for (Function<Collection<? extends S>, ?> processor : solutionsProcessors) {
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
    //clear series
    accumulators.forEach(Accumulator::clear);
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
