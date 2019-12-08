/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Sized;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author eric
 */
public class BestInfo<G, S, F> extends FirstRankIndividualInfo<G, S, F> {

  public BestInfo(Function<F, List<Item>> fitnessSplitter) {
    super(
            "best",
            (Collection<Individual<G, S, F>> individuals, Listener listener) -> Misc.first(individuals),
            new IndividualBasicInfo<>(fitnessSplitter)
    );
  }

  public BestInfo(Function<?, F> function, String... formats) {
    super(
            "best",
            (Collection<Individual<G, S, F>> individuals, Listener listener) -> Misc.first(individuals),
            new IndividualBasicInfo<>(function, formats)
    );
  }

  public BestInfo(String format) {
    super(
            "best",
            (Collection<Individual<G, S, F>> individuals, Listener listener) -> Misc.first(individuals),
            new IndividualBasicInfo<>(format)
    );
  }

}
