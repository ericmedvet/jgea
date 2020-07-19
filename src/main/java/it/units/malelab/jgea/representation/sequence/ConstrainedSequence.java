package it.units.malelab.jgea.representation.sequence;

import java.util.Set;

/**
 * @author eric
 */
public interface ConstrainedSequence<T> extends Sequence<T> {

  Set<T> domain(int index);

}
