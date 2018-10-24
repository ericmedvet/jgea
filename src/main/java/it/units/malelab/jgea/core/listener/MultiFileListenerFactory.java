/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.listener.collector.DataCollector;
import it.units.malelab.jgea.core.listener.collector.Item;
import it.units.malelab.jgea.core.listener.event.Event;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
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
 *
 * @author eric
 */
public class MultiFileListenerFactory {

  private final String baseDirName;
  private final String baseFileName;
  private final Map<List<String>, PrintStream> streams;

  private final static Logger L = Logger.getLogger(MultiFileListenerFactory.class.getName());

  public MultiFileListenerFactory(String baseDirName, String baseFileName) {
    this.baseDirName = baseDirName;
    this.baseFileName = baseFileName;
    streams = new HashMap<>();
  }

  public Listener build(DataCollector... collectors) {
    return new PrintStreamListener(null, false, 0, "; ", "; ", collectors) {
      @Override
      public void listen(Event event) {
        final EvolutionEvent evolutionEvent;
        if (event instanceof EvolutionEvent) {
          evolutionEvent = ((EvolutionEvent) event);
        } else {
          return;
        }
        //collect items
        List<List<Item>> items = collectItems(evolutionEvent);
        //retrieve printstream
        List<String> names = items.stream()
                .map(is -> is.stream().map(Item::getName))
                .reduce((l1s, l2s) -> Stream.concat(l1s, l2s))
                .get()
                .collect(Collectors.toList());
        PrintStream ps = streams.get(names);
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
        //print values: collectors
        String data = buildDataString(items);
        synchronized (streams) {
          ps.println(data);
        }
      }
    };
  }

}
