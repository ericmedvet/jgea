package io.github.ericmedvet.jgea.problem.synthetic;

import io.github.ericmedvet.jgea.core.problem.ComparableQualityBasedProblem;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class DoublesVariableTarget implements ComparableQualityBasedProblem<List<Double>, Double>,
        ProblemWithExampleSolution<List<Double>> {

    private final double target;
    private final int p;
    private final Function<List<Double>, Double> fitnessFunction;
    public DoublesVariableTarget(int p, double target){
        this.p = p;
        this.target = target;
        fitnessFunction = vs -> {
            if (vs.size() != p) {
                throw new IllegalArgumentException("Wrong input size: %d expected, %d found".formatted(p, vs.size()));
            }
            return vs.stream().mapToDouble(i -> Math.abs(target - i)).sum() / (double) vs.size();
        };
    }

    @Override
    public List<Double> example() {
        return Collections.nCopies(p, target);
    }

    @Override
    public Function<List<Double>, Double> qualityFunction() {
        return fitnessFunction;
    }
}
