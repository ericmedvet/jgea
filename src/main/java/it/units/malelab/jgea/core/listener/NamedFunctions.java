package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.Event;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.core.util.Sized;
import it.units.malelab.jgea.core.util.TextPlotter;

import java.util.*;
import java.util.function.Function;

/**
 * @author eric on 2021/01/02 for jgea
 */
public class NamedFunctions {

  private NamedFunctions() {
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Evolver.State> state() {
    return f(
        "state",
        "%s",
        Event::getState
    );
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Integer> iterations() {
    return f(
        "iterations",
        "%4d",
        e -> e.getState().getIterations()
    );
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Integer> births() {
    return f(
        "births",
        "%5d",
        e -> e.getState().getBirths()
    );
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Integer> fitnessEvaluations() {
    return f(
        "fitness.evaluations",
        "%5d",
        e -> e.getState().getFitnessEvaluations()
    );
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Float> elapsedSeconds() {
    return f(
        "elapsed.seconds",
        "%5.1f",
        e -> e.getState().getElapsedMillis() / 1000f
    );
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Collection<? extends Individual<? extends G, ? extends S, ? extends F>>> firsts() {
    return f(
        "firsts",
        "%s",
        e -> e.getOrderedPopulation().firsts()
    );
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Collection<? extends Individual<? extends G, ? extends S, ? extends F>>> lasts() {
    return f(
        "lasts",
        "%s",
        e -> e.getOrderedPopulation().lasts()
    );
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Collection<? extends Individual<? extends G, ? extends S, ? extends F>>> all() {
    return f(
        "all",
        "%s",
        e -> e.getOrderedPopulation().all()
    );
  }

  public static <T> NamedFunction<Collection<? extends T>, T> one() {
    return f(
        "one",
        "%s",
        Misc::first
    );
  }

  public static <T> NamedFunction<Collection<? extends T>, T> max(Comparator<T> comparator) {
    return f(
        "max",
        "%s",
        ts -> ts.stream().max(comparator).orElse(null)
    );
  }

  public static <T> NamedFunction<Collection<? extends T>, T> min(Comparator<T> comparator) {
    return f(
        "min",
        "%s",
        ts -> ts.stream().min(comparator).orElse(null)
    );
  }

  public static <T> NamedFunction<Collection<? extends T>, T> median(Comparator<T> comparator) {
    return f(
        "median",
        "%s",
        ts -> Misc.median(ts, comparator)
    );
  }

  public static <T> NamedFunction<List<? extends T>, T> nth(int index) {
    return f(
        "[" + index + "]",
        "%s",
        Misc::first
    );
  }

  public static <F, T> NamedFunction<Collection<? extends F>, Collection<T>> each(NamedFunction<F, T> mapper) {
    return f(
        "map[" + mapper.getName() + "]",
        "%s",
        individuals -> individuals.stream().map(mapper).collect(java.util.stream.Collectors.toList())
    );
  }

  public static <F, T> NamedFunction<F, T> f(Function<F, T> function) {
    return function::apply;
  }

  public static <F, T> NamedFunction<F, T> f(String name, Function<F, T> function) {
    return f(name, "%s", function);
  }

  public static <F, T> NamedFunction<F, T> f(String name, String format, Function<F, T> function) {
    return NamedFunction.build(name, format, function);
  }

  public static NamedFunction<Collection<?>, Double> uniqueness() {
    return f(
        "uniqueness",
        "%4.2f",
        ts -> (double) ts.stream().distinct().count() / (double) ts.size()
    );
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Individual<? extends G, ? extends S, ? extends F>> best() {
    return f(
        "best",
        "%s",
        e -> Misc.first(e.getOrderedPopulation().firsts())
    );
  }

  @SuppressWarnings("unchecked")
  public static NamedFunction<Collection<? extends Number>, String> hist(int bins) {
    return f(
        "hist",
        "%" + bins + "." + bins + "s",
        values -> TextPlotter.histogram(values instanceof List ? (List<? extends Number>) values : new ArrayList<>(values), bins)
    );
  }

  public static <G> NamedFunction<Individual<? extends G, ?, ?>, G> genotype() {
    return f("genotype", "%s", Individual::getGenotype);
  }

  public static <S> NamedFunction<Individual<?, ? extends S, ?>, S> solution() {
    return f("solution", "%s", Individual::getSolution);
  }

  public static <F> NamedFunction<Individual<?, ?, ? extends F>, F> fitness() {
    return f("fitness", "%s", Individual::getFitness);
  }

  public static NamedFunction<Individual<?, ?, ?>, Integer> birthIteration() {
    return f("birth.iteration", "%4d", Individual::getBirthIteration);
  }

  public static NamedFunction<Object, Number> size() {
    return f("size", "%3d", NamedFunctions::size);
  }

  public static <G, S, F, T> NamedFunction<Event<? extends G, ? extends S, ? extends F>, T> constant(String name, String format, T value) {
    return f(
        name,
        format,
        e -> value
    );
  }

  public static <G, S, F, T> NamedFunction<Event<? extends G, ? extends S, ? extends F>, T> constant(String name, T value) {
    return constant(name, NamedFunction.format(value.toString().length()), value);
  }

  public static <G, S, F, T> NamedFunction<Event<? extends G, ? extends S, ? extends F>, T> constant(String name, String format, Map<String, T> values) {
    return f(
        name,
        format,
        e -> values.get(name)
    );
  }

  @SuppressWarnings("unchecked")
  public static <T> NamedFunction<Object, T> as(Class<T> clazz) {
    return f(
        "as[" + clazz.getSimpleName() + "]",
        "%s",
        o -> (T) o
    );
  }

  public static Integer size(Object o) {
    if (o instanceof Sized) {
      return ((Sized) o).size();
    }
    if (o instanceof Collection) {
      if (Misc.first((Collection<?>) o) instanceof Sized) {
        return ((Collection<?>) o).stream().mapToInt(i -> ((Sized) i).size()).sum();
      }
      return ((Collection<?>) o).size();
    }
    if (o instanceof String) {
      return ((String) o).length();
    }
    if (o instanceof Pair) {
      Integer firstSize = size(((Pair<?, ?>) o).first());
      Integer secondSize = size(((Pair<?, ?>) o).second());
      if ((firstSize != null) && (secondSize != null)) {
        return firstSize + secondSize;
      }
    }
    return null;
  }

}
