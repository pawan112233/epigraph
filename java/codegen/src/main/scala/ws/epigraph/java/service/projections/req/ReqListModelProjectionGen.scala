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

package ws.epigraph.java.service.projections.req

import ws.epigraph.java.service.assemblers.ListAsmGen
import ws.epigraph.java.{GenContext, JavaGen}
import ws.epigraph.lang.Qn
import ws.epigraph.projections.op.output.OpOutputListModelProjection

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
class ReqListModelProjectionGen(
  baseNamespaceProvider: BaseNamespaceProvider,
  val op: OpOutputListModelProjection,
  baseNamespaceOpt: Option[Qn],
  _namespaceSuffix: Qn,
  override val parentClassGenOpt: Option[ReqModelProjectionGen],
  ctx: GenContext)
  extends ReqModelProjectionGen(
    baseNamespaceProvider,
    op,
    baseNamespaceOpt,
    _namespaceSuffix,
    parentClassGenOpt,
    ctx
  ) with AbstractReqListModelProjectionGen {

  override type OpProjectionType = OpOutputListModelProjection

  val elementGen: ReqTypeProjectionGen = ReqEntityProjectionGen.dataProjectionGen(
      baseNamespaceProvider,
      op.itemsProjection(),
      Some(baseNamespace),
      namespaceSuffix.append(elementsNamespaceSuffix),
      parentClassGenOpt match {
        case Some(lmpg: ReqListModelProjectionGen) => Some(lmpg.elementGen)
        case _ => None
      },
      ctx
    )

  override protected def tailGenerator(
    parentGen: ReqModelProjectionGen,
    op: OpOutputListModelProjection,
    normalized: Boolean) =
    new ReqListModelProjectionGen(
      baseNamespaceProvider,
      op,
      Some(baseNamespace),
      tailNamespaceSuffix(op.`type`(), normalized),
      Some(parentGen),
      ctx
    )
//    {
//      override protected val buildTails: Boolean = !normalized
//      override protected val buildNormalizedTails: Boolean = normalized
//    }

  override def children: Iterable[JavaGen] = super.children ++ Iterable(new ListAsmGen(this, ctx))

  override protected def generate: String = generate(
    Qn.fromDotSeparated("ws.epigraph.projections.req.ReqListModelProjection"),
    flagged
  )

}
