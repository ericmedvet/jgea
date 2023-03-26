package io.github.ericmedvet.jgea.experimenter.net;

import io.github.ericmedvet.jgea.core.listener.Listener;
import io.github.ericmedvet.jgea.core.listener.ListenerFactory;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jgea.experimenter.Run;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author "Eric Medvet" on 2023/03/25 for jgea
 */
public class NetListenerClient<G, S, Q> implements ListenerFactory<POSetPopulationState<G, S, Q>, Run<?, G, S, Q>> {

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

  public record Item(String name, String format, Object value) {}

  public record MachineInfo(String machineName, int numberOfProcessors, double cpuLoad) {}

  public record Message(
      MachineInfo machineInfo,
      ProcessInfo processInfo,
      double pollInterval,
      List<Update> updates
  ) {}

  public record ProcessInfo(String processName, String username, long usedMemory, long maxMemory) {}

  public record Update(Run<?, ?, ?, ?> run, List<Item> items) {}

  @Override
  public Listener<POSetPopulationState<G, S, Q>> build(Run<?, G, S, Q> run) {
    return state -> {
      Update update = new Update(
          run,
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
    Message message = new Message(
        NetUtils.getMachineInfo(),
        NetUtils.getProcessInfo(),
        pollInterval,
        new ArrayList<>(updates)
    );
    // TODO
  }
}
