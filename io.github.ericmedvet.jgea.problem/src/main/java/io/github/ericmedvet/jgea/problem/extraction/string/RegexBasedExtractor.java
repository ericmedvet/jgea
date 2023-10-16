
package io.github.ericmedvet.jgea.problem.extraction.string;

import com.google.common.collect.Range;
import io.github.ericmedvet.jgea.core.representation.graph.finiteautomata.Extractor;
import io.github.ericmedvet.jgea.core.util.Sized;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
public class RegexBasedExtractor implements Extractor<Character>, Sized {

  private final String regex;

  public RegexBasedExtractor(String regex) {
    this.regex = regex;
  }

  @Override
  public Set<Range<Integer>> extract(List<Character> sequence) {
    String string = sequence.stream().map(String::valueOf).collect(Collectors.joining());
    if (Pattern.compile(regex).matcher("").matches()) {
      return Set.of();
    }
    Matcher matcher = Pattern.compile(regex).matcher(string);
    Set<Range<Integer>> extractions = new LinkedHashSet<>();
    int s = 0;
    while (matcher.find(s)) {
      Range<Integer> extraction = Range.openClosed(matcher.start(), matcher.end());
      s = extraction.upperEndpoint();
      extractions.add(extraction);
    }
    return extractions;
  }

  @Override
  public boolean match(List<Character> sequence) {
    String string = sequence.stream().map(String::valueOf).collect(Collectors.joining());
    Matcher matcher = Pattern.compile(regex).matcher(string);
    return matcher.matches();
  }

  @Override
  public Set<Range<Integer>> extractNonOverlapping(List<Character> sequence) {
    return extract(sequence);
  }

  @Override
  public int hashCode() {
    return Objects.hash(regex);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    RegexBasedExtractor that = (RegexBasedExtractor) o;
    return regex.equals(that.regex);
  }

  @Override
  public String toString() {
    return regex;
  }

  @Override
  public int size() {
    return regex.length();
  }

}