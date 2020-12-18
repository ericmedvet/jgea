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

package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.listener.collector.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author federico
 */
public class PrintStreamPopulationListener<G, S, F> implements Listener<G, S, F> {
    private final PrintStreamListener<G, S, F> decoratedListener;
    private final List<Function<Individual<? extends G, ? extends S, ? extends F>, List<Item>>> individualCollectors;

    @SafeVarargs
    public PrintStreamPopulationListener(PrintStreamListener<G, S, F> listener,
                                         Function<Individual<? extends G, ? extends S, ? extends F>, List<Item>>... individualCollectors) {
        this.decoratedListener = listener;
        this.individualCollectors = List.of(individualCollectors);
    }

    @Override
    public void listen(Event<? extends G, ? extends S, ? extends F> event) {
        //collect items at population level
        List<List<Item>> commonGroups = PrintStreamListener.collectItems(event, this.decoratedListener.getCollectors());
        List<? extends Individual<? extends G, ? extends S, ? extends F>> individuals = new ArrayList<>(event.getOrderedPopulation().all());
        //collect items at individual level
        List<List<Item>> itemGroups = new ArrayList<>();
        for (int i=0; i < individuals.size(); ++i) {
            itemGroups.addAll(commonGroups);
            int finalI = i;
            itemGroups.addAll(this.individualCollectors.stream().map(c -> c.apply(individuals.get(finalI))).collect(Collectors.toList()));
            //delegate printing to decorated listener
            this.decoratedListener.print(itemGroups, event);
            itemGroups.clear();
        }
    }

}
