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
import ws.epigraph.java.JavaGenNames.ln
import ws.epigraph.java.NewlineStringInterpolator.NewlineHelper
import ws.epigraph.java.service.projections.req.output.ReqOutputProjectionGen.{classNamePrefix, classNameSuffix}
import ws.epigraph.java.service.projections.req._
import ws.epigraph.lang.Qn
import ws.epigraph.projections.op.output._
import ws.epigraph.types.{DatumTypeApi, TypeKind}

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
abstract class ReqOutputModelProjectionGen(
  protected val baseNamespaceProvider: BaseNamespaceProvider,
  op: OpOutputModelProjection[_, _, _ <: DatumTypeApi],
  baseNamespaceOpt: Option[Qn],
  _namespaceSuffix: Qn,
  override protected val parentClassGenOpt: Option[ReqProjectionGen],
  protected val ctx: GenContext) extends ReqOutputProjectionGen with ReqModelProjectionGen {

  override type OpProjectionType <: OpOutputModelProjection[_, _, _ <: DatumTypeApi]
  override type OpMetaProjectionType = OpOutputModelProjection[_, _, _ <: DatumTypeApi]

  override protected def baseNamespace: Qn = ReqProjectionGen.baseNamespace(
    referenceName,
    baseNamespaceOpt.getOrElse(super.baseNamespace)
  )

  override protected def namespaceSuffix: Qn = ReqProjectionGen.namespaceSuffix(referenceName, _namespaceSuffix)

  override val shortClassName: String = s"$classNamePrefix${ln(cType)}$classNameSuffix"

  override protected def reqVarProjectionFqn: Qn =
    Qn.fromDotSeparated("ws.epigraph.projections.req.output.ReqOutputVarProjection")

  override protected def reqModelProjectionFqn: Qn =
  Qn.fromDotSeparated("ws.epigraph.projections.req.output.ReqOutputModelProjection")

  override protected def reqModelProjectionParams: String = "<?, ?, ?>"

  protected lazy val required: CodeChunk = CodeChunk(/*@formatter:off*/sn"""\
  public boolean requried() { return raw.required(); }
"""/*@formatter:on*/)

  override protected def metaGenerator(metaOp: OpMetaProjectionType): ReqOutputModelProjectionGen =
    ReqOutputModelProjectionGen.dataProjectionGen(
      baseNamespaceProvider,
      metaOp,
      Some(baseNamespace),
      namespaceSuffix.append("meta"),
      None, // todo should extend parent projection's meta?
      ctx
    )
}

object ReqOutputModelProjectionGen {
  def dataProjectionGen(
    baseNamespaceProvider: BaseNamespaceProvider,
    op: OpOutputModelProjection[_, _, _ <: DatumTypeApi],
    baseNamespaceOpt: Option[Qn],
    namespaceSuffix: Qn,
    parentClassGenOpt: Option[ReqProjectionGen],
    ctx: GenContext): ReqOutputModelProjectionGen = op.`type`().kind() match {

    case TypeKind.RECORD =>
      new ReqOutputRecordModelProjectionGen(
        baseNamespaceProvider,
        op.asInstanceOf[OpOutputRecordModelProjection],
        baseNamespaceOpt,
        namespaceSuffix,
        parentClassGenOpt,
        ctx
      )
    case TypeKind.MAP =>
      new ReqOutputMapModelProjectionGen(
        baseNamespaceProvider,
        op.asInstanceOf[OpOutputMapModelProjection],
        baseNamespaceOpt,
        namespaceSuffix,
        parentClassGenOpt,
        ctx
      )
    case TypeKind.LIST =>
      new ReqOutputListModelProjectionGen(
        baseNamespaceProvider,
        op.asInstanceOf[OpOutputListModelProjection],
        baseNamespaceOpt,
        namespaceSuffix,
        parentClassGenOpt,
        ctx
      )
    case TypeKind.PRIMITIVE =>
      new ReqOutputPrimitiveModelProjectionGen(
        baseNamespaceProvider,
        op.asInstanceOf[OpOutputPrimitiveModelProjection],
        baseNamespaceOpt,
        namespaceSuffix,
        parentClassGenOpt,
        ctx
      )
    case x => throw new RuntimeException(s"Unsupported projection kind: $x")

  }
}
