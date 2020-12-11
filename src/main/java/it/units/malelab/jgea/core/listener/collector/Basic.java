/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.listener.Event;

import java.util.Arrays;
import java.util.List;

/**
 * @author eric
 */
public class Basic implements DataCollector<Object, Object, Object> {

  @Override
  public List<Item> collect(Event<?, ?, ?> event) {
    return Arrays.asList(
        new Item("iterations", event.getState().getIterations(), "%3d"),
        new Item("births", event.getState().getBirths(), "%5d"),
        new Item("fitness.evaluations", event.getState().getFitnessEvaluations(), "%5d"),
        new Item("elapsed.sec", (double) event.getState().getElapsedMillis() / 1000d, "%6.1f")
    );
  }

}
