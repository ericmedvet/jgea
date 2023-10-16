
package io.github.ericmedvet.jgea.problem.classification;
public interface Classifier<O, L extends Enum<L>> {
  L classify(O o);
}
