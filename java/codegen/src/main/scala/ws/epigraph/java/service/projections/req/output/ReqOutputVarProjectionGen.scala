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

import ws.epigraph.java.GenContext
import ws.epigraph.java.JavaGenNames.jn
import ws.epigraph.java.service.projections.req.output.ReqOutputProjectionGen.{classNamePrefix, classNameSuffix}
import ws.epigraph.java.service.projections.req.{OperationInfo, ReqProjectionGen, ReqVarProjectionGen}
import ws.epigraph.lang.Qn
import ws.epigraph.projections.op.output._
import ws.epigraph.types.TypeKind

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
class ReqOutputVarProjectionGen(
  protected val operationInfo: OperationInfo,
  protected val op: OpOutputVarProjection,
  protected val namespaceSuffix: Qn,
  protected val ctx: GenContext) extends ReqOutputProjectionGen with ReqVarProjectionGen {

  override type OpProjectionType = OpOutputVarProjection
  override type OpTagProjectionEntryType = OpOutputTagProjectionEntry

  override val shortClassName: String = genShortClassName(classNamePrefix, classNameSuffix)

  override protected def generatedProjections: java.util.Set[Qn] = ctx.reqOutputProjections

  override protected def tailGenerator(op: OpOutputVarProjection, normalized: Boolean) =
    new ReqOutputVarProjectionGen(
      operationInfo,
      op,
      namespaceSuffix.append(
        ReqVarProjectionGen.typeNameToPackageName(cType, namespace.toString) + ReqVarProjectionGen.tailPackageSuffix(
          normalized)
      ),
      ctx
    ) {
      override protected lazy val normalizedTailGenerators: Map[OpOutputVarProjection, ReqProjectionGen] = Map()
    }

  override protected def tagGenerator(tpe: OpOutputTagProjectionEntry): ReqProjectionGen =
    ReqOutputModelProjectionGen.dataProjectionGen(
      operationInfo,
      tpe.projection(),
      namespaceSuffix.append(jn(tpe.tag().name()).toLowerCase),
      ctx
    )

  override protected def generate: String = generate(
    Qn.fromDotSeparated("ws.epigraph.projections.req.output.ReqOutputVarProjection"),
    Qn.fromDotSeparated("ws.epigraph.projections.req.output.ReqOutputTagProjectionEntry")
  )
}

object ReqOutputVarProjectionGen {
  def dataProjectionGen(
    operationInfo: OperationInfo,
    op: OpOutputVarProjection,
    namespaceSuffix: Qn,
    ctx: GenContext): ReqOutputProjectionGen = op.`type`().kind() match {

    case TypeKind.UNION =>
      new ReqOutputVarProjectionGen(operationInfo, op, namespaceSuffix, ctx)
    case TypeKind.RECORD =>
      new ReqOutputRecordModelProjectionGen(
        operationInfo,
        op.singleTagProjection().projection().asInstanceOf[OpOutputRecordModelProjection],
        namespaceSuffix,
        ctx
      )
    case TypeKind.MAP =>
      new ReqOutputMapModelProjectionGen(
        operationInfo,
        op.singleTagProjection().projection().asInstanceOf[OpOutputMapModelProjection],
        namespaceSuffix,
        ctx
      )
    case TypeKind.LIST =>
      new ReqOutputListModelProjectionGen(
        operationInfo,
        op.singleTagProjection().projection().asInstanceOf[OpOutputListModelProjection],
        namespaceSuffix,
        ctx
      )
    case TypeKind.PRIMITIVE =>
      new ReqOutputPrimitiveModelProjectionGen(
        operationInfo,
        op.singleTagProjection().projection().asInstanceOf[OpOutputPrimitiveModelProjection],
        namespaceSuffix,
        ctx
      )
    case x => throw new RuntimeException(s"Unknown projection kind: $x")

  }
}
