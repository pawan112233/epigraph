/*
 * Copyright 2017 Sumo Logic
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

package ws.epigraph.java.service.projections.req.input

import java.util

import ws.epigraph.java.service.projections.req.{ReqProjectionGen, ReqTypeProjectionGen}
import ws.epigraph.lang.Qn
import ws.epigraph.projections.gen.ProjectionReferenceName

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
trait ReqInputProjectionGen extends ReqProjectionGen {
  override protected def baseNamespace: Qn = super.baseNamespace.append("input")

  protected def generatedProjections: util.Map[ProjectionReferenceName, ReqTypeProjectionGen] = ctx.reqInputProjections
}

object ReqInputProjectionGen {
  val classNamePrefix: String = ReqProjectionGen.classNamePrefix + "Input"
  val classNameSuffix: String = ReqProjectionGen.classNameSuffix
}
