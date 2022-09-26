package it.units.malelab.jgea.problem.classification;

import it.units.malelab.jgea.core.util.Pair;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

public class DatasetClassificationProblem extends ClassificationProblem<double[], Integer> {

  private static final Collection<Character> DELIMITERS = Set.of(',', ';', '\t');

  private final int numberOfFeatures;
  private final int numberOfClasses;

  public DatasetClassificationProblem(
      List<Pair<double[], Label<Integer>>> data,
      int folds,
      int i,
      ClassificationFitness.Metric learningErrorMetric,
      ClassificationFitness.Metric validationErrorMetric
  ) {
    super(data, folds, i, learningErrorMetric, validationErrorMetric);
    numberOfFeatures = data.get(0).first().length;
    numberOfClasses = data.get(0).second().values().size();
  }

  public DatasetClassificationProblem(
      String filename, int numberOfClasses, String yColumnName, int folds, int i,
      ClassificationFitness.Metric learningErrorMetric,
      ClassificationFitness.Metric validationErrorMetric
  ) throws IOException {
    this(buildData(filename, yColumnName, numberOfClasses), folds, i, learningErrorMetric, validationErrorMetric);
  }

  public int getNumberOfFeatures() {
    return numberOfFeatures;
  }

  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  private static List<Pair<double[], Label<Integer>>> buildData(String filename, String yColumnName, int numberOfClasses) throws IOException {
    char delimiter = inferColumnDelimiter(filename);
    Label.IntLabelFactory labelFactory = new Label.IntLabelFactory(numberOfClasses);

    Reader reader = new FileReader(filename);
    CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
        .setDelimiter(delimiter)
        .setHeader().setSkipHeaderRecord(true)
        .build();
    CSVParser csvParser = csvFormat.parse(reader);
    List<String> headers = csvParser.getHeaderNames().stream().filter(s -> !s.equals(yColumnName)).toList();
    List<CSVRecord> records = csvParser.getRecords();

    return records.stream().map(record -> Pair.of(
        headers.stream().mapToDouble(header -> Double.parseDouble(record.get(header))).toArray(),
        labelFactory.getLabel(Integer.parseInt(record.get(yColumnName)))
    )).toList();
  }

  private static char inferColumnDelimiter(String filename) throws IOException {
    Reader reader = new FileReader(filename);
    BufferedReader bufferedReader = new BufferedReader(reader);
    String headerLine = bufferedReader.readLine();
    bufferedReader.close();
    reader.close();
    Map<Character, Integer> delimiterCountMap = DELIMITERS.stream()
        .collect(Collectors.toMap(c -> c, c -> StringUtils.countMatches(headerLine, c)));
    return Collections.max(delimiterCountMap.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
  }

}
