package it.units.malelab.jgea.problem.classification;

import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.representation.sequence.bit.BitString;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.IntStream;

public class NeuralNetworkClassifier implements Classifier<double[], Integer> {

  public enum ActivationFunction implements Function<Double, Double> {
    RELU(x -> (x < 0) ? 0d : x),
    SIGMOID(x -> 1d / (1d + Math.exp(-x))),
    SIN(Math::sin),
    TANH(Math::tanh),
    SIGN(Math::signum),
    IDENTITY(x -> x);

    private final Function<Double, Double> f;

    ActivationFunction(Function<Double, Double> f) {
      this.f = f;
    }

    public Double apply(Double x) {
      return f.apply(x);
    }

  }

  private final ActivationFunction activationFunction;
  private final double[][][] weights;
  private final int[] neurons;
  private final Label.IntLabelFactory labelsFactory;

  public NeuralNetworkClassifier(ActivationFunction activationFunction, double[][][] weights, int[] neurons) {
    this.activationFunction = activationFunction;
    this.weights = weights;
    this.neurons = neurons;
    this.labelsFactory = new Label.IntLabelFactory(neurons[neurons.length - 1]);
  }

  public NeuralNetworkClassifier(double[] flatWeights, int[] neurons) {
    this(ActivationFunction.TANH, unflat(flatWeights, neurons), neurons);
  }

  @Override
  public Label<Integer> classify(double[] o) {
    double[] outputs = apply(o);
    double max = Arrays.stream(outputs).max().orElseThrow();
    int prediction = Arrays.stream(outputs).boxed().toList().indexOf(max);
    return labelsFactory.getLabel(prediction);
  }

  public double[] apply(double[] input) {
    if (input.length != neurons[0]) {
      throw new IllegalArgumentException(String.format(
          "Expected input length is %d: found %d",
          neurons[0],
          input.length
      ));
    }
    double[][] activationValues = new double[neurons.length][];
    activationValues[0] = Arrays.stream(input).map(activationFunction.f::apply).toArray();
    for (int i = 1; i < neurons.length; i++) {
      activationValues[i] = new double[neurons[i]];
      for (int j = 0; j < neurons[i]; j++) {
        double sum = weights[i - 1][j][0]; //set the bias
        for (int k = 1; k < neurons[i - 1] + 1; k++) {
          sum = sum + activationValues[i - 1][k - 1] * weights[i - 1][j][k];
        }
        activationValues[i][j] = activationFunction.apply(sum);
      }
    }
    return activationValues[neurons.length - 1];
  }

  private static double[][][] unflat(double[] flatWeights, int[] neurons) {
    double[][][] unflatWeights = new double[neurons.length - 1][][];
    int c = 0;
    for (int i = 1; i < neurons.length; i++) {
      unflatWeights[i - 1] = new double[neurons[i]][neurons[i - 1] + 1];
      for (int j = 0; j < neurons[i]; j++) {
        for (int k = 0; k < neurons[i - 1] + 1; k++) {
          unflatWeights[i - 1][j][k] = flatWeights[c];
          c = c + 1;
        }
      }
    }
    return unflatWeights;
  }

  public static double[] flat(double[][][] unflatWeights, int[] neurons) {
    double[] flatWeights = new double[countWeights(neurons)];
    int c = 0;
    for (int i = 1; i < neurons.length; i++) {
      for (int j = 0; j < neurons[i]; j++) {
        for (int k = 0; k < neurons[i - 1] + 1; k++) {
          flatWeights[c] = unflatWeights[i - 1][j][k];
          c = c + 1;
        }
      }
    }
    return flatWeights;
  }

  public static int countWeights(int[] neurons) {
    int c = 0;
    for (int i = 1; i < neurons.length; i++) {
      c = c + neurons[i] * (neurons[i - 1] + 1);
    }
    return c;
  }

  public void updateWeights(double[] weights) {
    double[][][] newWeights = unflat(weights, neurons);
    for (int l = 0; l < newWeights.length; l++) {
      for (int s = 0; s < newWeights[l].length; s++) {
        System.arraycopy(newWeights[l][s], 0, this.weights[l][s], 0, newWeights[l][s].length);
      }
    }
  }

  public void prune(BitString bitString) {
    double[] flatWeights = flat(weights, neurons);
    if (bitString.size() != flatWeights.length) {
      throw new IllegalArgumentException(String.format("Pruning indexes differ weights size: %d expected, %d found",
          flatWeights.length,
          bitString.size()
      ));
    }
    double[] updatedWeights = IntStream.range(0, flatWeights.length).mapToDouble(i ->
        bitString.get(i) ? flatWeights[i] : 0d
    ).toArray();
    updateWeights(updatedWeights);
  }

  public void prune(double rate) {
    double[] flatWeights = flat(weights, neurons);
    double[] absWeights = Arrays.stream(flatWeights).map(Math::abs).toArray();
    int weightsToKeep = Math.max((int) (1 - rate) * absWeights.length, absWeights.length);
    IntStream.range(0, absWeights.length)
        .mapToObj(i -> Pair.of(i, absWeights[i]))
        .sorted(Comparator.comparingDouble(Pair::second))
        .limit(weightsToKeep)
        .forEach(p -> flatWeights[p.first()] = 0);
    updateWeights(flatWeights);
  }

}
