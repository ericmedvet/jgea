package io.github.ericmedvet.jgea.problem.regression;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2023/04/30 for jgea
 */
public record Dataset(List<Example> examples, List<String> xVarNames, List<String> yVarNames) {
  public Dataset {
    List<Integer> xsSizes = examples.stream().map(e -> e.xs.length).distinct().toList();
    List<Integer> ysSizes = examples.stream().map(e -> e.ys.length).distinct().toList();
    if (xsSizes.size() > 1) {
      throw new IllegalArgumentException("Size of x is not consistent across examples, found sizes %s".formatted(xsSizes));
    }
    if (ysSizes.size() > 1) {
      throw new IllegalArgumentException("Size of y is not consistent across examples, found sizes %s".formatted(ysSizes));
    }
    if (xVarNames.size() != xsSizes.get(0)) {
      throw new IllegalArgumentException(("Number of names of x vars is different form size of x in examples: %d vs " +
          "%d").formatted(
          xVarNames().size(),
          xsSizes.get(0)
      ));
    }
    if (yVarNames.size() != ysSizes.get(0)) {
      throw new IllegalArgumentException(("Number of names of y vars is different form size of y in examples: %d vs " +
          "%d").formatted(
          xVarNames().size(),
          xsSizes.get(0)
      ));
    }
  }

  public Dataset(List<Example> examples) {
    this(
        examples,
        varNames("x", examples.get(0).xs().length),
        varNames("y", examples.get(0).ys().length)
    );
  }

  public record Example(double[] xs, double[] ys) {
    public Example(double[] xs, double y) {
      this(xs, new double[]{y});
    }
  }

  private static Dataset buildDataset(List<Map<String, String>> data, List<String> xVarNames, List<String> yVarNames) {
    return new Dataset(
        data.stream()
            .map(dp -> new Example(
                xVarNames.stream().mapToDouble(n -> Double.parseDouble(dp.get(n))).toArray(),
                yVarNames.stream().mapToDouble(n -> Double.parseDouble(dp.get(n))).toArray()
            ))
            .toList(),
        xVarNames,
        yVarNames
    );
  }

  public static Dataset loadFromCSV(InputStream inputStream, List<String> yVarNames) throws IOException {
    List<Map<String, String>> data = loadFromCSV(inputStream);
    if (!data.get(0).keySet().containsAll(yVarNames)) {
      Set<String> notFoundVars = new LinkedHashSet<>(yVarNames);
      data.get(0).keySet().forEach(notFoundVars::remove);
      throw new IOException("Some yVarNames not found in the file: %s".formatted(notFoundVars));
    }
    Set<String> xVarNamesSet = new LinkedHashSet<>(data.get(0).keySet());
    yVarNames.forEach(xVarNamesSet::remove);
    List<String> xVarNames = xVarNamesSet.stream().toList();
    return buildDataset(data, xVarNames, yVarNames);
  }

  public static Dataset loadFromCSV(
      InputStream inputStream,
      List<String> xVarNames,
      List<String> yVarNames
  ) throws IOException {
    List<Map<String, String>> data = loadFromCSV(inputStream);
    if (!data.get(0).keySet().containsAll(xVarNames)) {
      Set<String> notFoundVars = new LinkedHashSet<>(xVarNames);
      data.get(0).keySet().forEach(notFoundVars::remove);
      throw new IOException("Some xVarNames not found in the file: %s".formatted(notFoundVars));
    }
    if (!data.get(0).keySet().containsAll(yVarNames)) {
      Set<String> notFoundVars = new LinkedHashSet<>(yVarNames);
      data.get(0).keySet().forEach(notFoundVars::remove);
      throw new IOException("Some yVarNames not found in the file: %s".formatted(notFoundVars));
    }
    return buildDataset(data, xVarNames, yVarNames);
  }

  public static Dataset loadFromCSV(
      InputStream inputStream,
      String xVarNamePattern,
      String yVarNamePattern
  ) throws IOException {
    List<Map<String, String>> data = loadFromCSV(inputStream);
    List<String> varNames = data.get(0).keySet().stream().toList();
    return buildDataset(
        data,
        varNames.stream().filter(n -> n.matches(xVarNamePattern)).toList(),
        varNames.stream().filter(n -> n.matches(yVarNamePattern)).toList()
    );
  }

  public static Dataset loadFromCSV(InputStream inputStream, String yVarName) throws IOException {
    return loadFromCSV(inputStream, List.of(yVarName));
  }

  private static List<Map<String, String>> loadFromCSV(InputStream inputStream) throws IOException {
    try (inputStream) {
      CSVParser parser = CSVFormat.Builder.create()
          .setDelimiter(";")
          .build()
          .parse(new InputStreamReader(inputStream));
      List<CSVRecord> records = parser.getRecords();
      List<String> varNames = records.get(0).stream().toList();
      return records.stream().skip(1)
          .map(r -> IntStream.range(0, varNames.size())
              .mapToObj(i -> Map.entry(varNames.get(i), r.get(i)))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
          )
          .toList();
    }
  }

  public static void main(String[] args) throws IOException {
    System.out.println(Dataset.loadFromCSV(Dataset.class.getResourceAsStream("/datasets/regression/concrete.csv"), List.of("strength")));
    System.out.println(Dataset.loadFromCSV(Dataset.class.getResourceAsStream("/datasets/regression/xor.csv"), "x\\d+", "y"));
  }

  public static List<String> varNames(String name, int number) {
    int digits = (int) Math.ceil(Math.log10(number + 1));
    return IntStream.range(1, number + 1).mapToObj((name + "%0" + digits + "d")::formatted).toList();
  }

  @Override
  public String toString() {
    return "Dataset{" +
        "n=" + examples.size() +
        ", xVarNames=" + xVarNames +
        ", yVarNames=" + yVarNames +
        '}';
  }
}
