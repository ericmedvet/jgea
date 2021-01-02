package it.units.malelab.jgea.core.listener.collector2;

/**
 * @author eric on 2021/01/02 for jgea
 */
public abstract class AbstractCollector<G, S, F, O> implements Collector<G, S, F, O> {
  private final String name;
  private final String defaultFormat;

  public AbstractCollector(String name, String defaultFormat) {
    this.name = name;
    this.defaultFormat = defaultFormat;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDefaultFormat() {
    return defaultFormat;
  }
}
