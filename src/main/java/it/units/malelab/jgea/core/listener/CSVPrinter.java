package it.units.malelab.jgea.core.listener;

import org.apache.commons.csv.CSVFormat;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author eric on 2021/01/03 for jgea
 */
public class CSVPrinter<E> implements Listener.Factory<E> {

  private static final Logger L = Logger.getLogger(CSVPrinter.class.getName());
  private static final int FLUSH_N = 10;

  private final List<NamedFunction<? super E, ?>> functions;
  private final File file;

  private org.apache.commons.csv.CSVPrinter printer;
  private int lineCounter;

  public CSVPrinter(List<NamedFunction<? super E, ?>> functions, File file) {
    this.functions = functions;
    this.file = file;
    lineCounter = 0;
  }

  @Override
  public Listener<E> build() {
    return e -> {
      List<Object> values = functions.stream().map(f -> f.apply(e)).collect(Collectors.toList());
      synchronized (file) {
        if (printer == null) {
          File actualFile = check(file);
          try {
            printer = new org.apache.commons.csv.CSVPrinter(new PrintStream(actualFile), CSVFormat.DEFAULT.withDelimiter(';'));
          } catch (IOException ex) {
            L.severe(String.format("Cannot create CSVPrinter: %s", ex));
            return;
          }
          try {
            printer.printRecord(functions.stream().map(NamedFunction::getName).collect(Collectors.toList()));
          } catch (IOException ex) {
            L.warning(String.format("Cannot print header: %s", ex));
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

  private static File check(File file) {
    String originalFileName = file.getPath();
    while (file.exists()) {
      String newName = null;
      Matcher mNum = Pattern.compile("\\((?<n>[0-9]+)\\)\\.\\w+$").matcher(file.getPath());
      if (newName == null && mNum.find()) {
        int n = Integer.parseInt(mNum.group("n"));
        newName = newName = new StringBuilder(file.getPath()).replace(mNum.start("n"), mNum.end("n"), Integer.toString(n + 1)).toString();
      }
      Matcher mExtension = Pattern.compile("\\.\\w+$").matcher(file.getPath());
      if (newName == null && mExtension.find()) {
        newName = new StringBuilder(file.getPath()).replace(mExtension.start(), mExtension.end(), ".(1)" + mExtension.group()).toString();
      }
      if (newName == null) {
        newName = file.getPath() + ".newer";
      }
      file = new File(newName);
    }
    if (!file.getPath().equals(originalFileName)) {
      L.log(Level.WARNING, String.format("Given file name (%s) exists; will write on %s", originalFileName, file.getPath()));
    }
    return file;
  }
}
