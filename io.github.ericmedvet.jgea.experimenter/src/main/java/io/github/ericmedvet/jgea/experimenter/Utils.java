/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.jgea.experimenter;

import io.github.ericmedvet.jnb.core.Interpolator;
import io.github.ericmedvet.jnb.core.MapNamedParamMap;
import io.github.ericmedvet.jnb.core.ParamMap;
import java.util.SortedMap;

public class Utils {

  private Utils() {}

  public static String interpolate(String format, Run<?, ?, ?, ?> run) {
    ParamMap map = run.map();
    if (run.map() instanceof MapNamedParamMap mnpm) {
      SortedMap<MapNamedParamMap.TypedKey, Object> values = mnpm.getValues();
      values.put(new MapNamedParamMap.TypedKey("index", ParamMap.Type.INT), run.index());
      map = new MapNamedParamMap(mnpm.getName(), mnpm.getValues());
    }
    return Interpolator.interpolate(format, map, "_");
  }
}
