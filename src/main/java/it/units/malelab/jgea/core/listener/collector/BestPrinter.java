/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.function.BiFunction;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.Misc;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author eric
 */
public class BestPrinter<G, S, F> extends FirstRankIndividualInfo<G, S, F> {

  public static enum Part {
    GENOTYPE, SOLUTION
  };

  public BestPrinter(final Function<S, String> printer, final String format) {
    super(
            "best",
            (Collection<Individual<G, S, F>> individuals, Listener listener) -> Misc.first(individuals),
            (Individual<G, S, F> individual) -> Collections.singletonList(new Item<>(
                    "individual", 
                    printer.apply(individual.getSolution()), 
                    format)
            )
    );
  }

  public BestPrinter(final String format, Part part) {
    super(
            "best",
            (Collection<Individual<G, S, F>> individuals, Listener listener) -> Misc.first(individuals),
            (Individual<G, S, F> individual) -> Collections.singletonList(new Item<>(
                    part.name().toLowerCase(),
                    part.equals(Part.GENOTYPE) ? individual.getGenotype().toString() : individual.getSolution().toString(),
                    format
            ))
    );
  }

  public BestPrinter(Part part) {
    this("%s", part);
  }

  public BestPrinter() {
    this(Part.SOLUTION);
  }

}
