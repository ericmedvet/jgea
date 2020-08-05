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

package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;

import java.util.*;
import java.util.function.Function;

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
