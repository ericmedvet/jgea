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
      if (matchedFormatType != null) {
        try {
          items.add(new Item(
              name,
              matchedTransformerType != null ? transformers.get(matchedTransformerType).apply(m.invoke(o)) : m.invoke(o),
              formats.get(matchedFormatType)
          ));
        } catch (IllegalAccessException | InvocationTargetException e) {
          // ignore
        }
      } else if (recursive) {
        try {
          Object field = m.invoke(o);
          if (field != null) {
            String finalName = name;
            items.addAll(fromBean(field, recursive, formats, transformers).stream()
                .map(i -> new Item(finalName + "." + i.getName(), i.getValue(), i.getFormat()))
                .collect(Collectors.toList())
            );
          }
        } catch (IllegalAccessException | InvocationTargetException e) {
          // ignore
        }
      }
    }
    return items;
  }

}
