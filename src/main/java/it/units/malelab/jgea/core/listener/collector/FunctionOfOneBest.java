/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;

import java.util.List;
import java.util.function.Function;

/**
 * @author eric
 * @created 2020/06/17
 * @project jgea
 */
public class FunctionOfOneBest<G, S, F> extends Prefix<G, S, F> {

  public FunctionOfOneBest(Function<Individual<? extends G, ? extends S, ? extends F>, List<Item>> function) {
    super("best", new FunctionOfFirsts<>(firsts -> function.apply(firsts.iterator().next())));
  }
}
