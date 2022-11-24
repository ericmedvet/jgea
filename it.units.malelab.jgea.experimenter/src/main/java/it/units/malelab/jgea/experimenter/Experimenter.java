package it.units.malelab.jgea.experimenter;

import it.units.malelab.jnb.core.NamedBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author "Eric Medvet" on 2022/11/24 for jgea
 */
public class Experimenter {
  private final NamedBuilder<?> namedBuilder;
  private final ExecutorService runExecutorService;
  private final ExecutorService listenerExecutorService;

  public Experimenter(
      NamedBuilder<?> namedBuilder,
      ExecutorService runExecutorService,
      ExecutorService listenerExecutorService
  ) {
    this.namedBuilder = namedBuilder;
    this.runExecutorService = runExecutorService;
    this.listenerExecutorService = listenerExecutorService;
  }

  public Experimenter(NamedBuilder<?> namedBuilder, int nOfThreads) {
    this(namedBuilder, Executors.newFixedThreadPool(nOfThreads), Executors.newSingleThreadExecutor());
  }


}
