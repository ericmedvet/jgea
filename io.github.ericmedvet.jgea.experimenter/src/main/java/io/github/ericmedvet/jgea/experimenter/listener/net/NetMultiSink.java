/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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

import io.github.ericmedvet.jgea.experimenter.Utils;
import io.github.ericmedvet.jgea.experimenter.listener.decoupled.*;
import java.io.*;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class NetMultiSink {

  private static final Logger L = Logger.getLogger(NetMultiSink.class.getName());
  private final Sink<MachineKey, MachineInfo> machineSink;
  private final Sink<ProcessKey, ProcessInfo> processSink;
  private final Sink<ProcessKey, LogInfo> logSink;
  private final Sink<ExperimentKey, ExperimentInfo> experimentSink;
  private final Sink<RunKey, RunInfo> runSink;
  private final Sink<DataItemKey, DataItemInfo> datItemSink;
  private final String serverAddress;
  private final int serverPort;
  private final String serverKey;
  private final ScheduledExecutorService executorService;
  private final List<Item<?, ?>> toSendItems;
  private ObjectOutputStream oos = null;

  public NetMultiSink(double pollInterval, String serverAddress, int serverPort, File serverKeyFile) {
    this(pollInterval, serverAddress, serverPort, Utils.getCredentialFromFile(serverKeyFile));
  }

  public NetMultiSink(double pollInterval, String serverAddress, int serverPort, String serverKey) {
    this.serverAddress = serverAddress;
    this.serverPort = serverPort;
    this.serverKey = serverKey;
    toSendItems = new LinkedList<>();
    executorService = Executors.newSingleThreadScheduledExecutor();
    NetMultiSink thisNetMultiSink = this;
    machineSink = new Sink<>() {
      @Override
      public void push(LocalDateTime t, MachineKey k, MachineInfo v) {
        add(t, k, v);
      }

      @Override
      public void close() {
        thisNetMultiSink.close();
      }
    };
    processSink = this::add;
    logSink = this::add;
    experimentSink = this::add;
    runSink = this::add;
    datItemSink = this::add;
    executorService.scheduleAtFixedRate(this::sendItems, 0, (int) (pollInterval * 1000d), TimeUnit.MILLISECONDS);
  }

  public record Item<K, V>(LocalDateTime t, K k, V v) implements Serializable {}

  private <K, V> void add(LocalDateTime t, K k, V v) {
    synchronized (toSendItems) {
      toSendItems.add(new Item<>(t, k, v));
    }
  }

  private void close() {
    executorService.shutdownNow();
  }

  private void doHandshake(ObjectInputStream ois, ObjectOutputStream oos) throws IOException {
    String challenge = null;
    try {
      challenge = (String) ois.readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException("Cannot perform handshake due to %s".formatted(e.toString()));
    }
    String response;
    try {
      int n = Integer.parseInt(NetUtils.decrypt(challenge, serverKey));
      response = NetUtils.encrypt(Integer.toString(n + 1), serverKey);
    } catch (NoSuchAlgorithmException
        | InvalidKeyException
        | IllegalBlockSizeException
        | NoSuchPaddingException
        | InvalidAlgorithmParameterException
        | InvalidKeySpecException e) {
      throw new IOException("Cannot perform handshake due to %s".formatted(e.toString()));
    } catch (BadPaddingException e) {
      throw new IOException("Handshake failed, likely due to wrong key");
    }
    oos.writeObject(response);
  }

  public Sink<DataItemKey, DataItemInfo> getDatItemSink() {
    return datItemSink;
  }

  public Sink<ExperimentKey, ExperimentInfo> getExperimentSink() {
    return experimentSink;
  }

  public Sink<ProcessKey, LogInfo> getLogSink() {
    return logSink;
  }

  public Sink<MachineKey, MachineInfo> getMachineSink() {
    return machineSink;
  }

  public Sink<ProcessKey, ProcessInfo> getProcessSink() {
    return processSink;
  }

  public Sink<RunKey, RunInfo> getRunSink() {
    return runSink;
  }

  private void openConnection() {
    if (oos != null) {
      return;
    }
    Socket socket = null;
    try {
      socket = new Socket(serverAddress, serverPort);
      oos = new ObjectOutputStream(socket.getOutputStream());
      doHandshake(new ObjectInputStream(socket.getInputStream()), oos);
    } catch (IOException e) {
      L.warning("Cannot open connection due to: %s".formatted(e));
      if (socket != null) {
        try {
          socket.close();
        } catch (IOException ex) {
          // ignore
        }
      }
    }
  }

  private void sendItems() {
    openConnection();
    if (oos != null) {
      List<Item<?, ?>> items;
      synchronized (toSendItems) {
        items = toSendItems.stream().toList();
        toSendItems.clear();
      }
      try {
        oos.writeObject(items);
        L.fine("Message sent with %d updates".formatted(items.size()));
      } catch (IOException e) {
        L.warning("Cannot send message with %d updates due to: %s".formatted(items.size(), e));
        try {
          oos.close();
        } catch (IOException e2) {
          L.warning("Cannot close connection due to: %s".formatted(e2));
        }
        oos = null;
      }
    }
  }
}
