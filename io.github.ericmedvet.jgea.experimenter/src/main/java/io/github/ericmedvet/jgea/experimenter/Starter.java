/*
 * Copyright 2022 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jgea.experimenter;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import io.github.ericmedvet.jnb.core.BuilderException;
import io.github.ericmedvet.jnb.core.NamedBuilder;

import java.io.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2022/08/11 for 2d-robot-evolution
 */
public class Starter {

  private final static Logger L = Logger.getLogger(Starter.class.getName());

  static {
    try {
      LogManager.getLogManager().readConfiguration(Starter.class.getClassLoader()
          .getResourceAsStream("logging.properties"));
    } catch (IOException ex) {
      //ignore
    }
  }

  public static class Configuration {
    @Parameter(
        names = {"--expFile", "-f"},
        description = "Path of the file with the experiment description."
    )
    public String experimentDescriptionFilePath = "";
    @Parameter(
        names = {"--nOfThreads", "-nt"},
        description = "Number of threads to be used for fitness computation."
    )
    public int nOfThreads = 2;

    @Parameter(
        names = {"--nOfRuns", "-nr"},
        description = "Number of concurrent runs."
    )
    public int nOfConcurrentRuns = 1;

    @Parameter(
        names = {"--showExpFileHelp", "-d"},
        description = "Show a description of available constructs for the experiment file."
    )
    public boolean showExpFileHelp = false;

    @Parameter(
        names = {"--checkExpFile", "-c"},
        description = "Just check the correctness of the experiment description."
    )
    public boolean check = false;

    @Parameter(
        names = {"--help", "-h"},
        description = "Show this help.",
        help = true
    )
    public boolean help;

  }

  public static void main(String[] args) {
    //read configuration
    Configuration configuration = new Configuration();
    JCommander jc = JCommander.newBuilder()
        .addObject(configuration)
        .build();
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
    //check help
    if (configuration.help) {
      jc.usage();
      System.exit(0);
    }
    //prepare local named builder
    NamedBuilder<Object> nb = PreparedNamedBuilder.get();
    //check if it's just a help invocation
    if (configuration.showExpFileHelp) {
      System.out.println(NamedBuilder.prettyToString(nb, true));
      System.exit(0);
    }
    //read experiment description
    String expDescription = null;
    if (configuration.experimentDescriptionFilePath.isEmpty()) {
      L.config("Using default experiment description");
      InputStream inputStream = Starter.class.getResourceAsStream("/exp-examples/sr-comparison.txt");
      if (inputStream == null) {
        L.severe("Cannot find default experiment description");
      } else {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
          expDescription = br.lines().collect(Collectors.joining());
        } catch (IOException e) {
          L.severe("Cannot read default experiment description: %s%n".formatted(e));
        }
      }
    } else {
      L.config(String.format("Using provided experiment description: %s", configuration.experimentDescriptionFilePath));
      try (BufferedReader br = new BufferedReader(new FileReader(configuration.experimentDescriptionFilePath))) {
        expDescription = br.lines().collect(Collectors.joining());
      } catch (IOException e) {
        L.severe("Cannot read provided experiment description at %s: %s%n".formatted(
            configuration.experimentDescriptionFilePath,
            e
        ));
      }
    }
    if (expDescription == null) {
      System.exit(-1);
    }
    //check if just check
    if (configuration.check) {
      try {
        Experiment experiment = (Experiment) nb.build(expDescription);
        System.out.println("Experiment description is valid");
        System.out.printf("\t%d runs%n", experiment.runs().size());
        System.out.printf("\t%d listeners%n", experiment.listeners().size());
        System.exit(0);
      } catch (BuilderException e) {
        L.severe("Cannot build experiment: %s%n".formatted(e));
        System.exit(-1);
      }
    }
    //prepare and run experimenter
    try {
      Experimenter experimenter = new Experimenter(nb, configuration.nOfConcurrentRuns, configuration.nOfThreads);
      experimenter.run(expDescription);
    } catch (BuilderException e) {
      L.severe("Cannot run experiment: %s%n".formatted(e));
      System.exit(-1);
    }
  }

}
