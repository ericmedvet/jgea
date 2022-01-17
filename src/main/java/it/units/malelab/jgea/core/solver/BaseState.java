package it.units.malelab.jgea.core.solver;

public interface BaseState extends IterativeSolver.State {

  long getNOfBirths();

  long getNOfFitnessEvaluations();
}
