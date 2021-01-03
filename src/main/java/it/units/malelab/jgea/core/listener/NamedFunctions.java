package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.listener.collector.IndividualBasicInfo;
import it.units.malelab.jgea.core.order.DAGPartiallyOrderedCollection;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.TextPlotter;
import it.units.malelab.jgea.representation.sequence.FixedLengthListFactory;
import it.units.malelab.jgea.representation.sequence.numeric.UniformDoubleFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/**
 * @author eric on 2021/01/02 for jgea
 */
public class NamedFunctions {

  private NamedFunctions() {
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Evolver.State> state() {
    return NamedFunction.build(
        "state",
        "%s",
        Event::getState
    );
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Integer> iterations() {
    return NamedFunction.build(
        "iterations",
        "%4d",
        e -> e.getState().getIterations()
    );
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Integer> births() {
    return NamedFunction.build(
        "births",
        "%5d",
        e -> e.getState().getBirths()
    );
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Integer> fitnessEvaluations() {
    return NamedFunction.build(
        "fitness.evaluations",
        "%5d",
        e -> e.getState().getFitnessEvaluations()
    );
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Float> elapsedSeconds() {
    return NamedFunction.build(
        "elapsed.seconds",
        "%5.1f",
        e -> e.getState().getElapsedMillis() / 1000f
    );
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Collection<? extends Individual<? extends G, ? extends S, ? extends F>>> firsts() {
    return NamedFunction.build(
        "firsts",
        "%s",
        e -> e.getOrderedPopulation().firsts()
    );
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Collection<? extends Individual<? extends G, ? extends S, ? extends F>>> lasts() {
    return NamedFunction.build(
        "lasts",
        "%s",
        e -> e.getOrderedPopulation().lasts()
    );
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Collection<? extends Individual<? extends G, ? extends S, ? extends F>>> all() {
    return NamedFunction.build(
        "all",
        "%s",
        e -> e.getOrderedPopulation().all()
    );
  }

  public static <T> NamedFunction<Collection<? extends T>, T> one() {
    return NamedFunction.build(
        "one",
        "%s",
        Misc::first
    );
  }

  public static <F, T> NamedFunction<Collection<? extends F>, List<T>> map(NamedFunction<F, T> mapper) {
    return NamedFunction.build(
        "map[" + mapper.getName() + "]",
        "%s",
        individuals -> individuals.stream().map(mapper).collect(java.util.stream.Collectors.toList())
    );
  }

  public static <F, T> NamedFunction<F, T> f(Function<F, T> function) {
    return function::apply;
  }

  public static <F, T> NamedFunction<F, T> f(String name, String format, Map<String, Function<F, T>> functions) {
    return NamedFunction.build(
        name,
        format,
        f -> functions.get(name).apply(f)
    );
  }

  public static NamedFunction<Collection<?>, Double> uniqueness() {
    return NamedFunction.build(
        "uniqueness",
        "%4.2f",
        ts -> (double) ts.stream().distinct().count() / (double) ts.size()
    );
  }

  public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>, Individual<? extends G, ? extends S, ? extends F>> best() {
    return NamedFunction.build(
        "best",
        "%s",
        e -> Misc.first(e.getOrderedPopulation().firsts())
    );
  }

  public static NamedFunction<List<? extends Number>, String> hist(int bins) {
    return NamedFunction.build(
        "hist",
        "%" + bins + "." + bins + "s",
        values -> TextPlotter.histogram(values, bins)
    );
  }

  public static <G> NamedFunction<Individual<? extends G, ?, ?>, G> genotype() {
    return NamedFunction.build("genotype", "%s", Individual::getGenotype);
  }

  public static <S> NamedFunction<Individual<?, ? extends S, ?>, S> solution() {
    return NamedFunction.build("solution", "%s", Individual::getSolution);
  }

  public static <F> NamedFunction<Individual<?, ?, ? extends F>, F> fitness() {
    return NamedFunction.build("fitness", "%s", Individual::getFitness);
  }

  public static NamedFunction<Individual<?, ?, ?>, Integer> birthIteration() {
    return NamedFunction.build("birth.iteration", "%4d", Individual::getBirthIteration);
  }

  public static NamedFunction<Object, Number> size() {
    return NamedFunction.build("size", "%3d", IndividualBasicInfo::size);
  }

  public static <G, S, F, T> NamedFunction<Event<? extends G, ? extends S, ? extends F>, T> constant(String name, String format, T value) {
    return NamedFunction.build(
        name,
        format,
        e -> value
    );
  }

  public static <G, S, F, T> NamedFunction<Event<? extends G, ? extends S, ? extends F>, T> constant(String name, T value) {
    int l = value.toString().length();
    return constant(name, "%" + l + "." + l + "s", value);
  }

  public static <G, S, F, T> NamedFunction<Event<? extends G, ? extends S, ? extends F>, T> constant(String name, String format, Map<String, T> values) {
    return NamedFunction.build(
        name,
        format,
        e -> values.get(name)
    );
  }

  @SuppressWarnings("unchecked")
  public static <T>  NamedFunction<Object, T> as(Class<T> clazz) {
    return NamedFunction.build(
        "as["+clazz.getSimpleName()+"]",
        "%s",
        o -> (T)o
    );
  }

  public static void main(String[] args) {
    Random random = new Random(1);
    List<List<Double>> genotypes = new FixedLengthListFactory<>(2, new UniformDoubleFactory(0, 1)).build(100, random);
    List<Individual<List<Double>, String, Double>> individuals = genotypes.stream()
        .map(g -> new Individual<>(
            g,
            String.format("%3.1f;%3.1f", g.get(0), g.get(1)),
            g.get(0) + g.get(1),
            0
        ))
        .collect(java.util.stream.Collectors.toList());
    PartiallyOrderedCollection<Individual<List<Double>, String, Double>> poc = new DAGPartiallyOrderedCollection<>(
        PartialComparator.from(Double.class).comparing(Individual::getFitness)
    );
    individuals.forEach(poc::add);
    Event<List<Double>, String, Double> event = new Event<>(new Evolver.State(1, individuals.size(), individuals.size(), 1050), poc);

    Listener<Object, Object, Double> l = listener(List.of(
        iterations(),
        elapsedSeconds(),
        size().of(all()),
        size().of(firsts()),
        size().of(solution()).of(best()),
        f((Function<Individual<?, ?, ?>, Integer>) Individual::getBirthIteration).of(best()),
        fitness().reformat("%4.2f").of(one()).of(firsts()),
        birthIteration().of(one()).of(lasts()),
        uniqueness().of(map(solution())).of(all()),
        uniqueness().of(map(fitness())).of(all()),
        uniqueness().of(map(genotype())).of(all()),
        hist(8).of(map(fitness())).of(all())
    ));
    l.listen(event);

  }

  private static <G, S, F> Listener<G, S, F> listener(List<NamedFunction<Event<? extends G, ? extends S, ? extends F>, ?>> funs) {
    return e -> funs.forEach(collector -> System.out.printf(
        "%30.30s: " + collector.getFormat() + "%n",
        collector.getName(),
        collector.apply(e)
    ));
  }

}
