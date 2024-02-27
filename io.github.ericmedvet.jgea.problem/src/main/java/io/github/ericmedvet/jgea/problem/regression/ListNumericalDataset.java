/*-
 * ========================LICENSE_START=================================
 * jgea-problem
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.jgea.problem.regression;

import io.github.ericmedvet.jsdynsym.core.numerical.MultivariateRealFunction;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.IntFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public record ListNumericalDataset(List<Example> examples, List<String> xVarNames, List<String> yVarNames)
    implements NumericalDataset {

  private static final Logger L = Logger.getLogger(ListNumericalDataset.class.getName());

  public ListNumericalDataset {
    List<Integer> xsSizes =
        examples.stream().map(e -> e.xs().length).distinct().toList();
    List<Integer> ysSizes =
        examples.stream().map(e -> e.ys().length).distinct().toList();
    if (!examples.isEmpty()) {
      if (xsSizes.size() > 1) {
        throw new IllegalArgumentException(
            "Size of x is not consistent across examples, found sizes %s".formatted(xsSizes));
      }
      if (ysSizes.size() > 1) {
        throw new IllegalArgumentException(
            "Size of y is not consistent across examples, found sizes %s".formatted(ysSizes));
      }
      if (xVarNames.size() != xsSizes.get(0)) {
        throw new IllegalArgumentException(
            ("Number of names of x vars is different form size of x in examples: %d vs" + " " + "%d")
                .formatted(xVarNames().size(), xsSizes.get(0)));
      }
      if (yVarNames.size() != ysSizes.get(0)) {
        throw new IllegalArgumentException(
            ("Number of names of y vars is different form size of y in examples: %d vs" + " " + "%d")
                .formatted(xVarNames().size(), xsSizes.get(0)));
      }
    }
  }

  public ListNumericalDataset(List<Example> examples) {
    this(
        examples,
        MultivariateRealFunction.varNames("x", examples.get(0).xs().length),
        MultivariateRealFunction.varNames("y", examples.get(0).ys().length));
  }

  private static NumericalDataset buildDataset(
      List<Map<String, String>> data, List<String> xVarNames, List<String> yVarNames) {
    return new ListNumericalDataset(
            data.stream()
                .map(dp -> new Example(
                    xVarNames.stream()
                        .mapToDouble(n -> Double.parseDouble(dp.get(n)))
                        .toArray(),
                    yVarNames.stream()
                        .mapToDouble(n -> Double.parseDouble(dp.get(n)))
                        .toArray()))
                .toList(),
            xVarNames,
            yVarNames)
        .shuffled(1);
  }

  public static NumericalDataset loadFromCSV(InputStream inputStream, List<String> xVarNames, List<String> yVarNames)
      throws IOException {
    List<Map<String, String>> data = loadFromCSV(inputStream, Long.MAX_VALUE);
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

  public static NumericalDataset loadFromCSV(InputStream inputStream, String xVarNamePattern, String yVarNamePattern)
      throws IOException {
    List<Map<String, String>> data = loadFromCSV(inputStream, Long.MAX_VALUE);
    List<String> varNames = data.get(0).keySet().stream().toList();
    return buildDataset(
        data,
        varNames.stream().filter(n -> n.matches(xVarNamePattern)).toList(),
        varNames.stream().filter(n -> n.matches(yVarNamePattern)).toList());
  }

  public static NumericalDataset loadFromCSV(InputStream inputStream, String yVarName) throws IOException {
    return loadFromCSV(inputStream, List.of(yVarName));
  }

  public static NumericalDataset loadFromCSV(InputStream inputStream, List<String> yVarNames) throws IOException {
    List<Map<String, String>> data = loadFromCSV(inputStream, Long.MAX_VALUE);
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

  private static List<Map<String, String>> loadFromCSV(InputStream inputStream, long limit) throws IOException {
    try (inputStream) {
      CSVParser parser =
          CSVFormat.Builder.create().setDelimiter(";").build().parse(new InputStreamReader(inputStream));
      List<CSVRecord> records = parser.getRecords();
      List<String> varNames = records.get(0).stream().toList();
      List<Map<String, String>> maps = new ArrayList<>();
      int lc = 0;
      for (CSVRecord record : records) {
        if (lc >= limit) {
          break;
        }
        if (lc != 0) {
          if (record.size() != varNames.size()) {
            L.warning("Line %d/%d has %d items instead of expected %d: skipping it"
                .formatted(lc, records.size(), record.size(), varNames.size()));
          } else {
            Map<String, String> map = IntStream.range(0, varNames.size())
                .mapToObj(i -> Map.entry(varNames.get(i), record.get(i)))
                .collect(Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue, (s1, s2) -> s1, LinkedHashMap::new));
            maps.add(map);
          }
        }
        lc = lc + 1;
      }
      return maps;
    }
  }

  public static NumericalDataset loadFromCSVResource(String name, List<String> xVarNames, List<String> yVarNames)
      throws IOException {
    return loadFromCSV(ListNumericalDataset.class.getResourceAsStream(name), xVarNames, yVarNames);
  }

  public static NumericalDataset loadFromCSVResource(String name, List<String> yVarNames) throws IOException {
    return loadFromCSV(ListNumericalDataset.class.getResourceAsStream(name), yVarNames);
  }

  public static NumericalDataset loadFromCSVResource(String name, String yVarName) throws IOException {
    return loadFromCSV(ListNumericalDataset.class.getResourceAsStream(name), yVarName);
  }

  public static NumericalDataset loadFromCSVResource(String name, String xVarNamePattern, String yVarNamePattern)
      throws IOException {
    return loadFromCSV(ListNumericalDataset.class.getResourceAsStream(name), xVarNamePattern, yVarNamePattern);
  }

  @Override
  public IntFunction<Example> exampleProvider() {
    return i -> examples().get(i);
  }

  @Override
  public int size() {
    return examples().size();
  }

  @Override
  public String toString() {
    return "Dataset{" + "n=" + examples.size() + ", xVarNames=" + xVarNames + ", yVarNames=" + yVarNames + '}';
  }

  public ListNumericalDataset shuffled(long seed) {
    List<Example> shuffledExamples = new ArrayList<>(examples);
    Collections.shuffle(shuffledExamples, new Random(seed));
    return new ListNumericalDataset(shuffledExamples, xVarNames, yVarNames);
  }
}
