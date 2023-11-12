/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.jgea.experimenter.listener.net;

import io.github.ericmedvet.jgea.experimenter.listener.decoupled.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;

/**
 * @author "Eric Medvet" on 2023/11/12 for jgea
 */
public class NetMultiSource {

  private static final Logger L = Logger.getLogger(NetMultiSource.class.getName());

  private static final int SERVER_SOCKET_TIMEOUT_MILLIS = 1000;
  private static final int N_OF_CLIENTS = 100;
  private final int serverPort;
  private final String serverKey;

  private final DirectSinkSource<MachineKey, MachineInfo> machineSource;
  private final DirectSinkSource<ProcessKey, ProcessInfo> processSource;
  private final DirectSinkSource<ProcessKey, LogInfo> logSource;
  private final DirectSinkSource<ExperimentKey, ExperimentInfo> experimentSource;
  private final DirectSinkSource<RunKey, RunInfo> runSource;
  private final DirectSinkSource<DataItemKey, DataItemInfo> dataItemSource;
  private final ExecutorService executorService;

  private boolean isRunning;

  public NetMultiSource(int serverPort, String serverKey) {
    this.serverPort = serverPort;
    this.serverKey = serverKey;
    machineSource = new DirectSinkSource<>() {
      @Override
      public void close() {
        L.info("Closing");
        isRunning = false;
        executorService.shutdownNow();
      }
    };
    processSource = new DirectSinkSource<>();
    logSource = new DirectSinkSource<>();
    experimentSource = new DirectSinkSource<>();
    runSource = new DirectSinkSource<>();
    dataItemSource = new DirectSinkSource<>();
    executorService = Executors.newFixedThreadPool(N_OF_CLIENTS + 1);
    // start server
    isRunning = true;
    executorService.submit(this::listen);
  }

  private static <K, V> void push(Sink<K, V> sink, NetMultiSink.Item<?, ?> item) {
    //noinspection unchecked
    sink.push(item.t(), (K) item.k(), (V) item.v());
  }

  private boolean doHandshake(ObjectInputStream ois, ObjectOutputStream oos) throws IOException {
    RandomGenerator rg = new Random();
    int n = rg.nextInt();
    try {
      oos.writeObject(NetUtils.encrypt(Integer.toString(n), serverKey));
      int m = Integer.parseInt(NetUtils.decrypt((String) ois.readObject(), serverKey));
      return (m == n + 1);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  public Source<DataItemKey, DataItemInfo> getDataItemSource() {
    return dataItemSource;
  }

  public Source<ExperimentKey, ExperimentInfo> getExperimentSource() {
    return experimentSource;
  }

  public Source<ProcessKey, LogInfo> getLogSource() {
    return logSource;
  }

  public Source<MachineKey, MachineInfo> getMachineSource() {
    return machineSource;
  }

  public Source<ProcessKey, ProcessInfo> getProcessSource() {
    return processSource;
  }

  public Source<RunKey, RunInfo> getRunSource() {
    return runSource;
  }

  private void handleClient(Socket socket) {
    try (socket;
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
      try {
        if (!doHandshake(ois, oos)) {
          L.info("Bad attempt from %s: wrong handshake".formatted(socket.getInetAddress()));
          return;
        }
      } catch (IOException e) {
        L.info("Handshake from %s abruptly terminated due to %s".formatted(socket.getInetAddress(), e));
        return;
      }
      while (isRunning) {
        List<NetMultiSink.Item<?, ?>> items;
        try {
          //noinspection unchecked
          items = (List<NetMultiSink.Item<?, ?>>) ois.readObject();
        } catch (ClassNotFoundException e) {
          L.warning("Cannot read update from %s due to: %s".formatted(socket.getInetAddress(), e));
          return;
        }
        L.fine("Msg received with %d updates".formatted(items.size()));
        for (NetMultiSink.Item<?, ?> item : items) {
          if (item.v() instanceof MachineInfo) {
            push(machineSource, item);
          } else if (item.v() instanceof ProcessInfo) {
            push(processSource, item);
          } else if (item.v() instanceof LogInfo) {
            push(logSource, item);
          } else if (item.v() instanceof ExperimentInfo) {
            push(experimentSource, item);
          } else if (item.v() instanceof RunInfo) {
            push(runSource, item);
          } else if (item.v() instanceof DataItemInfo) {
            push(dataItemSource, item);
          }
        }
      }
    } catch (IOException e) {
      L.warning("Cannot handle client at %s due to: %s".formatted(socket.getInetAddress(), e));
    }
  }

  private void listen() {
    try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
      serverSocket.setSoTimeout(SERVER_SOCKET_TIMEOUT_MILLIS);
      L.info("Server started on port %d".formatted(serverPort));
      while (isRunning) {
        try {
          Socket socket = serverSocket.accept();
          executorService.submit(() -> handleClient(socket));
        } catch (SocketTimeoutException e) {
          // ignore
        } catch (IOException e) {
          L.warning("Cannot accept connection due to; %s".formatted(e));
        }
      }
    } catch (IOException e) {
      L.severe("Cannot start server due to: %s".formatted(e));
    }
  }
}
