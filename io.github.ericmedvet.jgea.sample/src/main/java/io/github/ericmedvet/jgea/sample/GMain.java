package io.github.ericmedvet.jgea.sample;

import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.listener.TabularPrinter;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.ListPopulationState;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.solver.State;
import io.github.ericmedvet.jgea.core.util.Progress;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.github.ericmedvet.jgea.core.listener.NamedFunctions.*;

public class GMain {
  public static void main(String[] args) {
    Function<? super Double, String> f1 = v -> "d(%s)=%.1f".formatted(v.getClass().getSimpleName(), toDouble(v));
    Function<Number, String> f2 = v -> "n(%s)=%.1f".formatted(v.getClass().getSimpleName(), v.doubleValue());
    Function<? super Double, String> f3 = v -> v.describeConstable().orElseThrow().toString();

    Double v = 3d;
    System.out.println(f2.apply(Integer.parseInt("3")));
    List<Function<Number, String>> fs = Stream.of((Function<Number, String>) f1, f2, (Function<Number, String>) f3)
        .toList();
    Number n = 3d;
    System.out.println(fs.stream().map(f -> f.apply(v)).toList());
    System.out.println(fs.stream().map(f -> f.apply(n)).toList());

    nf();
  }

  protected record ListState<I extends Individual<G, S, Q>, G, S, Q>(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      Progress progress,
      long nOfBirths,
      long nOfFitnessEvaluations,
      PartiallyOrderedCollection<I> pocPopulation,
      List<I> listPopulation
  )
      implements ListPopulationState<I, G, S, Q> {
    public static <I extends Individual<G, S, Q>, G, S, Q> ListState<I, G, S, Q> from(
        ListState<I, G, S, Q> state,
        Progress progress,
        long nOfBirths,
        long nOfFitnessEvaluations,
        Collection<I> listPopulation,
        Comparator<? super I> comparator
    ) {
      return new ListState<>(
          state.startingDateTime,
          ChronoUnit.MILLIS.between(state.startingDateTime, LocalDateTime.now()),
          state.nOfIterations() + 1,
          progress,
          state.nOfBirths() + nOfBirths,
          state.nOfFitnessEvaluations() + nOfFitnessEvaluations,
          PartiallyOrderedCollection.from(listPopulation, comparator),
          listPopulation.stream().sorted(comparator).toList()
      );
    }

    public static <I extends Individual<G, S, Q>, G, S, Q> ListState<I, G, S, Q> from(
        Collection<I> listPopulation, Comparator<? super I> comparator
    ) {
      return new ListState<>(
          LocalDateTime.now(),
          0,
          0,
          Progress.NA,
          listPopulation.size(),
          listPopulation.size(),
          PartiallyOrderedCollection.from(listPopulation, comparator),
          listPopulation.stream().sorted(comparator).toList()
      );
    }
  }

  public static void nf() {
    NamedFunction<State, Long> f1 = nOfIterations();
    //NamedFunction<? super POCPopulationState<?, ?, ?, ?>, Object> f2 = quality().of(best()).reformat("%.02f");
    NamedFunction<? extends State, Long> f2 = (GMain.<Individual<BitString, BitString, Double>, BitString, BitString, Double>births());

    new BitString("001");
    POCPopulationState<Individual<BitString, BitString, Double>, BitString, BitString, Double> s = ListState.from(
        List.of(Individual.of(new BitString("001"), new BitString("001"), 0.5d, 1, 1)),
        Comparator.comparingDouble(Individual::quality)
    );

    System.out.println(f1.apply(s));
    System.out.println(f2.apply((State)s));
    System.out.println(quality().of(best()).reformat("%.2f").apply((POCPopulationState) s));
    System.out.println((GMain.<Individual<BitString, BitString, Double>, BitString, BitString, Double>births()).apply(s));
    System.out.println(nOfBirths().apply(s));

    TabularPrinter<State, Void> tp = new TabularPrinter<>(List.of(f1), List.of());
    tp.build(null).listen(s);

  }

  private static double toDouble(Double v) {
    return v;
  }

  public static <I extends Individual<G,S,Q>,G,S,Q> NamedFunction<POCPopulationState<I,G,S,Q>, Long> births() {
    return f("births", "%5d", POCPopulationState::nOfBirths);
  }
}
