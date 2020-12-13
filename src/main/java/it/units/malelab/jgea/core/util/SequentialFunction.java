package it.units.malelab.jgea.core.util;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * @author eric
 */
public class SequentialFunction<S, F> implements Function<S, F> {
  private final SortedMap<Long, Function<S, F>> functions;
  private long nOfInvocations;
  private long nextThreshold;
  private Function<S, F> currentFunction;

  public SequentialFunction(Map<Long, Function<S, F>> functions) {
    // should not be used with caching
    this.functions = new TreeMap<>(functions);
    reset();
  }

  public void reset() {
    nOfInvocations = 0;
    nextThreshold = functions.firstKey();
    currentFunction = functions.get(nextThreshold);
  }

  private void updateCurrentFunction() {
    if (nOfInvocations > nextThreshold) {
      if (functions.lastKey() <= nOfInvocations) {
        return;
      }
      nextThreshold = functions.tailMap(nOfInvocations).firstKey();
      currentFunction = functions.get(nextThreshold);
    }
  }

  @Override
  public F apply(S s) {
    nOfInvocations = nOfInvocations + 1;
    updateCurrentFunction();
    return currentFunction.apply(s);
  }

}
