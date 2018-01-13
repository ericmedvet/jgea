/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.event;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author eric
 */
public class TimedEvent implements Event {
  
  private final long time;
  private final TimeUnit timeUnit;
  private final Event event;

  public TimedEvent(long time, TimeUnit timeUnit, Event event) {
    this.time = time;
    this.timeUnit = timeUnit;
    this.event = event;
  }

  public long getTime() {
    return time;
  }

  public TimeUnit getTimeUnit() {
    return timeUnit;
  }

  public Event getEvent() {
    return event;
  }
  
}
