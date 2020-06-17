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

package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.listener.collector.DataCollector;
import it.units.malelab.jgea.core.listener.collector.Item;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author eric
 */
public class MultiFileListenerFactory<G, S, F> {

  private final String baseDirName;
  private final String baseFileName;
  private final Map<List<String>, PrintStream> streams;

  private final static Logger L = Logger.getLogger(MultiFileListenerFactory.class.getName());

  public MultiFileListenerFactory(String baseDirName, String baseFileName) {
    this.baseDirName = baseDirName;
    this.baseFileName = baseFileName;
    streams = new HashMap<>();
  }

  public Listener build(DataCollector<? super G, ? super S, ? super F>... collectors) {
    return new PrintStreamListener<G, S, F>(null, false, 0, ";", ";", collectors) {
      @Override
      public void listen(Event<? extends G, ? extends S, ? extends F> event) {
        //collect items
        List<List<Item>> items = collectItems(event);
        //retrieve printstream
        List<String> names = items.stream()
            .map(is -> is.stream().map(Item::getName))
            .reduce(Stream::concat)
            .get()
            .collect(Collectors.toList());
        PrintStream ps = null;
        synchronized (streams) {
          ps = streams.get(names);
          if (ps == null) {
            String fileName = baseDirName + File.separator + String.format(baseFileName, Integer.toHexString(names.hashCode()));
            try {
              ps = new PrintStream(fileName);
              L.log(Level.INFO, String.format("New output file %s created", fileName));
            } catch (FileNotFoundException ex) {
              L.log(Level.SEVERE, String.format("Cannot create output file %s", fileName), ex);
              ps = System.out;
            }
            String headersString = buildHeadersString();
            synchronized (streams) {
              streams.put(names, ps);
              ps.println(headersString);
            }
          }
        }
        //print values: collectors
        String data = buildDataString(items);
        synchronized (streams) {
          ps.println(data);
        }
      }
    };
  }

}
