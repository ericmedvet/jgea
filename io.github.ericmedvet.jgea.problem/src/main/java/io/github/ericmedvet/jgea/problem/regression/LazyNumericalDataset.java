/*-
 * ========================LICENSE_START=================================
 * jgea-problem
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class LazyNumericalDataset implements NumericalDataset {

  private static final Map<DatasetKey, NumericalDataset> FILTERED_DATASETS = new HashMap<>();
  private static final Map<String, NumericalDataset> DATASETS = new HashMap<>();
  private final String path;
  private final List<String> xVarNames;
  private final List<String> yVarNames;

  public LazyNumericalDataset(String path, List<String> xVarNames, List<String> yVarNames) {
    this.path = path;
    this.xVarNames = xVarNames;
    this.yVarNames = yVarNames;
  }

  public LazyNumericalDataset(String path, String xVarNamePattern, String yVarNamePattern) throws IOException {
    // read just varNames
    NumericalDataset dataset = getDataset(path);
    this.path = path;
    xVarNames = dataset.xVarNames().stream()
        .filter(n -> n.matches(xVarNamePattern))
        .sorted()
        .toList();
    yVarNames = dataset.yVarNames().stream()
        .filter(n -> n.matches(yVarNamePattern))
        .sorted()
        .toList();
  }

  private record DatasetKey(String path, List<String> xVarNames, List<String> yVarNames) {}

  private static class FilteredNumericalDataset implements NumericalDataset {
    private final NumericalDataset dataset;
    private final List<String> xVarNames;

    private final List<String> yVarNames;

    public FilteredNumericalDataset(NumericalDataset dataset, List<String> xVarNames, List<String> yVarNames) {
      this.dataset = dataset;
      this.xVarNames = xVarNames;
      this.yVarNames = yVarNames;
    }

    @Override
    public IntFunction<Example> exampleProvider() {
      return i -> {
        NamedExample ne = dataset.namedExampleProvider().apply(i);
        return new Example(
            xVarNames.stream().mapToDouble(n -> ne.x().get(n)).toArray(),
            yVarNames.stream().mapToDouble(n -> ne.y().get(n)).toArray());
      };
    }

    @Override
    public int size() {
      return dataset.size();
    }

    @Override
    public List<String> xVarNames() {
      return xVarNames;
    }

    @Override
    public List<String> yVarNames() {
      return yVarNames;
    }

    @Override
    public IntFunction<NamedExample> namedExampleProvider() {
      return i -> {
        NamedExample ne = dataset.namedExampleProvider().apply(i);
        return new NamedExample(
            xVarNames.stream()
                .collect(Collectors.toMap(n -> n, n -> ne.x().get(n))),
            yVarNames.stream()
                .collect(Collectors.toMap(n -> n, n -> ne.x().get(n))));
      };
    }
  }

  private static NumericalDataset getDataset(String path) {
    NumericalDataset dataset = DATASETS.get(path);
    if (dataset == null) {
      try {
        dataset = ListNumericalDataset.loadFromCSV(new FileInputStream(path), ".*", ".*");
        DATASETS.put(path, dataset);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return dataset;
  }

  private static NumericalDataset getFilteredDataset(DatasetKey key) {
    NumericalDataset fDataset = FILTERED_DATASETS.get(key);
    if (fDataset == null) {
      NumericalDataset dataset = getDataset(key.path());
      fDataset = new FilteredNumericalDataset(dataset, key.xVarNames, key.yVarNames);
      FILTERED_DATASETS.put(key, fDataset);
    }
    return fDataset;
  }

  @Override
  public IntFunction<Example> exampleProvider() {
    return i -> {
      NumericalDataset dataset = getFilteredDataset();
      synchronized (dataset) {
        return dataset.exampleProvider().apply(i);
      }
    };
  }

  @Override
  public int size() {
    return getFilteredDataset().size();
  }

  @Override
  public List<String> xVarNames() {
    return xVarNames;
  }

  @Override
  public List<String> yVarNames() {
    return yVarNames;
  }

  private NumericalDataset getFilteredDataset() {
    return getFilteredDataset(new DatasetKey(path, xVarNames, yVarNames));
  }

  @Override
  public String toString() {
    return "LazyDataset{"
        + "n="
        + (!FILTERED_DATASETS.containsKey(new DatasetKey(path, xVarNames, yVarNames))
            ? "NA"
            : getFilteredDataset().size())
        + ", xVarNames="
        + xVarNames
        + ", yVarNames="
        + yVarNames
        + '}';
  }
}
