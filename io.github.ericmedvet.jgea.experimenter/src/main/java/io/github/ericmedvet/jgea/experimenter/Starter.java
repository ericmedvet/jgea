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

package io.github.ericmedvet.jgea.experimenter;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import io.github.ericmedvet.jnb.core.BuilderException;
import io.github.ericmedvet.jnb.core.NamedBuilder;
import io.github.ericmedvet.jnb.core.NamedParamMap;
import io.github.ericmedvet.jnb.core.ParamMap;
import io.github.ericmedvet.jnb.core.parsing.StringParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Starter {

  private static final Logger L = Logger.getLogger(Starter.class.getName());

  static {
    Locale.setDefault(Locale.ROOT);
    try {
      LogManager.getLogManager()
          .readConfiguration(Starter.class.getClassLoader().getResourceAsStream("logging.properties"));
    } catch (IOException ex) {
      // ignore
    }
  }

  public static class Configuration {
    @Parameter(
        names = {"--expFile", "-f"},
        description = "Path of the file with the experiment description.")
    public String experimentDescriptionFilePath = "";

    @Parameter(
        names = {"--exampleExp", "-e"},
        description = "Name of the example experiment description.")
    public String exampleExperimentDescriptionResourceName = "";

    @Parameter(
        names = {"--nOfThreads", "-nt"},
        description = "Number of threads to be used for fitness computation.")
    public int nOfThreads = 2;

    @Parameter(
        names = {"--nOfRuns", "-nr"},
        description = "Number of concurrent runs.")
    public int nOfConcurrentRuns = 1;

    @Parameter(
        names = {"--showExpFileHelp", "-d"},
        description = "Show a description of available constructs for the experiment file.")
    public boolean showExpFileHelp = false;

    @Parameter(
        names = {"--checkExpFile", "-c"},
        description = "Just check the correctness of the experiment description.")
    public boolean check = false;

    @Parameter(
        names = {"--verbose", "-v"},
        description = "Be verbose on errors (i.e., print stack traces)")
    public boolean verbose = false;

    @Parameter(
        names = {"--help", "-h"},
        description = "Show this help.",
        help = true)
    public boolean help;
  }

  public static void main(String[] args) {
    // read configuration
    Configuration configuration = new Configuration();
    JCommander jc = JCommander.newBuilder().addObject(configuration).build();
    jc.setProgramName(Starter.class.getName());
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
    if (configuration.help) {
      jc.usage();
      return;
    }
    // prepare local named builder
    NamedBuilder<Object> nb = NamedBuilder.fromDiscovery();
    // check if it's just a help invocation
    if (configuration.showExpFileHelp) {
      System.out.println(NamedBuilder.prettyToString(nb, true));
      return;
    }
    // read experiment description
    String expDescription = null;
    if (configuration.experimentDescriptionFilePath.isEmpty()
        && !configuration.exampleExperimentDescriptionResourceName.isEmpty()) {
      L.config("Using example experiment description: %s"
          .formatted(configuration.exampleExperimentDescriptionResourceName));
      InputStream inputStream = Starter.class.getResourceAsStream(
          "/exp-examples/%s.txt".formatted(configuration.exampleExperimentDescriptionResourceName));
      if (inputStream == null) {
        L.severe("Cannot find default experiment description: %s"
            .formatted(configuration.exampleExperimentDescriptionResourceName));
      } else {
        try {
          expDescription = new String(inputStream.readAllBytes());
        } catch (IOException e) {
          L.severe("Cannot read default experiment description: %s".formatted(e));
        }
      }
    } else if (!configuration.experimentDescriptionFilePath.isEmpty()) {
      L.config(String.format(
          "Using provided experiment description: %s", configuration.experimentDescriptionFilePath));
      try {
        expDescription = Files.readString(Path.of(configuration.experimentDescriptionFilePath));
      } catch (IOException e) {
        L.severe("Cannot read provided experiment description at %s: %s"
            .formatted(configuration.experimentDescriptionFilePath, e));
      }
    }
    if (expDescription == null) {
      L.info("No experiment provided");
      System.exit(-1);
    }
    // parse and add name
    Experiment experiment = (Experiment) nb.build(expDescription);
    if (experiment.name().isEmpty()) {
      Path path = Path.of(
          configuration.experimentDescriptionFilePath.isEmpty()
              ? configuration.exampleExperimentDescriptionResourceName
              : configuration.experimentDescriptionFilePath);
      NamedParamMap expNPM = StringParser.parse(expDescription)
          .with("name", ParamMap.Type.STRING, path.getFileName().toString())
          .with(
              "startTime",
              ParamMap.Type.STRING,
              "%1$tY-%1$tm-%1$td--%1$tH-%1$tM-%1$tS"
                  .formatted(Instant.now().toEpochMilli()));
      experiment = (Experiment) nb.build(expNPM);
    }
    // check if just check
    if (configuration.check) {
      try {
        System.out.println("Experiment description is valid");
        System.out.printf("\t%d runs%n", experiment.runs().size());
        System.out.printf("\t%d listeners%n", experiment.listeners().size());
        return;
      } catch (BuilderException e) {
        L.severe("Cannot build experiment: %s%n".formatted(e));
        if (configuration.verbose) {
          //noinspection CallToPrintStackTrace
          e.printStackTrace();
        }
        System.exit(-1);
      }
    }
    // prepare and run experimenter
    try {
      L.info("Running experiment '%s' with %d runs and %d listeners"
          .formatted(
              experiment.name(),
              experiment.runs().size(),
              experiment.listeners().size()));
      Experimenter experimenter = new Experimenter(configuration.nOfConcurrentRuns, configuration.nOfThreads);
      experimenter.run(experiment, configuration.verbose);
    } catch (BuilderException e) {
      L.severe("Cannot run experiment: %s%n".formatted(e));
      if (configuration.verbose) {
        //noinspection CallToPrintStackTrace
        e.printStackTrace();
      }
      System.exit(-1);
    }
  }
}
