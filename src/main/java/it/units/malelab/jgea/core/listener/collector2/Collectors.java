package it.units.malelab.jgea.core.listener.collector2;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.listener.Event;
import it.units.malelab.jgea.core.listener.Listener;
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
import java.util.Random;
import java.util.function.Function;

/**
 * @author eric on 2021/01/02 for jgea
 */
public class Collectors {

  private Collectors() {
  }

  public static <G, S, F> Named<Event<? extends G, ? extends S, ? extends F>, Collection<? extends Individual<? extends G, ? extends S, ? extends F>>> firsts() {
    return Named.build(
        "firsts",
        "%s",
        e -> e.getOrderedPopulation().firsts()
    );
  }

  public static <G, S, F> Named<Event<? extends G, ? extends S, ? extends F>, Collection<? extends Individual<? extends G, ? extends S, ? extends F>>> all() {
    return Named.build(
        "all",
        "%s",
        e -> e.getOrderedPopulation().all()
    );
  }

  public static <T> Named<Collection<T>, T> one() {
    return Named.build(
        "one",
        "%s",
        Misc::first
    );
  }

  public static <F, T> Named<Collection<F>, List<T>> map(Named<F, T> mapper) {
    return Named.build(
        "map[" + mapper.getName() + "]",
        "%s",
        individuals -> individuals.stream().map(mapper).collect(java.util.stream.Collectors.toList())
    );
  }

  public static <G, S, F> Named<Event<? extends G, ? extends S, ? extends F>, Individual<? extends G, ? extends S, ? extends F>> best() {
    return Named.build(
        "best",
        "%s",
        e -> Misc.first(e.getOrderedPopulation().firsts())
    );
  }

  public static <F> Named<Individual<? extends Object, ? extends Object, F>, F> fitness(String format) {
    return Named.build("fitness", format, Individual::getFitness);
  }

  public static Named<?, Number> size() {
    return Named.build("size", "%3.0f", IndividualBasicInfo::size);
  }

  public static Named<List<? extends Number>, String> hist(int bins) {
    return Named.build(
        "hist",
        "%" + bins + "." + bins + "s",
        values -> TextPlotter.histogram(values, bins)
    );
  }

  @SuppressWarnings("unchecked")
  public static <C> Named<?, C> as(Class<C> clazz) {
    return Named.build(
        "hist",
        "%s",
        o -> (C) o
    );
  }

  public static <G, S, F> Named<? super Event<? extends G, ? extends S, ? extends F>, Event<? extends G, ? extends S, ? extends F>> on(Class<G> gClass, Class<S> sClass, Class<F> fClass) {
    return Named.build("event", "%s", Function.identity());
  }

  public static <G, S, F> Listener<G, S, F> listener2(List<Named<? super Event<? extends G, ? extends S, ? extends F>, ?>> collectors) {
    return (Event<? extends G, ? extends S, ? extends F> event) -> collectors
        .forEach(collector -> System.out.printf(
            "%20.20s: " + collector.getFormat() + "%n",
            collector.getName(),
            collector.apply(event)
        ));
  }


  private interface EventFunction<G, S, F, O> extends Function<Event<? extends G, ? extends S, ? extends F>, O> {
  }

  public static <G, S, F> Listener<G, S, F> listener3(List<EventFunction<? super G, ? super S, ? super F, ?>> functions) {
    return event -> functions.forEach(f -> System.out.println(f.apply(event)));
  }

  public static void main(String[] args) {
    Random random = new Random(1);
    List<List<Double>> genotypes = new FixedLengthListFactory<>(2, new UniformDoubleFactory(0, 1)).build(2, random);
    List<Individual<List<Double>, String, Double>> individuals = genotypes.stream()
        .map(g -> new Individual<>(g, String.format("%4.2f,%4.2f", g.get(0), g.get(1)), g.get(0) + g.get(1), 0))
        .collect(java.util.stream.Collectors.toList());
    PartiallyOrderedCollection<Individual<List<Double>, String, Double>> poc = new DAGPartiallyOrderedCollection<>(
        PartialComparator.from(Double.class).comparing(Individual::getFitness)
    );
    individuals.forEach(poc::add);
    Event<List<Double>, String, Double> event = new Event<>(new Evolver.State(1, individuals.size(), individuals.size(), 1050), poc);

    Named<Collection<Individual<?, ?, Object>>, List<Object>> map = map(fitness("%3.1f"));
    Named<List<? extends Number>, String> hist = hist(2);
    Named<? extends Individual<?, ?, ?>, Double> of = as(Double.class).of(fitness(""));

    Function<Student, List<Object>> funs = all(List.of(
        ((Function<Person, String>) Person::getName).andThen(String::toUpperCase),
        Person::getName,
        student -> Integer.toString(student.age),
        Student::getAge
    ));
    System.out.println(funs.apply(new Student("eric", 3)));

    Function<Event<?, ?, ? extends Double>, List<Object>> colls = all(List.of(
        new PopSize()
    ));
    colls.apply(event).forEach(System.out::println);
  }

  private static class PopSize implements Named<Event<Object,Object,Object>, Integer> {
    @Override
    public String getFormat() {
      return "%3d";
    }

    @Override
    public String getName() {
      return "popSize";
    }

    @Override
    public Integer apply(Event<Object, Object, Object> e) {
      return e.getOrderedPopulation().size();
    }
  }

  private static <G, S, F> Function<Event<G, S, F>, Collection<? extends Individual<G, S, F>>> fFirsts() {
    return e -> e.getOrderedPopulation().firsts();
  }

  static class Person {
    public final String name;

    public Person(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  static class Student extends Person {
    public final int age;

    public Student(String name, int age) {
      super(name);
      this.age = age;
    }

    public int getAge() {
      return age;
    }
  }


  static <A, O> Function<A, List<O>> all(List<Function<A, ? extends O>> functions) {
    return a -> functions.stream()
        .map(f -> f.apply(a))
        .collect(java.util.stream.Collectors.toList());
  }


}
