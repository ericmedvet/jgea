/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea;

import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.PrintStreamListener;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.units.malelab.jgea.core.listener.collector.DataCollector;
import it.units.malelab.jgea.core.util.Args;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static it.units.malelab.jgea.core.util.Args.*;

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

  public Worker(String[] args) throws FileNotFoundException {
    this.args = args;
    executorService = Executors.newFixedThreadPool(i(Args.a(args, "threads", Integer.toString(Runtime.getRuntime().availableProcessors()))));
    run();
    executorService.shutdown();
  }

  protected String a(String name, String defaultValue) {
    return Args.a(args, name, defaultValue);
  }

  protected <G, S, F> Listener<G, S, F> listener(DataCollector<G, S, F>... collectors) {
    return new PrintStreamListener<>(System.out, true, 10, " ", " | ", collectors);
  }

}
