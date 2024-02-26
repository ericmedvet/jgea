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
module io.github.ericmedvet.jgea.experimenter {
  opens io.github.ericmedvet.jgea.experimenter.builders to
      io.github.ericmedvet.jnb.core;
  opens io.github.ericmedvet.jgea.experimenter.listener.net to
      jcommander;

  exports io.github.ericmedvet.jgea.experimenter;
  exports io.github.ericmedvet.jgea.experimenter.listener;
  exports io.github.ericmedvet.jgea.experimenter.listener.plot.accumulator;

  requires jcodec;
  requires io.github.ericmedvet.jnb.core;
  requires io.github.ericmedvet.jnb.datastructure;
  requires io.github.ericmedvet.jgea.core;
  requires io.github.ericmedvet.jgea.problem;
  requires io.github.ericmedvet.jsdynsym.core;
  requires io.github.ericmedvet.jsdynsym.buildable;
  requires io.github.ericmedvet.jviz.core;
  requires java.desktop;
  requires java.telegram.bot.api;
  requires java.logging;
  requires jdk.management;
  requires com.googlecode.lanterna;
  requires jcommander;
  requires org.apache.commons.csv;
}
