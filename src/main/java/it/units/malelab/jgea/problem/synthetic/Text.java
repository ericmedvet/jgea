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

package it.units.malelab.jgea.problem.synthetic;

import it.units.malelab.jgea.representation.tree.Tree;
import it.units.malelab.jgea.representation.sequence.Sequence;
import it.units.malelab.jgea.distance.Distance;
import it.units.malelab.jgea.distance.Edit;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class Text implements GrammarBasedProblem<String, String, Double> {

  private static class FitnessFunction implements Function<String, Double> {

    private final Sequence<Character> target;
    private final Distance<Sequence<Character>> distance;

    public FitnessFunction(String targetString) {
      this.target = Sequence.from(targetString.chars().mapToObj(c -> (char) c).toArray(Character[]::new));
      this.distance = new Edit<>();
    }

    @Override
    public Double apply(String string) {
      double d = (double) distance.apply(
          target,
          Sequence.from(string.chars().mapToObj(c -> (char) c).toArray(Character[]::new))
      ) / (double) target.size();
      return d;
    }

  }

  private final Grammar<String> grammar;
  private final Function<Tree<String>, String> solutionMapper;
  private final Function<String, Double> fitnessFunction;

  public Text(String targetString) throws IOException {
    grammar = Grammar.fromFile(new File("grammars/text.bnf"));
    solutionMapper = (Tree<String> tree) -> tree.leaves().stream()
        .map(Tree::content)
        .collect(Collectors.joining()).replace("_", " ");
    fitnessFunction = new FitnessFunction(targetString);
  }

  @Override
  public Grammar<String> getGrammar() {
    return grammar;
  }

  @Override
  public Function<Tree<String>, String> getSolutionMapper() {
    return solutionMapper;
  }

  @Override
  public Function<String, Double> getFitnessFunction() {
    return fitnessFunction;
  }

}
