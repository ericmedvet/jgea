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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import io.github.ericmedvet.jgea.experimenter.listener.decoupled.TuiMonitor;
import java.util.logging.Logger;

/**
 * @author "Eric Medvet" on 2023/11/12 for jgea
 */
public class NetTui {

  private static final int DEFAULT_PORT = 10979;
  private static final Logger L = Logger.getLogger(NetTui.class.getName());

  private static class CommandLineConfiguration {
    @Parameter(
        names = {"--port", "-p"},
        description = "Server port.")
    public int port = DEFAULT_PORT;

    @Parameter(
        names = {"--key", "-k"},
        description = "Handshake key.",
        required = true)
    public String key;

    @Parameter(
        names = {"--help", "-h"},
        description = "Show this help.",
        help = true)
    public boolean help;
  }

  public static void main(String[] args) {
    CommandLineConfiguration cmdConfiguration = new CommandLineConfiguration();
    JCommander jc = JCommander.newBuilder().addObject(cmdConfiguration).build();
    jc.setProgramName(NetTui.class.getName());
    try {
      jc.parse(args);
    } catch (ParameterException e) {
      e.usage();
      L.severe(String.format("Cannot read command line options: %s", e));
      System.exit(-1);
    } catch (RuntimeException e) {
      L.severe(e.getClass().getSimpleName() + ": " + e.getMessage());
      System.exit(-1);
    }
    // check help
    if (cmdConfiguration.help) {
      jc.usage();
      System.exit(0);
    }
    // start server
    NetMultiSource netMultiSource = new NetMultiSource(cmdConfiguration.port, cmdConfiguration.key);
    TuiMonitor tuiMonitor = new TuiMonitor(
        () -> "Net: listening on %d".formatted(cmdConfiguration.port),
        netMultiSource.getMachineSource(),
        netMultiSource.getProcessSource(),
        netMultiSource.getLogSource(),
        netMultiSource.getExperimentSource(),
        netMultiSource.getRunSource(),
        netMultiSource.getDataItemSource());
    tuiMonitor.run();
  }
}
