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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author eric
 */
@FunctionalInterface
public interface DataCollector<G, S, F> {

  List<Item> collect(Event<? extends G, ? extends S, ? extends F> event);

  static List<Item> fromBean(Object o, boolean recursive, Map<Class<?>, String> formats, Map<Class<?>, Function<Object, String>> transformers) {
    List<Item> items = new ArrayList<>();
    for (Method m : o.getClass().getDeclaredMethods()) {
      if (m.getParameterCount() > 0) {
        continue;
      }
      if ((m.getModifiers() & Modifier.PUBLIC) == 0) {
        continue;
      }
      if (!m.getName().startsWith("get") && !m.getName().startsWith("is")) {
        continue;
      }
      Class<?> returnType = m.getReturnType();
      Class<?> matchedFormatType = formats.keySet().stream().filter(returnType::isAssignableFrom).findAny().orElse(null);
      Class<?> matchedTransformerType = transformers.keySet().stream().filter(returnType::isAssignableFrom).findAny().orElse(null);
      String name = m.getName().substring(m.getName().startsWith("get") ? 3 : 2);
      name = name.substring(0, 1).toLowerCase() + name.substring(1);
      name = transform(name);
      try {
        Object field = m.invoke(o);
        if (matchedFormatType != null) {
          items.add(new Item(
              name,
              matchedTransformerType != null ? transformers.get(matchedTransformerType).apply(m.invoke(o)) : m.invoke(o),
              formats.get(matchedFormatType)
          ));
        } else if (recursive) {
          if (field != null) {
            String finalName = name;
            items.addAll(fromBean(field, recursive, formats, transformers).stream()
                .map(i -> new Item(finalName + "." + i.getName(), i.getValue(), i.getFormat()))
                .collect(Collectors.toList())
            );
          }
        }
      } catch (IllegalAccessException | InvocationTargetException e) {
        // ignore
        Logger.getLogger(DataCollector.class.getName()).log(Level.WARNING, String.format("Cannot extract items from bean due to %s", e), e);
      }
    }
    return items;
  }

  private static String transform(String name) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (Character.isUpperCase(c)) {
        sb.append(".");
        sb.append(Character.toLowerCase(c));
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

}
