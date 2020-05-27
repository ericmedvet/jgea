package it.units.malelab.jgea.problem.synthetic;

import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;

public class Rastrigin implements Problem<double[], Double> {

    private static class FitnessFunction implements Function<double[], Double>, Bounded<Double> {

        @Override
        public Double bestValue() {
            return 0d;
        }

        @Override
        public Double worstValue() {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public Double apply(double[] solution, Listener listener) throws FunctionException {
            double sum = 0.0;
            for (int i = 0; i < solution.length; i++) {
                sum += solution[i] * solution[i] - 10 * Math.cos(2 * Math.PI * solution[i]);
            }
            return 10 * solution.length + sum;
        }
    }

    private final FitnessFunction fitnessFunction = new FitnessFunction();

    @Override
    public NonDeterministicFunction<double[], Double> getFitnessFunction() {
        return fitnessFunction;
    }
}
