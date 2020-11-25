package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.listener.collector.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * @author federico
 */
public class PrintStreamPopulationListener<G, S, F> implements Listener<G, S, F> {

    private final PrintStreamListener<G, S, F> decoratedListener;
    private final Stream<Function<Individual<? extends G, ? extends S, ? extends F>, List<Item>>> individualCollectors;

    @SafeVarargs
    public PrintStreamPopulationListener(PrintStreamListener<G, S, F> listener,
                                         Function<Individual<? extends G, ? extends S, ? extends F>, List<Item>>... individualCollectors) {
        decoratedListener = listener;
        this.individualCollectors = Stream.of(individualCollectors);
    }

    @Override
    public void listen(Event<? extends G, ? extends S, ? extends F> event) {
        //collect items at population level
        List<List<Item>> commonGroups = PrintStreamListener.collectItems(event, decoratedListener.getCollectors());
        //collect items at individual level
        List<List<Item>> itemGroups = new ArrayList<>();
        for (Individual<? extends G, ? extends S, ? extends F> individual : event.getOrderedPopulation().all()) {
            itemGroups.addAll(commonGroups);
            itemGroups.addAll(this.individualCollectors.map(c -> c.apply(individual)).collect(Collectors.toList()));
            //delegate printing to decorated listener
            decoratedListener.print(itemGroups, event);
            itemGroups.clear();
        }
    }

}
