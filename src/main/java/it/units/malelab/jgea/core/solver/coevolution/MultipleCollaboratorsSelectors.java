package it.units.malelab.jgea.core.solver.coevolution;

public class MultipleCollaboratorsSelectors {

  private MultipleCollaboratorsSelectors() {
  }

  public static <K> CollaboratorSelector<K> complete() {
    return (ks, random) -> ks.all();
  }

  public static <K> CollaboratorSelector<K> firsts() {
    return (ks, random) -> ks.firsts();
  }

  public static <K> CollaboratorSelector<K> lasts() {
    return (ks, random) -> ks.lasts();
  }

}
