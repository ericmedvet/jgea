/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.problem.extraction.string;

import com.google.common.collect.Range;
import it.units.malelab.jgea.core.util.Sized;
import it.units.malelab.jgea.problem.extraction.Extractor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author eric
 * @created 2020/08/17
 * @project jgea
 */
public class RegexBasedExtractor implements Extractor<Character>, Sized {

  private final String regex;

  public RegexBasedExtractor(String regex) {
    this.regex = regex;
  }

  @Override
  public Set<Range<Integer>> extract(List<Character> sequence) {
    String string = sequence.stream()
        .map(String::valueOf)
        .collect(Collectors.joining());
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
  public Set<Range<Integer>> extractNonOverlapping(List<Character> sequence) {
    return extract(sequence);
  }

  @Override
  public boolean match(List<Character> sequence) {
    String string = sequence.stream()
        .map(String::valueOf)
        .collect(Collectors.joining());
    Matcher matcher = Pattern.compile(regex).matcher(string);
    return matcher.matches();
  }

  @Override
  public int size() {
    return regex.length();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RegexBasedExtractor that = (RegexBasedExtractor) o;
    return regex.equals(that.regex);
  }

  @Override
  public int hashCode() {
    return Objects.hash(regex);
  }

  @Override
  public String toString() {
    return regex;
  }

}