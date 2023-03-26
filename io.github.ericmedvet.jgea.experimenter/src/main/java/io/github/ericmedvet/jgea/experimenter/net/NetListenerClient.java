package io.github.ericmedvet.jgea.experimenter.net;

import io.github.ericmedvet.jgea.core.listener.Listener;
import io.github.ericmedvet.jgea.core.listener.ListenerFactory;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jgea.experimenter.Run;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author "Eric Medvet" on 2023/03/25 for jgea
 */
public class NetListenerClient<G, S, Q> implements ListenerFactory<POSetPopulationState<G, S, Q>, Run<?, G, S, Q>> {

  private final static Logger L = Logger.getLogger(NetListenerClient.class.getName());
  private final String serverAddress;
  private final int serverPort;
  private final double pollInterval;
  private final List<NamedFunction<? super POSetPopulationState<G, S, Q>, ?>> stateFunctions;
  private final List<Update> updates;
  private final ScheduledExecutorService service;

  public NetListenerClient(
      String serverAddress,
      int serverPort,
      double pollInterval,
      List<NamedFunction<? super POSetPopulationState<G, S, Q>, ?>> stateFunctions
  ) {
    this.serverAddress = serverAddress;
    this.serverPort = serverPort;
    this.pollInterval = pollInterval;
    this.stateFunctions = stateFunctions;
    updates = new ArrayList<>();
    service = Executors.newSingleThreadScheduledExecutor();
    service.scheduleAtFixedRate(this::sendUpdates, 0, (int) (1000 * pollInterval), TimeUnit.MILLISECONDS);
  }

  public record Item(String name, String format, Object value) implements Serializable {}

  public record MachineInfo(String machineName, int numberOfProcessors, double cpuLoad) implements Serializable {}

  public record Message(
      MachineInfo machineInfo,
      ProcessInfo processInfo,
      double pollInterval,
      List<Update> updates
  ) implements Serializable {}

  public record ProcessInfo(
      String processName, String username, long usedMemory, long maxMemory
  ) implements Serializable {}

  public record Update(long localTime, String runMap, List<Item> items) implements Serializable {}

  @Override
  public Listener<POSetPopulationState<G, S, Q>> build(Run<?, G, S, Q> run) {
    return state -> {
      Update update = new Update(
          System.currentTimeMillis(),
          run.map().toString(),
          stateFunctions.stream()
              .map(f -> new Item(f.getName(), f.getFormat(), f.apply(state)))
              .toList()
      );
      synchronized (updates) {
        updates.add(update);
      }
    };
  }

  @Override
  public void shutdown() {
    sendUpdates();
    service.shutdownNow();
  }

  private void sendUpdates() {
    List<Update> toSendUpdates;
    synchronized (updates) {
      toSendUpdates = new ArrayList<>(updates);
      updates.clear();
    }
    //prepare message
    Message message = new Message(
        NetUtils.getMachineInfo(),
        NetUtils.getProcessInfo(),
        pollInterval,
        toSendUpdates
    );
    //attempt send
    try (
        Socket socket = new Socket(serverAddress, serverPort);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())
    ) {
      oos.writeObject(message);
      L.fine("Message sent with %d updates".formatted(message.updates().size()));
    } catch (IOException e) {
      L.warning("Cannot send message with %d updates due to: %s".formatted(message.updates().size(), e));
      synchronized (updates) {
        updates.addAll(message.updates());
      }
    }
  }
}
