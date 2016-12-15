/*
 * Copyright 2016 Sumo Logic
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

/* Created by yegor on 7/1/16. */

package ws.epigraph.edl.compiler

import java.util.regex.Pattern

import com.intellij.psi.PsiElement
import ws.epigraph.lang.Qn
import ws.epigraph.edl.parser.psi.{EdlQnTypeRef, EdlTypeDef}
import org.jetbrains.annotations.Nullable


abstract class CTypeName protected(val name: String)(implicit val ctx: CContext) {

  final def canEqual(other: Any): Boolean = other.isInstanceOf[CTypeName]

  final override def equals(other: Any): Boolean = other match {
    case that: CTypeName => that.canEqual(this) && name == that.name
    case _ => false
  }

  final override def hashCode: Int = name.hashCode

}


class CTypeFqn private(csf: CEdlFile, val fqn: Qn, val psi: PsiElement)(implicit ctx: CContext)
    extends CTypeName(fqn.toString) {

  val local: String = validate(fqn.last())

  @Nullable val namespace: String = if (fqn.size == 1) null else fqn.removeLastSegment().toString

  def this(csf: CEdlFile, parentNs: Qn, lqn: EdlQnTypeRef)(implicit ctx: CContext) = this(
    csf, parentNs.append(lqn.getQn.getQn), lqn: PsiElement
  )

  def this(csf: CEdlFile, parentNs: Qn, typeDef: EdlTypeDef)(implicit ctx: CContext) = this(
    csf, parentNs.append(typeDef.getQid.getCanonicalName), typeDef.getQid.getId: PsiElement
  )

  private def validate(local: String)(implicit ctx: CContext): String = {
    if (!CTypeFqn.LocalTypeNamePattern.matcher(local).matches) ctx.errors.add(
      CError(csf.filename, csf.position(psi), s"Invalid type name '$local'")
    )
    local
  }

}

object CTypeFqn {

  val LocalTypeNamePattern: Pattern = """\p{Upper}\p{Alnum}*""".r.pattern

}


class CAnonListTypeName(val elementDataType: CDataType)(implicit ctx: CContext) extends {

  val elementTypeRef: CTypeRef = elementDataType.typeRef

} with CTypeName("list[" + elementDataType.name + "]")


class CAnonMapTypeName(val keyTypeRef: CTypeRef, val valueDataType: CDataType)(implicit ctx: CContext) extends {

  val valueTypeRef: CTypeRef = valueDataType.typeRef

} with CTypeName("map[" + keyTypeRef.name.name + "," + valueDataType.name + "]")
