package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.Event;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.core.util.Sized;
import it.units.malelab.jgea.core.util.TextPlotter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * @author eric on 2021/01/02 for jgea
 */
public class NamedFunctions {

  private final static String DEFAULT_FORMAT = "%s";
  private final static long CACHE_SIZE = 100;

  private NamedFunctions() {
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Evolver.State> state() {
    return f("state", Event::getState);
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Integer> iterations() {
    return f("iterations", "%4d", e -> e.getState().getIterations());
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Integer> births() {
    return f("births", "%5d", e -> e.getState().getBirths());
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Integer> fitnessEvaluations() {
    return f("fitness.evaluations", "%5d", e -> e.getState().getFitnessEvaluations());
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Float> elapsedSeconds() {
    return f("elapsed.seconds", "%5.1f", e -> e.getState().getElapsedMillis() / 1000f);
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Collection<? extends Individual<? extends G, ? extends S, ? extends F>>> firsts() {
    return f("firsts", e -> e.getOrderedPopulation().firsts());
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Collection<? extends Individual<? extends G, ? extends S, ? extends F>>> lasts() {
    return f("lasts", e -> e.getOrderedPopulation().lasts());
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Collection<? extends Individual<? extends G, ? extends S, ? extends F>>> all() {
    return f("all", e -> e.getOrderedPopulation().all());
  }

  public static <T> NamedFunction<Collection<? extends T>, T> one() {
    return f("one", Misc::first);
  }

  public static <T> NamedFunction<Collection<? extends T>, T> max(Comparator<T> comparator) {
    return f("max", ts -> ts.stream().max(comparator).orElse(null));
  }

  public static <T> NamedFunction<Collection<? extends T>, T> min(Comparator<T> comparator) {
    return f("min", ts -> ts.stream().min(comparator).orElse(null));
  }

  public static <T> NamedFunction<Collection<? extends T>, T> median(Comparator<T> comparator) {
    return f("median", ts -> Misc.median(ts, comparator));
  }

  public static <T> NamedFunction<List<? extends T>, T> nth(int index) {
    return f("[" + index + "]", l -> l.get(index));
  }

  public static <F, T> NamedFunction<Collection<? extends F>, Collection<T>> each(NamedFunction<F, T> mapper) {
    return f("each[" + mapper.getName() + "]", individuals -> individuals.stream().map(mapper).collect(java.util.stream.Collectors.toList()));
  }

  public static <F, T> NamedFunction<F, T> f(String name, Function<F, T> function) {
    return f(name, DEFAULT_FORMAT, function);
  }

  public static <F, T> NamedFunction<F, T> f(String name, String format, Function<F, T> function) {
    return NamedFunction.build(name, format, function);
  }

  public static <F, T> NamedFunction<F, T> cachedF(String name, Function<F, T> function) {
    return f(name, Misc.cached(function, CACHE_SIZE));
  }

  public static <F, T> NamedFunction<F, T> cachedF(String name, String format, Function<F, T> function) {
    return f(name, format, Misc.cached(function, CACHE_SIZE));
  }

  public static <F, T> NamedFunction<F, T> cachedF(String name, String format, Function<F, T> function, long size) {
    return f(name, format, Misc.cached(function, size));
  }

  public static NamedFunction<Collection<?>, Double> uniqueness() {
    return f("uniqueness", "%4.2f",
        ts -> (double) ts.stream().distinct().count() / (double) ts.size()
    );
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Individual<? extends G, ? extends S, ? extends F>> best() {
    return f("best", e -> Misc.first(e.getOrderedPopulation().firsts()));
  }

  @SuppressWarnings("unchecked")
  public static NamedFunction<Collection<? extends Number>, String> hist(int bins) {
    return f("hist", NamedFunction.format(bins),
        values -> TextPlotter.histogram(values instanceof List ? (List<? extends Number>) values : new ArrayList<>(values), bins)
    );
  }

  public static <G> NamedFunction<Individual<? extends G, ?, ?>, G> genotype() {
    return f("genotype", Individual::getGenotype);
  }

  public static <S> NamedFunction<Individual<?, ? extends S, ?>, S> solution() {
    return f("solution", Individual::getSolution);
  }

  public static <F> NamedFunction<Individual<?, ?, ? extends F>, F> fitness() {
    return f("fitness", Individual::getFitness);
  }

  public static <G, S, F> NamedFunction<Individual<? extends G, ? extends S, ? extends F>, Integer> birthIteration() {
    return f("birth.iteration", "%4d", Individual::getBirthIteration);
  }

  public static <G, S, F> NamedFunction<Individual<? extends G, ? extends S, ? extends F>, Integer> genotypeBirthIteration() {
    return f("genotype.birth.iteration", "%4d", Individual::getGenotypeBirthIteration);
  }

  public static NamedFunction<Object, Number> size() {
    return f("size", "%3d", NamedFunctions::size);
  }

  public static <G, S, F, T> NamedFunction<Event<? extends G, ? extends S, ? extends F>, T> constant(String name, String format, T value) {
    return f(name, format, e -> value);
  }

  public static <G, S, F, T> NamedFunction<Event<? extends G, ? extends S, ? extends F>, T> constant(String name, T value) {
    return constant(name, NamedFunction.format(value.toString().length()), value);
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Object> eventAttribute(String name) {
    return f(name, e -> e.getAttributes().get(name));
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Object> eventAttribute(String name, String format) {
    return f(name, format, e -> e.getAttributes().get(name));
  }

  @SuppressWarnings("unchecked")
  public static <T> NamedFunction<Object, T> as(Class<T> clazz) {
    return f("as[" + clazz.getSimpleName() + "]", o -> (T) o);
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
