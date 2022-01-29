package it.units.malelab.jgea.core.listener.telegram;

import it.units.malelab.jgea.core.listener.Accumulator;
import it.units.malelab.jgea.core.listener.Factory;
import it.units.malelab.jgea.core.listener.Listener;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author eric on 2021/01/03 for jgea
 */
public class TelegramUpdater<E, K> extends TelegramClient implements Factory<E, K> {

  private final List<Accumulator.Factory<E, ?, K>> factories;

  public TelegramUpdater(
      List<Accumulator.Factory<E, ?, K>> factories, String botToken, long chatId
  ) {
    super(botToken, chatId);
    this.factories = factories;
    sendText(String.format(
        "%s started on %s: will send updates with %d accumulators",
        TelegramUpdater.class.getSimpleName(),
        getMachineName(),
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
        sendText(String.format("done() on %s", getMachineName()));
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
    sendText(String.format("%s shutting down on %s", TelegramUpdater.class.getSimpleName(), getMachineName()));
  }

}
