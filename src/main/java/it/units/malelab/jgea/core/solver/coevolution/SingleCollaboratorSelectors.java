package it.units.malelab.jgea.core.solver.coevolution;

import it.units.malelab.jgea.core.selector.First;
import it.units.malelab.jgea.core.selector.Last;
import it.units.malelab.jgea.core.selector.Random;
import it.units.malelab.jgea.core.selector.Tournament;

import java.util.Set;

public class SingleCollaboratorSelectors {

  private SingleCollaboratorSelectors() {
  }

  public static <K> CollaboratorSelector<K> best() {
    return (ks, random) -> Set.of(new First().select(ks, random));
  }

  public static <K> CollaboratorSelector<K> worst() {
    return (ks, random) -> Set.of(new Last().select(ks, random));
  }

  public static <K> CollaboratorSelector<K> random() {
    return (ks, random) -> Set.of(new Random().select(ks, random));
  }

  public static <K> CollaboratorSelector<K> tournament(int size) {
    return (ks, random) -> Set.of(new Tournament(size).select(ks, random));
  }

}
