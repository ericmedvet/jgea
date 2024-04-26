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
import io.github.ericmedvet.jnb.core.InfoPrinter;
import io.github.ericmedvet.jnb.core.NamedBuilder;
import java.io.*;
import java.util.Locale;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
        names = {"--showExpFileHelpMarkdown", "-m"},
        description =
            "Save to file a description (in Markdown format) of available constructs for the experiment file.")
    public String expFileHelpMarkdownFile = "";

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
      System.exit(0);
    }
    // prepare local named builder
    NamedBuilder<Object> nb = NamedBuilder.fromDiscovery();
    // check if it's just a help invocation
    if (configuration.showExpFileHelp) {
      System.out.println(NamedBuilder.prettyToString(nb, true));
      System.exit(0);
    }
    if (!configuration.expFileHelpMarkdownFile.isEmpty()) {
      try (PrintStream ps = new PrintStream(configuration.expFileHelpMarkdownFile)) {
        new InfoPrinter().print(nb, ps);
        System.exit(0);
      } catch (FileNotFoundException e) {
        L.severe("Cannot save help file description: %s".formatted(e));
        System.exit(-1);
      }
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
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
          expDescription = br.lines().collect(Collectors.joining());
        } catch (IOException e) {
          L.severe("Cannot read default experiment description: %s".formatted(e));
        }
      }
    } else if (!configuration.experimentDescriptionFilePath.isEmpty()) {
      L.config(String.format(
          "Using provided experiment description: %s", configuration.experimentDescriptionFilePath));
      try (BufferedReader br = new BufferedReader(new FileReader(configuration.experimentDescriptionFilePath))) {
        expDescription = br.lines().collect(Collectors.joining());
      } catch (IOException e) {
        L.severe("Cannot read provided experiment description at %s: %s"
            .formatted(configuration.experimentDescriptionFilePath, e));
      }
    }
    if (expDescription == null) {
      L.info("No experiment provided");
      System.exit(-1);
    }
    // check if just check
    if (configuration.check) {
      try {
        Experiment experiment = (Experiment) nb.build(expDescription);
        System.out.println("Experiment description is valid");
        System.out.printf("\t%d runs%n", experiment.runs().size());
        System.out.printf("\t%d listeners%n", experiment.listeners().size());
        System.exit(0);
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
      Experimenter experimenter = new Experimenter(nb, configuration.nOfConcurrentRuns, configuration.nOfThreads);
      experimenter.run(expDescription, configuration.verbose);
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
