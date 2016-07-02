/* Created by yegor on 7/1/16. */

package com.sumologic.epigraph.schema.compiler

import com.intellij.psi.PsiElement
import com.sumologic.epigraph.schema.parser.Fqn
import com.sumologic.epigraph.schema.parser.psi.{SchemaAnonList, SchemaAnonMap, SchemaFqnTypeRef, SchemaTypeDef}

import scala.collection.JavaConversions._


abstract class CTypeName protected(val csf: CSchemaFile, val name: String, val psi: PsiElement)
    (implicit val ctx: CContext) {

  def canEqual(other: Any): Boolean = other.isInstanceOf[CTypeName]

  override def equals(other: Any): Boolean = other match {
    case that: CTypeName => that.canEqual(this) && name == that.name
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(name)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

}


class CTypeFqn private(csf: CSchemaFile, fqn: Fqn, psi: PsiElement)(implicit ctx: CContext) extends CTypeName(
  csf, fqn.toString, psi
) {

  def this(csf: CSchemaFile, parentNs: Fqn, lqn: SchemaFqnTypeRef)(implicit ctx: CContext) = this(
    csf, parentNs.append(lqn.getFqn.getFqn), lqn: PsiElement
  )

  def this(csf: CSchemaFile, parentNs: Fqn, typeDef: SchemaTypeDef)(implicit ctx: CContext) = this(
    csf, parentNs.append(typeDef.getQid.getCanonicalName), typeDef.getQid.getId: PsiElement
  )

}


class CAnonListTypeName(csf: CSchemaFile, override val psi: SchemaAnonList)(implicit ctx: CContext) extends {

  val elementTypeRef: CTypeRef = CTypeRef(csf, psi.getTypeRef)

} with CTypeName(csf, "list[" + elementTypeRef.name.name + "]", psi)


class CAnonMapTypeName(csf: CSchemaFile, override val psi: SchemaAnonMap)(implicit ctx: CContext) extends {

  val keyTypeRef: CTypeRef = CTypeRef(csf, psi.getTypeRefList.head)

  val valueTypeRef: CTypeRef = CTypeRef(csf, psi.getTypeRefList.last)

} with CTypeName(csf, "map[" + keyTypeRef.name.name + "," + valueTypeRef.name.name + "]", psi)
