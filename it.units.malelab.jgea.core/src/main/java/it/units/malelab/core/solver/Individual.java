package it.units.malelab.core.solver;

import java.io.Serializable;

public record Individual<G, S, F>(
    G genotype, S solution, F fitness, long fitnessMappingIteration, long genotypeBirthIteration
) implements Serializable {}
