
package io.github.ericmedvet.jgea.core.distance;

import java.util.List;
public class StringSequence implements Distance<String> {

  private final Distance<List<Character>> innerDistance;

  public StringSequence(Distance<List<Character>> innerDistance) {
    this.innerDistance = innerDistance;
  }

  @Override
  public Double apply(String string1, String string2) {
    return innerDistance.apply(
        string1.chars().mapToObj(c -> (char) c).toList(),
        string2.chars().mapToObj(c -> (char) c).toList()
    );
  }

}
