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

package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.problem.classification.DataUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * @author eric
 */
public class DataBasedSymbolicRegressionProblem extends SymbolicRegressionProblem<SymbolicRegressionFitness> {

  public DataBasedSymbolicRegressionProblem(
      List<Pair<double[], Double>> trainingData,
      List<Pair<double[], Double>> validationData,
      SymbolicRegressionFitness.Metric metric
  ) {
    super(
        new SymbolicRegressionFitness(trainingData, metric),
        new SymbolicRegressionFitness(validationData, metric)
    );
  }

  public DataBasedSymbolicRegressionProblem(
      List<Pair<double[], Double>> data,
      int folds,
      int i,
      SymbolicRegressionFitness.Metric metric
  ) {
    this(DataUtils.splitData(data, i, folds).first(), DataUtils.splitData(data, i, folds).second(), metric);
  }

  public DataBasedSymbolicRegressionProblem(
      String filename, String yColumnName, int folds, int i, SymbolicRegressionFitness.Metric metric
  ) throws IOException {
    this(buildData(filename, yColumnName), folds, i, metric);
  }

  private static List<Pair<double[], Double>> buildData(String filename, String yColumnName) throws IOException {
    Reader reader = new FileReader(filename);
    CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
        .setDelimiter(';')
        .setHeader().setSkipHeaderRecord(true)
        .build();
    CSVParser csvParser = csvFormat.parse(reader);
    List<String> headers = csvParser.getHeaderNames().stream().filter(s -> !s.equals(yColumnName)).toList();
    List<CSVRecord> records = csvParser.getRecords();

    return records.stream().map(record -> Pair.of(
        headers.stream().mapToDouble(header -> Double.parseDouble(record.get(header))).toArray(),
        Double.parseDouble(record.get(yColumnName))
    )).toList();
  }

}
