package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.util.Misc;
import org.apache.commons.csv.CSVFormat;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author eric on 2021/01/03 for jgea
 */
public class CSVPrinter<E, K> implements Factory<E, K> {

  private static final Logger L = Logger.getLogger(CSVPrinter.class.getName());
  private static final int FLUSH_N = 10;

  private final List<NamedFunction<? super E, ?>> eFunctions;
  private final List<NamedFunction<? super K, ?>> kFunctions;
  private final File file;

  private org.apache.commons.csv.CSVPrinter printer;
  private int lineCounter;

  public CSVPrinter(
      List<NamedFunction<? super E, ?>> eFunctions, List<NamedFunction<? super K, ?>> kFunctions, File file
  ) {
    this.eFunctions = eFunctions;
    this.kFunctions = kFunctions;
    this.file = file;
    lineCounter = 0;
  }

  private static File check(File file) {
    String originalFileName = file.getPath();
    while (file.exists()) {
      String newName = null;
      Matcher mNum = Pattern.compile("\\((?<n>[0-9]+)\\)\\.\\w+$").matcher(file.getPath());
      if (mNum.find()) {
        int n = Integer.parseInt(mNum.group("n"));
        newName = new StringBuilder(file.getPath()).replace(mNum.start("n"), mNum.end("n"), Integer.toString(n + 1))
            .toString();
      }
      Matcher mExtension = Pattern.compile("\\.\\w+$").matcher(file.getPath());
      if (newName == null && mExtension.find()) {
        newName = new StringBuilder(file.getPath()).replace(mExtension.start(),
            mExtension.end(),
            ".(1)" + mExtension.group()
        ).toString();
      }
      if (newName == null) {
        newName = file.getPath() + ".newer";
      }
      file = new File(newName);
    }
    if (!file.getPath().equals(originalFileName)) {
      L.log(Level.WARNING,
          String.format("Given file name (%s) exists; will write on %s", originalFileName, file.getPath())
      );
    }
    return file;
  }

  @Override
  public Listener<E> build(K k) {
    List<?> kValues = kFunctions.stream().map(f -> f.apply(k)).toList();
    List<String> headers = Misc.concat(List.of(kFunctions, eFunctions)).stream().map(NamedFunction::getName).toList();
    return e -> {
      List<?> eValues = eFunctions.stream().map(f -> f.apply(e)).toList();
      synchronized (file) {
        if (printer == null) {
          File actualFile = check(file);
          try {
            printer = new org.apache.commons.csv.CSVPrinter(new PrintStream(actualFile),
                CSVFormat.DEFAULT.withDelimiter(';')
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
          L.info(String.format("File %s created and header for %d columns written",
              actualFile.getPath(),
              eFunctions.size()
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
