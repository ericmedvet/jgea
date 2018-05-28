/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea;

import it.units.malelab.jgea.distance.Edit;
import it.units.malelab.jgea.distance.Pairwise;
import it.units.malelab.jgea.distance.TreeLeaves;
import it.units.malelab.jgea.problem.booleanfunction.EvenParity;
import it.units.malelab.jgea.problem.mapper.EnhancedProblem;
import it.units.malelab.jgea.problem.symbolicregression.Pagie1;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class RepresentationEvolution extends Worker {

  public RepresentationEvolution(String[] args) throws FileNotFoundException {
    super(args);
  }    

  @Override
  public void run() {
    List<EnhancedProblem> problems = new ArrayList<>();    
    try {
      problems.add(new EnhancedProblem(new EvenParity(5), new Pairwise<>(new TreeLeaves<>(new Edit<>())), null));
      problems.add(new EnhancedProblem(new Pagie1(), new Pairwise<>(new TreeLeaves<>(new Edit<>())), null));
    } catch (IOException ex) {
      Logger.getLogger(RepresentationEvolution.class.getName()).log(Level.SEVERE, "Cannot instantiate problems", ex);
      System.exit(-1);
    }
  }
  
}
