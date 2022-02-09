package it.units.malelab.jgea.core.listener.telegram;

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
