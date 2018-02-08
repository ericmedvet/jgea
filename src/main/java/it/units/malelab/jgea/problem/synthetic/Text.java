/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.synthetic;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.mapper.BoundMapper;
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.grammarbased.GrammarBasedProblem;

/**
 *
 * @author eric
 */
public class Text extends GrammarBasedProblem<String, String, Integer> {

  public Text(Grammar<String> grammar, BoundMapper<Node<String>, Integer> fitnessMapper) {
    super(grammar, fitnessMapper);
  }
  
}
