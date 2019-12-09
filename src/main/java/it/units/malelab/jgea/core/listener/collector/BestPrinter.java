/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.util.Misc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author eric
 */
public class BestPrinter<G, S, F> extends FirstRankIndividualInfo<G, S, F> {

  public BestPrinter(final Function<S, String> printer, final String format) {
    super(
            "best",
            (Collection<Individual<G, S, F>> individuals, Listener listener) -> Misc.first(individuals),
            (Individual<G, S, F> individual) -> Collections.singletonList(new Item<>("solution", printer.apply(individual.getSolution()), format))
    );
  }

  public BestPrinter(final String format) {
    super(
            "best",
            (Collection<Individual<G, S, F>> individuals, Listener listener) -> Misc.first(individuals),
            (Individual<G, S, F> individual) -> Collections.singletonList(new Item<>("solution", individual.getSolution().toString(), format))
    );
  }

  public BestPrinter() {
    super(
            "best",
            (Collection<Individual<G, S, F>> individuals, Listener listener) -> Misc.first(individuals),
            (Individual<G, S, F> individual) -> Collections.singletonList(new Item<>("solution", individual.getSolution().toString(), "%s"))
    );
  }

}
