package it.units.malelab.jgea.core.listener.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import it.units.malelab.jgea.core.listener.Event;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.NamedFunction;
import it.units.malelab.jgea.core.util.Pair;
import okhttp3.OkHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author eric on 2021/01/03 for jgea
 */
public class TelegramListener<G, S, F> implements Listener<G, S, F> {

  private static class PointFunction<G, S, F> {
    private final NamedFunction<Event<? extends G, ? extends S, ? extends F>, ? extends Number> xFunction;
    private final NamedFunction<Event<? extends G, ? extends S, ? extends F>, ? extends Number> yFunction;

    public PointFunction(NamedFunction<Event<? extends G, ? extends S, ? extends F>, ? extends Number> xFunction, NamedFunction<Event<? extends G, ? extends S, ? extends F>, ? extends Number> yFunction) {
      this.xFunction = xFunction;
      this.yFunction = yFunction;
    }

  }

  public static <G, S, F> PointFunction<G, S, F> xy(NamedFunction<Event<? extends G, ? extends S, ? extends F>, ? extends Number> xFunction, NamedFunction<Event<? extends G, ? extends S, ? extends F>, ? extends Number> yFunction) {
    return new PointFunction<>(xFunction, yFunction);
  }

  private static final Logger L = Logger.getLogger(TelegramListener.class.getName());

  private final long chatId;
  private final List<NamedFunction<Event<? extends G, ? extends S, ? extends F>, ?>> summaryFunctions;
  private final List<PointFunction<G, S, F>> pointFunctions;
  private final List<Function<Collection<? extends S>, byte[]>> resultsPlotters;

  private final List<List<Pair<Number, Number>>> series;

  private Event<? extends G, ? extends S, ? extends F> lastEvent;

  private OkHttpClient client;
  private TelegramBot bot;


  public TelegramListener(
      String botToken,
      long chatId,
      List<NamedFunction<Event<? extends G, ? extends S, ? extends F>, ?>> summaryFunctions,
      List<PointFunction<G, S, F>> pointFunctions,
      List<Function<Collection<? extends S>, byte[]>> resultsPlotters
  ) {
    this.chatId = chatId;
    this.summaryFunctions = summaryFunctions;
    this.pointFunctions = pointFunctions;
    this.resultsPlotters = resultsPlotters;
    series = pointFunctions.stream().map(p -> new ArrayList<Pair<Number, Number>>()).collect(Collectors.toList());
    client = new OkHttpClient();
    try {
      bot = new TelegramBot.Builder(botToken).okHttpClient(client).build();
    } catch (RuntimeException e) {
      L.severe(String.format("Cannot create bot: %s", e));
    }
  }

  @Override
  public void listen(Event<? extends G, ? extends S, ? extends F> event) {
    for (int i = 0; i < pointFunctions.size(); i++) {
      Number x = pointFunctions.get(i).xFunction.apply(event);
      Number y = pointFunctions.get(i).yFunction.apply(event);
      series.get(i).add(Pair.of(x, y));
    }
    lastEvent = event;
  }

  @Override
  public void listenSolutions(Collection<? extends S> solutions) {
    //send last event
    String msg = summaryFunctions.stream()
        .map(f -> String.format(
            "%s : " + f.getFormat() + "",
            f.getName(),
            f.apply(lastEvent)
        ))
        .collect(Collectors.joining("\n"));
    try {
      bot.execute(new SendMessage(
          chatId,
          msg
      ));
    } catch (RuntimeException e) {
      L.severe(String.format("Cannot creat bot: %s", e));
    }
    //do xy plots
    series.forEach(s -> System.out.printf(
        "x: %s%ny: %s%n",
        s.stream().map(Pair::first).collect(Collectors.toList()),
        s.stream().map(Pair::second).collect(Collectors.toList())
    ));
    //do solutions plots
    //clear series
    series.forEach(List::clear);
  }

  public static void main(String[] args) {
    OkHttpClient client = new OkHttpClient();
    TelegramBot bot = new TelegramBot.Builder("xxx").okHttpClient(client).build();
    long chatId = 207490209;
    bot.setUpdatesListener(updates -> {
      updates.forEach(u -> {
        System.out.println(u.message().chat().id());
        System.out.println(u.message().from());
        System.out.println(u.message().text());
        if (u.message().text().equals("/img")) {
          bot.execute(new SendPhoto(
              u.message().chat().id(),
              getImage("/home/eric/Immagini/io-bn.jpg")
          ));
        } else if (u.message().text().equals("/chatId")) {
          bot.execute(new SendMessage(
              u.message().chat().id(),
              String.format("It's `%d`", u.message().chat().id())
          ).parseMode(ParseMode.MarkdownV2));
        } else if (u.message().text().equals("/hw")) {
          bot.execute(new SendMessage(
              chatId,
              "ehi"
          ).parseMode(ParseMode.MarkdownV2));
        } else if (u.message().text().equals("/hw2")) {
          bot.execute(new SendMessage(
              u.message().chat().id(),
              "ehi"
          ).parseMode(ParseMode.MarkdownV2));
        } else {
          bot.execute(new SendMessage(
              u.message().chat().id(),
              String.format("I'm only a listener `%s` doesn't mean anything to me", u.message().text().replaceAll("[\\W]+", " "))
          ).parseMode(ParseMode.MarkdownV2));
        }
      });
      return UpdatesListener.CONFIRMED_UPDATES_ALL;
    });
    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(
        () -> {
          System.out.println(bot.execute(new SendMessage(
              chatId,
              "hello **world**!"
          )));
        },
        1, 10, TimeUnit.SECONDS);
    if (false) {
      bot.setUpdatesListener(null);
      client.dispatcher().executorService().shutdown();
      client.connectionPool().evictAll();
    }
  }

  private static byte[] getImage(String path) {
    int l = 1024;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (FileInputStream fis = new FileInputStream(path)) {
      byte[] buffer = new byte[l];
      int readBytes;
      while ((readBytes = fis.read(buffer)) != -1) {
        baos.write(buffer, 0, readBytes);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return baos.toByteArray();
  }

}
