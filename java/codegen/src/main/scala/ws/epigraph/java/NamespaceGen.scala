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

/* Created by yegor on 7/8/16. */

package ws.epigraph.java

import java.nio.file.Path

import org.jetbrains.annotations.Nullable
import ws.epigraph.compiler.CNamespace
import ws.epigraph.java.NewlineStringInterpolator.NewlineHelper

import scala.language.implicitConversions

class NamespaceGen(from: CNamespace, protected val ctx: GenContext) extends JavaGen {

  // TODO respect annotations changing namespace names for scala

  override def relativeFilePath: Path =
    JavaGenUtils.fqnToPath(from.fqn).resolve("package.scala")

  protected def generate: String = sn"""\
${JavaGenUtils.topLevelComment}
${if (from.parent ne null) s"\npackage ${JavaGenNames.javaFqn(from.fqn.removeLastSegment())}\n" else ""}
import ws.epigraph.names

/**
 * Package object for `${from.fqn}` namespace.
 * TODO: doc annotation here
 */
package object ${JavaGenNames.jn(from.local)} {

  val namespace: names.QualifiedNamespaceName = new names.QualifiedNamespaceName(
    ${nsOpt(from.parent)}, names.LocalNamespaceName("${from.local}")
  )

}
"""

  private def nsOpt(@Nullable ns: String): String = if (ns == null) "None" else s"Some($ns.namespace)"

}
