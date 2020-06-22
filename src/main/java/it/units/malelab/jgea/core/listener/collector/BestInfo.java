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


import java.util.ArrayList;
import java.util.List;

/**
 * @author eric
 */
public class BestInfo extends FunctionOfOneBest<Object, Object, Object> {

  public BestInfo(String... fitnessFormats) {
    super(new IndividualBasicInfo<>(f -> {
      List<Item> items = new ArrayList<>();
      if (f instanceof List<?>) {
        for (int i = 0; i < ((List<?>) f).size(); i++) {
          items.add(new Item(
              "objective." + i,
              ((List<?>) f).get(i),
              fitnessFormats.length > 0 ? fitnessFormats[i % fitnessFormats.length] : "%s"
          ));
        }
      } else {
        items.add(new Item(
            "value",
            f,
            fitnessFormats.length > 0 ? fitnessFormats[0] : "%s"
        ));
      }
      return items;
    }));
  }

}
