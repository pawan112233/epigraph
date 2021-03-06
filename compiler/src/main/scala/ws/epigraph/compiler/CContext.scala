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

/* Created by yegor on 6/9/16. */

package ws.epigraph.compiler

import java.util.concurrent.{ConcurrentHashMap, ConcurrentLinkedQueue}

import ws.epigraph.lang.Qn
import ws.epigraph.schema.ResourcesSchema


class CContext(val tabWidth: Int = 2) {

  private var _phase: CPhase = CPhase.PARSE

  /** Accumulated compile messages */
  val errors: ConcurrentLinkedQueue[CMessage] = new java.util.concurrent.ConcurrentLinkedQueue

  /** Schema files being processed by compiler */
  val schemaFiles: ConcurrentHashMap[String, CSchemaFile] = new java.util.concurrent.ConcurrentHashMap

  /** Accumulated namespaces */
  val namespaces: ConcurrentHashMap[String, CNamespace] = new java.util.concurrent.ConcurrentHashMap

  // TODO: split typeDefs into data type defs and var type defs?
  val typeDefs: ConcurrentHashMap[CTypeName, CTypeDef] = new java.util.concurrent.ConcurrentHashMap

  //val anonListTypes: ConcurrentHashMap[CAnonListTypeName, CAnonListType] = new java.util.concurrent.ConcurrentHashMap
  val anonListTypes: ConcurrentHashMap[CDataType, CAnonListType] = new java.util.concurrent.ConcurrentHashMap

  //val anonMapTypes: ConcurrentHashMap[CAnonMapTypeName, CAnonMapType] = new java.util.concurrent.ConcurrentHashMap
  val anonMapTypes: ConcurrentHashMap[(CTypeRef, CDataType), CAnonMapType] = new java.util.concurrent.ConcurrentHashMap

  val resourcesSchemas: ConcurrentHashMap[CSchemaFile, ResourcesSchema] = new java.util.concurrent.ConcurrentHashMap

  /** Types implicitly imported (unless superseeded by explicit import statement) by every schema file */
  val implicitImports: Map[String, Qn] = Seq(
    "epigraph.String",
    "epigraph.Integer",
    "epigraph.Long",
    "epigraph.Double",
    "epigraph.Boolean"
  ).map(Qn.fromDotSeparated).map { fqn => (fqn.last, fqn) }.toMap

  def phase: CPhase = _phase

  def phase(next: CPhase): this.type = {
    assert(phase.ordinal == next.ordinal || next.ordinal == phase.ordinal + 1, phase.name)
    _phase = next
    this
  }

  def after[A](prevPhase: CPhase, default: A, f: => A): A = if (phase.ordinal <= prevPhase.ordinal) default else f

  def getOrCreateAnonListOf(elementDataType: CDataType): CAnonListType =
    anonListTypes.computeIfAbsent(elementDataType, AnonListTypeConstructor)

  private val AnonListTypeConstructor = JavaFunction[CDataType, CAnonListType](new CAnonListType(_)(this))

  def getOrCreateAnonMapOf(keyTypeRef: CTypeRef, valueDataType: CDataType): CAnonMapType =
    anonMapTypes.computeIfAbsent((keyTypeRef, valueDataType), AnonMapTypeConstructor)

  private val AnonMapTypeConstructor =
    JavaFunction[(CTypeRef, CDataType), CAnonMapType] { case (kt, vt) => new CAnonMapType(kt, vt)(this) }

}

case class CMessage(filename: String, position: CMessagePosition, message: String, level: CMessageLevel) {

  override def toString: String = toStringBuilder(new java.lang.StringBuilder).toString

  def toStringBuilder(sb: java.lang.StringBuilder): java.lang.StringBuilder = {
    if (filename != null)
      sb.append(filename)
    if (position != CMessagePosition.NA) {
      sb.append(':').append(position.line).append(':').append(position.column)
    }
    sb.append('\n').append(level.name).append(": ").append(message)
    position.lineText.foreach { text =>
      sb.append('\n').append(text)
      sb.append('\n').append(" " * (position.column - 1)).append("^" * position.tokenLength)
    }
    sb
  }

}

object CMessage {
  def error(filename: String, position: CMessagePosition, message: String) =
    new CMessage(filename, position, message, CMessageLevel.Error)

  def warning(filename: String, position: CMessagePosition, message: String) =
    new CMessage(filename, position, message, CMessageLevel.Warning)
}

case class CMessagePosition(line: Int, column: Int, tokenLength: Int, lineText: Option[String])

sealed trait CMessageLevel {

  def name: String

}

object CMessageLevel {

  object Warning extends CMessageLevel {

    override val name: String = "Warning"

  }

  object Error extends CMessageLevel {

    override val name: String = "Error"

  }

}

object CMessagePosition {

  val NA: CMessagePosition = CMessagePosition(0, 0, 0, None)

  def apply(line: Int, column: Int, lineText: Option[String]): CMessagePosition = CMessagePosition(
    line,
    column,
    1,
    lineText
  )

}
