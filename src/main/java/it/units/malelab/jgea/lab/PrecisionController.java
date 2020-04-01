/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.lab;

import com.google.common.collect.EvictingQueue;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.Pair;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 * @author eric
 */
//NOTE: this class is not thread safe, having a state
public abstract class PrecisionController<S> implements Function<S, Double> {
  
  private final int historySize;
  private final EvictingQueue<Pair<S, Double>> history;
  
  private double sumOfPrecisions;
  private double calls;

  public PrecisionController(int historySize) {
    this.historySize = historySize;
    history = EvictingQueue.create(historySize);
    sumOfPrecisions = 0d;
    calls = 0d;
  }  
  
  @Override
  public synchronized Double apply(S solution, Listener listener) throws FunctionException {
    double precision = apply(solution, history, listener);
    precision = Math.max(0d, Math.min(precision, 1d));
    history.add(Pair.build(solution, precision));
    sumOfPrecisions = sumOfPrecisions+precision;
    calls = calls+1;
    return precision;
  }
  
  public abstract Double apply(S solution, Collection<Pair<S, Double>> history, Listener listener) throws FunctionException;

  public int getHistorySize() {
    return historySize;
  }

  public synchronized Collection<Pair<S, Double>> getHistory() {
    return new ArrayList<>(history);
  }  

  public double getSumOfPrecisions() {
    return sumOfPrecisions;
  }

  public double getCalls() {
    return calls;
  }
  
}
