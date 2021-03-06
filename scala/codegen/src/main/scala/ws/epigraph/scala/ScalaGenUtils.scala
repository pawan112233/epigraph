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

/* Created by yegor on 7/12/16. */

package ws.epigraph.scala

import java.io.{BufferedWriter, OutputStream, OutputStreamWriter}
import java.nio.charset.{Charset, CharsetEncoder, StandardCharsets}
import java.nio.file.{Files, Path, Paths, StandardOpenOption}

import ws.epigraph.lang.Qn

import scala.collection.JavaConversions._

object ScalaGenUtils {

  val EmptyPath: Path = Paths.get("")

  def fqnToPath(fqn: Qn): Path = if (fqn.isEmpty) EmptyPath else Paths.get(fqn.first, fqn.segments.tail: _*)

  def writeFile(
      root: Path,
      relativeFilePath: Path,
      content: String
  ): Unit = {
    val fullFilePath = root.resolve(relativeFilePath)
    //println(s"Writing to $fullFilePath")
    Files.createDirectories(fullFilePath.getParent)
    writeFile(fullFilePath, content)
  }

  private def writeFile(filePath: Path, content: String): Unit = {
    val encoder: CharsetEncoder = StandardCharsets.UTF_8.newEncoder // TODO figure out what needs to be closed
    val out: OutputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW)
    try {
      val writer: BufferedWriter = new BufferedWriter(new OutputStreamWriter(out, encoder))
      try writer.append(content) finally writer.close()
    } finally {
      out.close()
    }
  }

  def rmrf(path: Path, top: Path): Path = {
    //println(s"Removing $path")
    if (Files.exists(path)) {
      checkBounds(path, top)
      if (Files.isDirectory(path)) {
        val stream = Files.newDirectoryStream(path)
        try stream.foreach(rmrf(_, top)) finally stream.close()
      }
      Files.delete/*IfExists*/ (path)
    }
    path
  }

  def checkBounds(path: Path, top: Path): Unit = {
    val rpath = path.toRealPath()
    val rtop = top.toRealPath()
    if (!(rpath.startsWith(rtop) && rpath.getNameCount > rtop.getNameCount))
      throw new IllegalArgumentException(s"out of bounds! $path, $top")
  }

  def move(source: Path, target: Path, top: Path): Unit = {
    if (Files.exists(target)) {
      val tmp = rmrf(target.resolveSibling(target.getFileName.toString + "~old"), top)
      Files.move(target, tmp)
      Files.move(source, target)
      rmrf(tmp, top)
    } else {
      Files.move(source, target)
    }
  }

}
