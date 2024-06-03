package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.experimenter.listener.plot.AggregatedXYDataSeriesMRPAF;
import io.github.ericmedvet.jgea.experimenter.listener.plot.DistributionMRPAF;
import io.github.ericmedvet.jnb.core.Alias;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@Discoverable(prefixTemplate = "ea.plot.multi|m")
public class MultiPlots {
  private MultiPlots() {
  }

  @SuppressWarnings("unused")
  @Alias(name = "xyExp", value = """
        xy(
          xSubplot = ea.f.runString(name = none; s = "_");
          ySubplot = ea.f.runString(name = problem; s = "{problem.name}");
          line = ea.f.runString(name = solver; s = "{solver.name}");
          x = f.quantized(of = ea.f.nOfEvals(); q = 500)
        )
      """)
  @Alias(name = "quality", value = """
        xyExp(y = ea.f.quality(of = ea.f.best()))
      """)
  @Alias(name = "uniqueness", value = """
        xyExp(y = f.uniqueness(of = f.each(mapF = ea.f.genotype(); of = ea.f.all())))
      """)
  public static <E, R> AggregatedXYDataSeriesMRPAF<E, R, String> xy(
      @Param("xSubplot") Function<? super R, String> xSubplotFunction,
      @Param("ySubplot") Function<? super R, String> ySubplotFunction,
      @Param("line") Function<? super R, String> lineFunction,
      @Param("x") Function<? super E, ? extends Number> xFunction,
      @Param("y") Function<? super E, ? extends Number> yFunction,
      @Param(value = "valueAggregator", dNPM = "f.median()")
      Function<List<Number>, Number> valueAggregator,
      @Param(value = "minAggregator", dNPM = "f.percentile(p=25)")
      Function<List<Number>, Number> minAggregator,
      @Param(value = "maxAggregator", dNPM = "f.percentile(p=75)")
      Function<List<Number>, Number> maxAggregator,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange
  ) {
    return new AggregatedXYDataSeriesMRPAF<>(
        xSubplotFunction,
        ySubplotFunction,
        lineFunction,
        xFunction,
        yFunction,
        valueAggregator,
        minAggregator,
        maxAggregator,
        xRange,
        yRange
    );
  }

  @SuppressWarnings("unused")
  @Alias(name = "yBoxplotExp", value = """
        yBoxplot(
          xSubplot = ea.f.runString(name = none; s = "_");
          ySubplot = ea.f.runString(name = problem; s = "{problem.name}");
          box = ea.f.runString(name = solver; s = "{solver.name}");
          predicateValue = ea.f.rate(of = ea.f.progress());
          condition = predicate.gtEq(t = 1)
        )
      """)
  @Alias(name = "qualityBoxplot", value = """
        yBoxplotExp(y = ea.f.quality(of = ea.f.best()))
      """)
  @Alias(name = "uniquenessBoxplot", value = """
        yBoxplotExp(y = f.uniqueness(of = f.each(mapF = ea.f.genotype(); of = ea.f.all())))
      """)
  public static <E, R, X> DistributionMRPAF<E, R, String, X> yBoxplot(
      @Param("xSubplot") Function<? super R, String> xSubplotFunction,
      @Param("ySubplot") Function<? super R, String> ySubplotFunction,
      @Param("box") Function<? super R, String> boxFunction,
      @Param("y") Function<? super E, ? extends Number> yFunction,
      @Param("predicateValue") Function<E, X> predicateValueFunction,
      @Param(value = "condition", dNPM = "predicate.gtEq(t=1)") Predicate<X> condition,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange
  ) {
    return new DistributionMRPAF<>(
        xSubplotFunction,
        ySubplotFunction,
        boxFunction,
        yFunction,
        predicateValueFunction,
        condition,
        yRange
    );
  }

}