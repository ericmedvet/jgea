package io.github.ericmedvet.jgea.experimenter.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author "Eric Medvet" on 2023/03/26 for jgea
 */
public class NetListenerServer implements Runnable {

  private final static Logger L = Logger.getLogger(NetListenerServer.class.getName());
  private final static Configuration DEFAULT_CONFIGURATION = new Configuration(
      0f,
      0f,
      0f,
      20,
      250,
      10979,
      100,
      2,
      5,
      20
  );
  private final Configuration configuration;
  private final Map<MachineKey, SortedMap<Long, MachineInfo>> machinesData;
  private final Map<ProcessKey, SortedMap<Long, ProcessInfo>> processesData;
  private final Map<RunKey, Map<ItemKey, SortedMap<Long, Object>>> runsData;
  private final Map<MachineKey, Status> machinesStatus;
  private final Map<ProcessKey, Status> processesStatus;
  private final ExecutorService clientsExecutorService;
  private final ScheduledExecutorService uiExecutorService;

  public NetListenerServer(
      Configuration configuration
  ) {
    this.configuration = configuration;
    machinesData = new HashMap<>();
    processesData = new HashMap<>();
    runsData = new HashMap<>();
    machinesStatus = new HashMap<>();
    processesStatus = new HashMap<>();
    clientsExecutorService = Executors.newFixedThreadPool(configuration.nOfClients());
    uiExecutorService = Executors.newSingleThreadScheduledExecutor();
  }

  private enum TimeStatus {OK, LATE, LATER, MISSING, PURGE}

  public record Configuration(
      float runsSplit, float verticalSplit, float machinesProcessesSplit,
      int barLength, int refreshIntervalMillis,
      int port, int nOfClients, double laterThreshold, double missingThreshold, double purgeThreshold
  ) {}

  private record ItemKey(String name, String format) {}

  private record MachineKey(String machineName) {}

  private record ProcessKey(String machineName, String processName) {}

  private record RunKey(String machineName, String processName, int runIndex) {}

  private record Status(Instant lastContact, double pollInterval, TimeStatus status) {}

  public static void main(String[] args) {
    NetListenerServer server = new NetListenerServer(DEFAULT_CONFIGURATION);
    server.run();
  }

  private void refreshData() {
    //update statuses
    Instant now = Instant.now();
    machinesStatus.replaceAll((k, s) -> update(s, now));
    processesStatus.replaceAll((k, s) -> update(s, now));
    // TODO: purge old from all tables
  }

  @Override
  public void run() {
    //start painter task
    uiExecutorService.scheduleAtFixedRate(
        () -> {
          refreshData();
          updateUI();
        },
        0,
        configuration.refreshIntervalMillis,
        TimeUnit.MILLISECONDS
    );
    //start server
    try (ServerSocket serverSocket = new ServerSocket(configuration.port())) {
      L.info("Server started on port %d".formatted(configuration.port()));
      while (true) {
        try {
          Socket socket = serverSocket.accept();
          clientsExecutorService.submit(() -> {
            try (socket; ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
              Message message = (Message) ois.readObject();
              L.fine("Msg received with %d updates".formatted(message.updates().size()));
              storeMessage(message);
            } catch (IOException e) {
              L.warning("Cannot open input stream due to: %s".formatted(e));
            } catch (ClassNotFoundException e) {
              L.warning("Cannot read message due to: %s".formatted(e));
            }
          });
        } catch (IOException e) {
          L.warning("Cannot accept connection due to; %s".formatted(e));
        }
      }
    } catch (IOException e) {
      L.severe("Cannot start server due to: %s".formatted(e));
    }
  }

  private synchronized void storeMessage(Message message) {
    MachineKey machineKey = new MachineKey(message.machineInfo().machineName());
    machinesData.putIfAbsent(machineKey, new TreeMap<>());
    machinesData.get(machineKey).put(message.localTime(), message.machineInfo());
    machinesStatus.put(machineKey, new Status(Instant.now(), message.pollInterval(), TimeStatus.OK));
    ProcessKey processKey = new ProcessKey(message.machineInfo().machineName(), message.processInfo().processName());
    processesData.putIfAbsent(processKey, new TreeMap<>());
    processesData.get(processKey).put(message.localTime(), message.processInfo());
    processesStatus.put(processKey, new Status(Instant.now(), message.pollInterval(), TimeStatus.OK));
    for (Update update : message.updates()) {
      RunKey runKey = new RunKey(
          message.machineInfo().machineName(),
          message.processInfo().processName(),
          update.runIndex()
      );
      runsData.putIfAbsent(runKey, new HashMap<>());
      Map<ItemKey, SortedMap<Long, Object>> runData = runsData.get(runKey);
      for (Item item : update.items()) {
        ItemKey itemKey = new ItemKey(item.name(), item.format());
        runData.putIfAbsent(itemKey, new TreeMap<>());
        runData.get(itemKey).put(update.localTime(), item.value());
      }
    }
  }

  private Status update(Status status, Instant now) {
    long elapsed = Duration.between(status.lastContact(), now).toMillis();
    long expected = Math.round(status.pollInterval() * 1000);
    TimeStatus timeStatus = TimeStatus.OK;
    if (elapsed > configuration.purgeThreshold() * expected) {
      timeStatus = TimeStatus.PURGE;
    } else if (elapsed > configuration.missingThreshold() * expected) {
      timeStatus = TimeStatus.MISSING;
    } else if (elapsed > configuration.laterThreshold() * expected) {
      timeStatus = TimeStatus.LATER;
    } else if (elapsed > expected) {
      timeStatus = TimeStatus.LATE;
    }
    return new Status(status.lastContact(), status.pollInterval(), timeStatus);
  }

  private void updateUI() {

  }
}
