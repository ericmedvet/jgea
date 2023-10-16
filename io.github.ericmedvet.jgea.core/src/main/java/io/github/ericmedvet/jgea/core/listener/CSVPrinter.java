
package io.github.ericmedvet.jgea.core.listener;

import io.github.ericmedvet.jgea.core.util.Misc;
import org.apache.commons.csv.CSVFormat;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Logger;
public class CSVPrinter<E, K> implements ListenerFactory<E, K> {

  private static final Logger L = Logger.getLogger(CSVPrinter.class.getName());
  private static final int FLUSH_N = 10;

  private final List<? extends NamedFunction<? super E, ?>> eFunctions;
  private final List<? extends NamedFunction<? super K, ?>> kFunctions;
  private final File file;

  private org.apache.commons.csv.CSVPrinter printer;
  private int lineCounter;

  public CSVPrinter(
      List<NamedFunction<? super E, ?>> eFunctions,
      List<NamedFunction<? super K, ?>> kFunctions,
      File file,
      boolean robust
  ) {
    this.eFunctions = robust ? eFunctions.stream().map(NamedFunction::robust).toList() : eFunctions;
    this.kFunctions = robust ? kFunctions.stream().map(NamedFunction::robust).toList() : kFunctions;
    this.file = file;
    lineCounter = 0;
  }

  @Override
  public Listener<E> build(K k) {
    List<?> kValues = kFunctions.stream().map(f -> f.apply(k)).toList();
    List<String> headers = Misc.concat(List.of(kFunctions, eFunctions)).stream().map(NamedFunction::getName).toList();
    return e -> {
      List<?> eValues = eFunctions.stream().map(f -> f.apply(e)).toList();
      synchronized (file) {
        if (printer == null) {
          File actualFile = Misc.checkExistenceAndChangeName(file);
          try {
            printer = new org.apache.commons.csv.CSVPrinter(
                new PrintStream(actualFile),
                CSVFormat.Builder.create().setDelimiter(";").build()
            );
          } catch (IOException ex) {
            L.severe(String.format("Cannot create CSVPrinter: %s", ex));
            return;
          }
          try {
            printer.printRecord(headers);
          } catch (IOException ex) {
            L.warning(String.format("Cannot print header: %s", ex));
            return;
          }
          L.info(String.format(
              "File %s created and header for %d columns written",
              actualFile.getPath(),
              eFunctions.size() + kFunctions.size()
          ));
        }
        try {
          printer.printRecord(Misc.concat(List.of(kValues, eValues)));
        } catch (IOException ex) {
          L.warning(String.format("Cannot print values: %s", ex));
          return;
        }
        if (lineCounter % FLUSH_N == 0) {
          try {
            printer.flush();
          } catch (IOException ex) {
            L.warning(String.format("Cannot flush CSVPrinter: %s", ex));
            return;
          }
        }
        lineCounter = lineCounter + 1;
      }
    };
  }

  @Override
  public void shutdown() {
    if (printer != null) {
      try {
        printer.flush();
        printer.close();
      } catch (IOException e) {
        L.warning(String.format("Cannot close CSVPrinter: %s", e));
      }
    }
  }
}
