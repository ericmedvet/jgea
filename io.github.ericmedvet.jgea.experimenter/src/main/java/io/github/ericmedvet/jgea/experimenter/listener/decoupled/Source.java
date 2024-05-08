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
package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import io.github.ericmedvet.jnb.datastructure.Pair;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public interface Source<K, V> {

  Map<Pair<LocalDateTime, K>, V> pull(LocalDateTime t);

  default void close() {}

}
