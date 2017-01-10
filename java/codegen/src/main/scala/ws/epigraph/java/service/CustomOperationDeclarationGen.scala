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

import ws.epigraph.java.NewlineStringInterpolator.{NewlineHelper, i}
import ws.epigraph.java.service.ServiceObjectGen.gen
import ws.epigraph.schema.operations.{CustomOperationDeclaration, HttpMethod}

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
class CustomOperationDeclarationGen(od: CustomOperationDeclaration)
  extends ServiceObjectGen[CustomOperationDeclaration](od) {

  override protected def generateObject(ctx: ServiceGenContext): String =
  /*@formatter:off*/sn"""\
new CustomOperationDeclaration(
  ${generateHttpMethod(od.method(), ctx)},
  ${gen(od.name(), ctx)},
  ${i(gen(od.annotations(), ctx))},
  null, /* todo OpFieldPath */
  null, /* todo OpInputFieldProjection */
  null, /* todo OpOutputFieldProjection */
  ${gen(od.location(), ctx)}
)"""/*@formatter:on*/

  private def generateHttpMethod(m: HttpMethod, ctx: ServiceGenContext): String = {
    ctx.addImport(classOf[HttpMethod].getName)
    m match {
      case HttpMethod.GET => "HttpMethod.GET"
      case HttpMethod.POST => "HttpMethod.POST"
      case HttpMethod.PUT => "HttpMethod.PUT"
      case HttpMethod.DELETE => "HttpMethod.DELETE"
    }
  }
}