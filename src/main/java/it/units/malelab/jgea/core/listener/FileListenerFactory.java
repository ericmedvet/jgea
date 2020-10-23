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

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class FileListenerFactory<G, S, F> implements ListnerFactory<G, S, F> {

  private final String filePathName;
  private final List<String> firstNames;
  private PrintStream ps;

  private final static Logger L = Logger.getLogger(FileListenerFactory.class.getName());

  public FileListenerFactory(String filePathName) {
    this.filePathName = filePathName;
    firstNames = new ArrayList<>();
  }

  @Override
  public Listener<G, S, F> build(DataCollector<? super G, ? super S, ? super F>... collectors) {
    return new PrintStreamListener<G, S, F>(null, false, 0, ";", ";", collectors) {
      @Override
      public void listen(Event<? extends G, ? extends S, ? extends F> event) {
        //collect items
        List<List<Item>> items = collectItems(event);
        //check consistency of item names
        if (!firstNames.isEmpty()) {
          List<String> currentNames = items.stream()
              .flatMap(Collection::stream)
              .map(Item::getName)
              .collect(Collectors.toList());
          if (!currentNames.equals(firstNames)) {
            L.warning(String.format("%d items received, %d expected", currentNames.size(), firstNames.size()));
          }
        }
        //retrieve printstream
        synchronized (firstNames) {
          if (firstNames.isEmpty()) {
            firstNames.addAll(items.stream()
                .flatMap(Collection::stream)
                .map(Item::getName)
                .collect(Collectors.toList()));
            try {
              ps = new PrintStream(filePathName);
              L.log(Level.INFO, String.format("New output file %s created", filePathName));
            } catch (FileNotFoundException ex) {
              L.log(Level.SEVERE, String.format("Cannot create output file %s", filePathName), ex);
              ps = System.out;
            }
            String headersString = buildHeadersString();
            ps.println(headersString);
          }
        }
        //print values: collectors
        String data = buildDataString(items);
        synchronized (firstNames) {
          ps.println(data);
        }
      }
    };
  }

}
