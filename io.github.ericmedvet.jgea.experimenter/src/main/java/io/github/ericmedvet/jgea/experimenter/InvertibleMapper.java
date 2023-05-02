/*
 * Copyright 2023 eric
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

package io.github.ericmedvet.jgea.experimenter;

import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2022/11/21 for 2d-robot-evolution
 */
public interface InvertibleMapper<T, R> extends Function<T, R> {
  T exampleInput();

  static <T, R> InvertibleMapper<T, R> from(Function<T, R> f, T t) {
    return new InvertibleMapper<>() {
      @Override
      public R apply(T t) {
        return f.apply(t);
      }

      @Override
      public T exampleInput() {
        return t;
      }
    };
  }
}
