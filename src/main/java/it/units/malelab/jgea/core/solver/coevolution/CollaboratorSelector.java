package it.units.malelab.jgea.core.solver.coevolution;

import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.selector.First;
import it.units.malelab.jgea.core.selector.Last;
import it.units.malelab.jgea.core.selector.Random;
import it.units.malelab.jgea.core.selector.Tournament;

import java.util.Collection;
import java.util.Set;
import java.util.random.RandomGenerator;

@FunctionalInterface
public interface CollaboratorSelector<K> {
  Collection<K> select(PartiallyOrderedCollection<K> ks, RandomGenerator random);

  static <K> CollaboratorSelector<K> build(String collaboratorSelector) {
    switch (collaboratorSelector) {
      case "b":
        return CollaboratorSelector.best();
      case "w":
        return CollaboratorSelector.worst();
      case "r":
        return CollaboratorSelector.random();
      case "c":
        return CollaboratorSelector.complete();
      case "f":
        return CollaboratorSelector.firsts();
      case "l":
        return CollaboratorSelector.lasts();
      default:
        if (collaboratorSelector.matches("t\\d")) {
          return CollaboratorSelector.tournament(Integer.parseInt(String.valueOf(collaboratorSelector.charAt(1))));
        }
    }
    throw new IllegalArgumentException("Illegal collaborator selection specified");
  }

  static <K> CollaboratorSelector<K> best() {
    return (ks, random) -> Set.of(new First().select(ks, random));
  }

  static <K> CollaboratorSelector<K> worst() {
    return (ks, random) -> Set.of(new Last().select(ks, random));
  }

  static <K> CollaboratorSelector<K> random() {
    return (ks, random) -> Set.of(new Random().select(ks, random));
  }

  static <K> CollaboratorSelector<K> tournament(int size) {
    return (ks, random) -> Set.of(new Tournament(size).select(ks, random));
  }

  static <K> CollaboratorSelector<K> complete() {
    return (ks, random) -> ks.all();
  }

  static <K> CollaboratorSelector<K> firsts() {
    return (ks, random) -> ks.firsts();
  }

  static <K> CollaboratorSelector<K> lasts() {
    return (ks, random) -> ks.lasts();
  }

}
