/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
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

import io.github.ericmedvet.jnb.core.MapNamedParamMap;
import io.github.ericmedvet.jnb.core.NamedParamMap;
import io.github.ericmedvet.jnb.core.ParamMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
  private static final Logger L = Logger.getLogger(Utils.class.getName());

  private static final String FORMAT_REGEX = "%#?\\d*(\\.\\d+)?[sdf]";
  private static final String MAP_KEYS_REGEX = "[A-Za-z][A-Za-z0-9_]*";
  private static final Pattern INTERPOLATOR = Pattern.compile("\\{(?<mapKeys>"
      + MAP_KEYS_REGEX
      + "(\\."
      + MAP_KEYS_REGEX
      + ")*)"
      + "(:(?<format>"
      + FORMAT_REGEX
      + "))?\\}");

  private Utils() {}

  public static Object getKeyFromParamMap(ParamMap paramMap, List<String> keyPieces) {
    if (keyPieces.size() == 1) {
      return paramMap.value(keyPieces.get(0));
    }
    NamedParamMap namedParamMap = (NamedParamMap) paramMap.value(keyPieces.get(0), ParamMap.Type.NAMED_PARAM_MAP);
    if (namedParamMap == null) {
      return null;
    }
    return getKeyFromParamMap(namedParamMap, keyPieces.subList(1, keyPieces.size()));
  }

  public static String interpolate(String format, ParamMap map) {
    Matcher m = INTERPOLATOR.matcher(format);
    StringBuilder sb = new StringBuilder();
    int c = 0;
    while (m.find(c)) {
      sb.append(format, c, m.start());
      try {
        String mapKeys = m.group("mapKeys");
        String f = m.group("format") != null ? m.group("format") : "%s";
        Object v = getKeyFromParamMap(
            map, Arrays.stream(mapKeys.split("\\.")).toList());
        sb.append(f.formatted(v));
      } catch (RuntimeException e) {
        L.warning("Cannot interpolate name: %s".formatted(e));
        sb.append("I_ERR");
      }
      c = m.end();
    }
    sb.append(format, c, format.length());
    return sb.toString();
  }

  public static String interpolate(String format, Run<?, ?, ?, ?> run) {
    ParamMap map = run.map();
    if (run.map() instanceof MapNamedParamMap mnpm) {
      SortedMap<MapNamedParamMap.TypedKey, Object> values = mnpm.getValues();
      values.put(new MapNamedParamMap.TypedKey("index", ParamMap.Type.INT), run.index());
      map = new MapNamedParamMap(mnpm.getName(), mnpm.getValues());
    }
    return interpolate(format, map);
  }

  public static List<String> interpolationKeys(String format) {
    List<String> keys = new ArrayList<>();
    Matcher m = INTERPOLATOR.matcher(format);
    int c = 0;
    while (m.find(c)) {
      keys.add(m.group("mapKeys"));
      c = m.end();
    }
    return keys;
  }
}
