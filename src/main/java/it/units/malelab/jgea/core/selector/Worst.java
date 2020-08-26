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

package it.units.malelab.jgea.core.selector;

import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.util.Misc;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class Worst implements Selector<Object> {

  @Override
  public <K> K select(PartiallyOrderedCollection<K> ks, Random random) {
    return Misc.pickRandomly(ks.lasts(), random);
  }

  @Override
  public String toString() {
    return "Worst{" + '}';
  }

}
