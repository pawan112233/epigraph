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

package ws.epigraph.java.service

import java.nio.file.Path

import ws.epigraph.compiler.CompilerException
import ws.epigraph.java.JavaGenUtils.up
import ws.epigraph.java.{GenContext, JavaGenUtils}
import ws.epigraph.java.NewlineStringInterpolator.{NewlineHelper, i, sp}
import ws.epigraph.java.service.ServiceObjectGen.gen
import ws.epigraph.lang.Qn
import ws.epigraph.schema.ResourceDeclaration
import ws.epigraph.schema.operations._
import ws.epigraph.types.DataTypeApi

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
class ResourceDeclarationGen(rd: ResourceDeclaration) extends ServiceObjectGen[ResourceDeclaration](rd) {

  val serviceClassName: String = up(rd.fieldName()) + "ResourceDeclaration"

  override protected def generateObject(ctx: ServiceGenContext): String = {
    val fieldType: DataTypeApi = rd.fieldType()


    import scala.collection.JavaConversions._
    val operationFieldNames: List[String] = rd.operations().map{ od: OperationDeclaration =>

      val operationFieldName = ResourceDeclarationGen.fieldName(od)
      val operationConstructorName = ResourceDeclarationGen.operationConstructorName(od)

      ctx.addField(s"public static final ${od.getClass.getSimpleName} $operationFieldName = $operationConstructorName();")
      ctx.addMethod(

        /*@formatter:off*/sn"""\
private static ${od.getClass.getSimpleName} $operationConstructorName() {
  return ${sp(2, gen(od, ctx))};
}"""/*@formatter:on*/
      )

      operationFieldName
    }.toList

    // see JavaTypeGen.dataTypeExpr, typeExpression
    /*@formatter:off*/sn"""\
super(
  "${rd.fieldName()}",
  ${ServiceGenUtils.genDataTypeExpr(rd.fieldType(), ctx.gctx)},
  ${i(ServiceGenUtils.genList(operationFieldNames, ctx))},
  ${gen(rd.location(), ctx)}
)"""/*@formatter:on*/

  }

  def generateFile(namespace: String, gctx: GenContext): String = {
    val sgctx = new ServiceGenContext(gctx)
    val superCall = generate(sgctx)

    /*@formatter:off*/sn"""\
package $namespace;

${ServiceGenUtils.genImports(sgctx)}
final class $serviceClassName extends ResourceDeclaration {
  public static final $serviceClassName INSTANCE = new $serviceClassName();

  ${i(ServiceGenUtils.genFields(sgctx))}

  private $serviceClassName() {
    ${i(superCall)};
  }

  ${i(ServiceGenUtils.genMethods(sgctx))}
}
"""/*@formatter:on*/

  }

  def writeUnder(sourcesRoot: Path, namespace: Qn, gctx: GenContext): Unit = {
    val relativePath = JavaGenUtils.fqnToPath(namespace).resolve(serviceClassName + ".java")
    val contents = generateFile(namespace.toString, gctx)
    JavaGenUtils.writeFile(sourcesRoot, relativePath, contents)
  }
}

object ResourceDeclarationGen {
  private val operationKinds: Map[OperationKind, String] = Map(
    OperationKind.CREATE -> "create",
    OperationKind.READ -> "read",
    OperationKind.UPDATE -> "update",
    OperationKind.DELETE -> "delete",
    OperationKind.CUSTOM -> "custom"
  )

  def fieldName(od: OperationDeclaration): String = {
    val kind = operationKinds.getOrElse(od.kind(), {throw new CompilerException})

    if (od.name() != null)
      s"${od.name()}${up(kind)}OperationDeclaration"
    else
      kind + "OperationDeclaration"
  }

  def operationConstructorName(od: OperationDeclaration): String = {
    val kind = up(operationKinds.getOrElse(od.kind(), {throw new CompilerException}))

    if (od.name() != null)
      s"construct${up(od.name())}${kind}OperationDeclaration"
    else
      s"construct${kind}OperationDeclaration"
  }

}