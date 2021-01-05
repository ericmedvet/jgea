/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
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

package it.units.malelab.jgea;

import it.units.malelab.jgea.core.util.Args;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static it.units.malelab.jgea.core.util.Args.i;

/**
 * @author eric
 */
public abstract class Worker implements Runnable {

  static {
    try {
      LogManager.getLogManager().readConfiguration(Worker.class.getClassLoader().getResourceAsStream("logging.properties"));
    } catch (IOException ex) {
      //ignore
    }
  }

  protected final ExecutorService executorService;
  protected final String[] args;

  protected final static Logger L = Logger.getLogger(Worker.class.getName());

  public Worker(String[] args) {
    this.args = args;
    executorService = Executors.newFixedThreadPool(i(Args.a(args, "threads", Integer.toString(Runtime.getRuntime().availableProcessors()))));
    run();
    executorService.shutdown();
  }

  protected String a(String name, String defaultValue) {
    return Args.a(args, name, defaultValue);
  }

}
