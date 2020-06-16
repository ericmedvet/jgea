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

package it.units.malelab.jgea.core.evolver.stopcondition;

import it.units.malelab.jgea.core.listener.Event;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * @author eric
 */
public class ElapsedTime implements Predicate<Event<Object, Object, Object>> {

  private final double t;
  private final TimeUnit timeUnit;

  public ElapsedTime(double t, TimeUnit timeUnit) {
    this.t = t;
    this.timeUnit = timeUnit;
  }

  public double getT() {
    return t;
  }

  public TimeUnit getTimeUnit() {
    return timeUnit;
  }

  @Override
  public boolean test(Event<Object, Object, Object> event) {
    long tMillis = TimeUnit.MILLISECONDS.convert((long) t, timeUnit);
    return event.getElapsedMillis() > tMillis;
  }

}
