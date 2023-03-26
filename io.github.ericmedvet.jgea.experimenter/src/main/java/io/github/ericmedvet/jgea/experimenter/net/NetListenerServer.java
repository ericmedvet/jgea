package io.github.ericmedvet.jgea.experimenter.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * @author "Eric Medvet" on 2023/03/26 for jgea
 */
public class NetListenerServer implements Runnable {

  private final static Logger L = Logger.getLogger(NetListenerServer.class.getName());

  private final int port = 10979;
  private final int nOfClients = 100;
  private final double lazyThreshold = 2;
  private final double missingThreshold = 5;
  private final double purgeThreshold = 10;


  public static void main(String[] args) {
    NetListenerServer server = new NetListenerServer();
    server.run();
  }

  @Override
  public void run() {
    ExecutorService executorService = Executors.newFixedThreadPool(nOfClients);
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      L.info("Server started on port %d".formatted(port));
      while (true) {
        try {
          Socket socket = serverSocket.accept();
          executorService.submit(() -> {
            try (socket; ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
              NetListenerClient.Message message = (NetListenerClient.Message) ois.readObject();
              System.out.printf("Msg received with %d updates%n", message.updates().size());
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
    executorService.shutdown();
  }
}
