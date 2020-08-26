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
