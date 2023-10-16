
package io.github.ericmedvet.jgea.problem.booleanfunction;

import io.github.ericmedvet.jgea.core.fitness.ListCaseBasedFitness;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import io.github.ericmedvet.jgea.core.representation.tree.booleanfunction.Element;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
public class BooleanFunctionFitness extends ListCaseBasedFitness<List<Tree<Element>>, boolean[], Boolean, Double> {

  public BooleanFunctionFitness(TargetFunction targetFunction, List<boolean[]> observations) {
    super(observations, new Error(targetFunction), new ErrorRate());
  }

  public interface TargetFunction extends Function<boolean[], boolean[]> {
    String[] varNames();

    static TargetFunction from(final Function<boolean[], boolean[]> function, final String... varNames) {
      return new TargetFunction() {
        @Override
        public boolean[] apply(boolean[] values) {
          return function.apply(values);
        }

        @Override
        public String[] varNames() {
          return varNames;
        }
      };
    }
  }

  private static class Error implements BiFunction<List<Tree<Element>>, boolean[], Boolean> {

    private final BooleanFunctionFitness.TargetFunction targetFunction;

    public Error(BooleanFunctionFitness.TargetFunction targetFunction) {
      this.targetFunction = targetFunction;
    }

    @Override
    public Boolean apply(List<Tree<Element>> solution, boolean[] observation) {
      Map<String, Boolean> varValues = new LinkedHashMap<>();
      for (int i = 0; i < targetFunction.varNames().length; i++) {
        varValues.put(targetFunction.varNames()[i], observation[i]);
      }
      boolean[] computed = BooleanUtils.compute(solution, varValues);
      return Arrays.equals(computed, targetFunction.apply(observation));
    }

  }

  private static class ErrorRate implements Function<List<Boolean>, Double> {

    @Override
    public Double apply(List<Boolean> vs) {
      double errors = 0;
      for (Boolean v : vs) {
        errors = errors + (v ? 0d : 1d);
      }
      return errors / (double) vs.size();
    }

  }

}
