/*-
 * ========================LICENSE_START=================================
 * jgea-problem
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
module io.github.ericmedvet.jgea.problem {
  exports io.github.ericmedvet.jgea.problem.booleanfunction;
  exports io.github.ericmedvet.jgea.problem.classification;
  exports io.github.ericmedvet.jgea.problem.extraction;
  exports io.github.ericmedvet.jgea.problem.extraction.string;
  exports io.github.ericmedvet.jgea.problem.image;
  exports io.github.ericmedvet.jgea.problem.mapper;
  exports io.github.ericmedvet.jgea.problem.regression;
  exports io.github.ericmedvet.jgea.problem.simulation;
  exports io.github.ericmedvet.jgea.problem.synthetic;
  exports io.github.ericmedvet.jgea.problem.regression.univariate;
  exports io.github.ericmedvet.jgea.problem.regression.multivariate;
  exports io.github.ericmedvet.jgea.problem.regression.univariate.synthetic;
  exports io.github.ericmedvet.jgea.problem.grid;
  exports io.github.ericmedvet.jgea.problem;
  exports io.github.ericmedvet.jgea.problem.synthetic.numerical;

  requires io.github.ericmedvet.jgea.core;
  requires io.github.ericmedvet.jnb.datastructure;
  requires io.github.ericmedvet.jsdynsym.core;
  requires io.github.ericmedvet.jsdynsym.control;
  requires io.github.ericmedvet.jviz.core;
  requires commons.math3;
  requires java.desktop;
  requires org.apache.commons.csv;
  requires java.logging;
}
