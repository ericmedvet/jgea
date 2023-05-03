package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.problem.regression.NumericalDataset;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionProblem;
import io.github.ericmedvet.jnb.core.Param;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author "Eric Medvet" on 2023/05/01 for jgea
 */
public class NumericalDatasets {
  private NumericalDatasets() {
  }

  @SuppressWarnings("unused")
  public static Supplier<NumericalDataset> empty(
      @Param("xVars") List<String> xVarNames,
      @Param("yVars") List<String> yVarNames
  ) {
    return () -> new NumericalDataset(List.of(), xVarNames, yVarNames);
  }

  @SuppressWarnings("unused")
  public static Supplier<NumericalDataset> fromFile(
      @Param("filePath") String filePath,
      @Param(value = "folds", dIs = {0}) List<Integer> folds,
      @Param(value = "nFolds", dI = 1) int nFolds,
      @Param(value = "xVarNamePattern", dS = "x.*") String xVarNamePattern,
      @Param(value = "yVarNamePattern", dS = "y.*") String yVarNamePattern
  ) {
    return () -> {
      try {
        return NumericalDataset
            .loadFromCSV(new FileInputStream(filePath), xVarNamePattern, yVarNamePattern)
            .folds(folds, nFolds);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };
  }

  @SuppressWarnings("unused")
  public static Supplier<NumericalDataset> fromProblem(
      @Param("problem") UnivariateRegressionProblem<UnivariateRegressionFitness> problem
  ) {
    return () -> problem.qualityFunction().getDataset();
  }
}
