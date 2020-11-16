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

package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.listener.collector.DataCollector;
import it.units.malelab.jgea.core.listener.collector.Item;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author eric
 */
public class MultiFileListenerFactory<G, S, F> implements ListnerFactory<G, S, F> {

  private final String baseDirName;
  private final String baseFileName;
  private final Map<List<List<PrintStreamListener.Column>>, PrintStream> streams;

  private final static Logger L = Logger.getLogger(MultiFileListenerFactory.class.getName());

  public MultiFileListenerFactory(String baseDirName, String baseFileName) {
    this.baseDirName = baseDirName;
    this.baseFileName = baseFileName;
    streams = new HashMap<>();
  }

  public String getBaseDirName() {
    return baseDirName;
  }

  public String getBaseFileName() {
    return baseFileName;
  }

  @Override
  public Listener<G, S, F> build(DataCollector<? super G, ? super S, ? super F>... collectorArray) {
    List<DataCollector<? super G, ? super S, ? super F>> collectors = Arrays.asList(collectorArray);
    L.info(String.format("Building new listener with %d collectors", collectors.size()));
    return event -> {
      List<List<Item>> itemGroups = PrintStreamListener.collectItems(event, collectors);
      List<List<PrintStreamListener.Column>> columnGroups = PrintStreamListener.buildColumnGroups(itemGroups, false);
      PrintStream ps = streams.get(columnGroups);
      if (ps == null) {
        String fileName = baseDirName + File.separator + String.format(baseFileName, Integer.toHexString(columnGroups.hashCode()));
        try {
          ps = new PrintStream(fileName);
          streams.put(columnGroups, ps);
          L.log(Level.INFO, String.format("New output file %s created", fileName));
        } catch (FileNotFoundException ex) {
          L.log(Level.SEVERE, String.format("Cannot create output file %s", fileName), ex);
          ps = System.out;
        }
        synchronized (streams) {
          ps.println(PrintStreamListener.buildHeaderString(columnGroups, ";", ";", false));
        }

      }
      PrintStreamListener.checkConsistency(itemGroups, columnGroups);
      synchronized (streams) {
        ps.println(PrintStreamListener.buildDataString(itemGroups, columnGroups, ";", ";", false));
      }
    };
  }

}
