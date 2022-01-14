package it.units.malelab.jgea.core.solver;

public interface BaseState {

  long getElapsedMillis();

  long getNOfBirths();

  long getNOfFitnessEvaluations();

  long getNOfIterations();
}
