package it.units.malelab.jgea.sample.experimenter;

import it.units.malelab.jgea.experimenter.InvertibleMapper;
import it.units.malelab.jgea.experimenter.PreparedNamedBuilder;
import it.units.malelab.jgea.problem.synthetic.Sphere;
import it.units.malelab.jnb.core.NamedBuilder;
import it.units.malelab.jnb.core.Param;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2022/11/24 for jgea
 */
public class Starter {

  public static class Builders {
    public static InvertibleMapper<List<Double>, List<Double>> fixed(@Param("n") int n) {
      return new InvertibleMapper<>() {
        @Override
        public List<Double> apply(List<Double> doubles) {
          if (doubles.size() != n) {
            throw new IllegalArgumentException("Wrong input size: %d found, %d expected".formatted(doubles.size(), n));
          }
          return doubles;
        }

        @Override
        public List<Double> exampleInput() {
          return Arrays.stream((new double[n])).boxed().toList();
        }
      };
    }

    public static <T> Function<T, T> identity() {
      return t -> t;
    }

    public static Function<List<Double>, Double> sphere() {
      return new Sphere().qualityFunction();
    }
  }

  public static void main(String[] args) {
    String expDesc = """
        ea.experiment(
          runs = [
            ea.run(
              solver = ea.s.numGA(
                mapper = fixed(n = 10);
                nEval = 100;
              );
              randomGenerator = ea.rg.defaultRG(seed = 1);
              problem = ea.p.totalOrder(
                qFunction = sphere();
                cExtractor = identity()
              )
            )
          ]
        )
        """;
    NamedBuilder<?> nb = NamedBuilder.empty()
        .and(PreparedNamedBuilder.get());
  }
}
