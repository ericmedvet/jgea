package it.units.malelab.jgea.core.consumer;

import org.apache.commons.csv.CSVFormat;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author eric on 2021/01/03 for jgea
 */
public class CSVPrinter<G, S, F> implements Consumer.Factory<G, S, F, Void> {

  private static final Logger L = Logger.getLogger(CSVPrinter.class.getName());
  private static final int FLUSH_N = 10;

  private final List<NamedFunction<Event<? extends G, ? extends S, ? extends F>, ?>> functions;
  private final File file;

  private org.apache.commons.csv.CSVPrinter printer;
  private int lineCounter;

  public CSVPrinter(List<NamedFunction<Event<? extends G, ? extends S, ? extends F>, ?>> functions, File file) {
    this.functions = functions;
    this.file = file;
    lineCounter = 0;
  }

  @Override
  public Consumer<G, S, F, Void> build() {
    return event -> {
      List<Object> values = functions.stream().map(f -> f.apply(event)).collect(Collectors.toList());
      synchronized (file) {
        if (printer == null) {
          File actualFile = check(file);
          try {
            printer = new org.apache.commons.csv.CSVPrinter(new PrintStream(actualFile), CSVFormat.DEFAULT.withDelimiter(';'));
          } catch (IOException e) {
            L.severe(String.format("Cannot create CSVPrinter: %s", e));
            return;
          }
          try {
            printer.printRecord(functions.stream().map(NamedFunction::getName).collect(Collectors.toList()));
          } catch (IOException e) {
            L.warning(String.format("Cannot print header: %s", e));
            return;
          }
          L.info(String.format(
              "File %s created and header for %d columns written",
              actualFile.getPath(),
              functions.size()
          ));
        }
        try {
          printer.printRecord(values);
        } catch (IOException e) {
          L.warning(String.format("Cannot print values: %s", e));
          return;
        }
        if (lineCounter % FLUSH_N == 0) {
          try {
            printer.flush();
          } catch (IOException e) {
            L.warning(String.format("Cannot flush CSVPrinter: %s", e));
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

  private static File check(File file) {
    String originalFileName = file.getPath();
    while (file.exists()) {
      file = new File(file.getPath() + ".newer");
    }
    if (!file.getPath().equals(originalFileName)) {
      L.log(Level.WARNING, String.format("Given file name (%s) exists; will write on %s", originalFileName, file.getPath()));
    }
    return file;
  }
}
