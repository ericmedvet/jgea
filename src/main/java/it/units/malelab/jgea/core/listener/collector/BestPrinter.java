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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author eric
 */
public class BestPrinter extends FunctionOfOneBest<Object, Object, Object> {

  public enum Part {
    GENOTYPE, SOLUTION
  }

  public BestPrinter(Map<Part, String> parts) {
    super(individual -> {
      List<Item> items = new ArrayList<>();
      if (parts.keySet().contains(Part.GENOTYPE)) {
        items.add(new Item(Part.GENOTYPE.toString().toLowerCase(), individual.getGenotype(), parts.get(Part.GENOTYPE)));
      }
      if (parts.keySet().contains(Part.SOLUTION)) {
        items.add(new Item(Part.SOLUTION.toString().toLowerCase(), individual.getSolution(), parts.get(Part.SOLUTION)));
      }
      return items;
    });
  }

  public BestPrinter(Part part, String format) {
    this(Map.of(part, format));
  }

  public BestPrinter(Part part) {
    this(part, "%s");
  }
}
