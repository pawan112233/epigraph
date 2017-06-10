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

import ws.epigraph.java.GenContext
import ws.epigraph.java.JavaGenNames.jn
import ws.epigraph.java.service.projections.req.input.ReqInputProjectionGen.{classNamePrefix, classNameSuffix}
import ws.epigraph.java.service.projections.req.{BaseNamespaceProvider, ReqProjectionGen, ReqVarProjectionGen}
import ws.epigraph.lang.Qn
import ws.epigraph.projections.op.input._
import ws.epigraph.types.TypeKind

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
class ReqInputVarProjectionGen(
  protected val baseNamespaceProvider: BaseNamespaceProvider,
  protected val op: OpInputVarProjection,
  _baseNamespace: Qn,
  _namespaceSuffix: Qn,
  protected val ctx: GenContext) extends ReqInputProjectionGen with ReqVarProjectionGen {

  override type OpProjectionType = OpInputVarProjection
  override type OpTagProjectionEntryType = OpInputTagProjectionEntry

  override protected def baseNamespace: Qn = ReqProjectionGen.baseNamespace(referenceName, _baseNamespace)

  override protected def namespaceSuffix: Qn = ReqProjectionGen.namespaceSuffix(referenceName, _namespaceSuffix)

  override val shortClassName: String = genShortClassName(classNamePrefix, classNameSuffix)

  override protected def tailGenerator(op: OpInputVarProjection, normalized: Boolean) =
    new ReqInputVarProjectionGen(
      baseNamespaceProvider,
      op,
      baseNamespace,
      tailNamespaceSuffix(op.`type`(), normalized),
      ctx
    ) {
      override protected lazy val normalizedTailGenerators: Map[OpInputVarProjection, ReqProjectionGen] = Map()
    }

  override protected def tagGenerator(tpe: OpInputTagProjectionEntry): ReqProjectionGen =
    ReqInputModelProjectionGen.dataProjectionGen(
      baseNamespaceProvider,
      tpe.projection(),
      baseNamespace,
      namespaceSuffix.append(jn(tpe.tag().name()).toLowerCase),
      ctx
    )

  override protected def generate: String = generate(
    Qn.fromDotSeparated("ws.epigraph.projections.req.input.ReqInputVarProjection"),
    Qn.fromDotSeparated("ws.epigraph.projections.req.input.ReqInputTagProjectionEntry")
  )
}

object ReqInputVarProjectionGen {
  def dataProjectionGen(
    baseNamespaceProvider: BaseNamespaceProvider,
    op: OpInputVarProjection,
    baseNamespace: Qn,
    namespaceSuffix: Qn,
    ctx: GenContext): ReqInputProjectionGen = op.`type`().kind() match {

    case TypeKind.ENTITY =>
      new ReqInputVarProjectionGen(baseNamespaceProvider, op, baseNamespace, namespaceSuffix, ctx)
    case TypeKind.RECORD =>
      new ReqInputRecordModelProjectionGen(
        baseNamespaceProvider,
        op.singleTagProjection().projection().asInstanceOf[OpInputRecordModelProjection],
        baseNamespace,
        namespaceSuffix,
        ctx
      )
    case TypeKind.MAP =>
      new ReqInputMapModelProjectionGen(
        baseNamespaceProvider,
        op.singleTagProjection().projection().asInstanceOf[OpInputMapModelProjection],
        baseNamespace,
        namespaceSuffix,
        ctx
      )
    case TypeKind.LIST =>
      new ReqInputListModelProjectionGen(
        baseNamespaceProvider,
        op.singleTagProjection().projection().asInstanceOf[OpInputListModelProjection],
        baseNamespace,
        namespaceSuffix,
        ctx
      )
    case TypeKind.PRIMITIVE =>
      new ReqInputPrimitiveModelProjectionGen(
        baseNamespaceProvider,
        op.singleTagProjection().projection().asInstanceOf[OpInputPrimitiveModelProjection],
        baseNamespace,
        namespaceSuffix,
        ctx
      )
    case x => throw new RuntimeException(s"Unknown projection kind: $x")

  }
}
