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

package ws.epigraph.java.service.projections.req.output

import ws.epigraph.compiler.CField
import ws.epigraph.java.GenContext
import ws.epigraph.java.JavaGenNames.jn
import ws.epigraph.java.service.projections.req._
import ws.epigraph.lang.Qn
import ws.epigraph.projections.op.output.OpOutputRecordModelProjection

import scala.collection.JavaConversions._

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
class ReqOutputRecordModelProjectionGen(
  baseNamespaceProvider: BaseNamespaceProvider,
  protected val op: OpOutputRecordModelProjection,
  _baseNamespace: Qn,
  _namespaceSuffix: Qn,
  ctx: GenContext)
  extends ReqOutputModelProjectionGen(
    baseNamespaceProvider,
    op,
    _baseNamespace,
    _namespaceSuffix,
    ctx
  ) with ReqRecordModelProjectionGen {

  override type OpProjectionType = OpOutputRecordModelProjection

  override protected lazy val fieldGenerators: Map[CField, ReqOutputFieldProjectionGen] =
    op.fieldProjections().values().map { fpe =>
      (
        findField(fpe.field().name()),
        new ReqOutputFieldProjectionGen(
          baseNamespaceProvider,
          fpe.field().name(),
          fpe.fieldProjection(),
          Some(baseNamespace),
          namespaceSuffix.append(jn(fpe.field().name()).toLowerCase),
          ctx
        )
      )
    }.toMap

  override protected def tailGenerator(
    op: OpOutputRecordModelProjection,
    normalized: Boolean): ReqModelProjectionGen =
    new ReqOutputRecordModelProjectionGen(
      baseNamespaceProvider,
      op,
      baseNamespace,
      tailNamespaceSuffix(op.`type`(), normalized),
      ctx
    ) {
      override protected val buildTails: Boolean = !normalized
      override protected val buildNormalizedTails: Boolean = normalized
    }

  override protected def generate: String = generate(
    Qn.fromDotSeparated("ws.epigraph.projections.req.output.ReqOutputRecordModelProjection"),
    Qn.fromDotSeparated("ws.epigraph.projections.req.output.ReqOutputFieldProjectionEntry"),
    required
  )
}
