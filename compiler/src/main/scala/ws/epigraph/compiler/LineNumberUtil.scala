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

package ws.epigraph.compiler

import com.intellij.psi.PsiElement
import org.jetbrains.annotations.Nullable

import scala.collection.mutable

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
class LineNumberUtil(text: String, tabWidth: Int = 2) {

  private case class Line(number: Int, startOffset: Int, endOffset: Int, text: String)

  private val lines = mutable.MutableList[Line]()

  {
    var offset = 0
    var lineStartOffset = 0
    var line = new StringBuilder
    var lineNumber = 1
    while (offset < text.length) {
      val ch = text.charAt(offset)

      if (ch == '\n') {
        lines += Line(lineNumber, lineStartOffset, offset, line.toString)
        lineStartOffset = offset + 1
        line = new StringBuilder
        lineNumber += 1
      } else {
        line.append(ch)
      }

      offset += 1
    }

    if (line.nonEmpty)
      lines += Line(lineNumber, lineStartOffset, offset, line.toString)
  }

  def pos(@Nullable psi: PsiElement): CMessagePosition =
    if (psi eq null) CMessagePosition.NA else pos(psi.getTextRange.getStartOffset, psi.getTextRange.getLength)

  def pos(offset: Int): CMessagePosition = pos(offset, 1)

  def pos(offset: Int, len: Int): CMessagePosition = {
    lines.find(_.endOffset >= offset) match {
      case Some(line) => CMessagePosition(line.number, column(line, offset), len, Some(line.text))
      case None => CMessagePosition.NA
    }
  }

  @Deprecated
  def line(offset: Int): Int = {
    lines.find(_.endOffset >= offset) match {
      case None => -1
      case Some(line) => line.number
    }
  }

  @Deprecated
  def column(offset: Int): Int = {
    lines.find(_.endOffset >= offset) match {
      case None => 0
      case Some(line) => column(line, offset)
    }
  }

  def column(line: Line, offset: Int): Int = {
    val offsetInLine = offset - line.startOffset
    val linePrefix = expandTabs(line.text.substring(0, offsetInLine))
    // FIXME deal with (escape? remove?) other control characters here (or in constructor)?
    val numCrs = linePrefix.count(_ == '\r')
    1 + linePrefix.length - numCrs
  }

  def lineText(offset: Int, expandTabs: Boolean = true): Option[String] = {
    val text = lines.find(_.endOffset >= offset).map(_.text)
    if (expandTabs) text.map(this.expandTabs) else text
  }

  def expandTabs(text: String): String =
    if (!text.contains('\t')) text
    else {
      val line = new StringBuilder
      var columnNumber = 1
      var offset = 0

      while (offset < text.length) {
        val ch = text.charAt(offset)

        if (ch == '\t') {
          val nextTabStop: Int = (columnNumber / tabWidth) * tabWidth + tabWidth + 1
          val numSpaces = nextTabStop - columnNumber
          line.append(" " * numSpaces)
          columnNumber = nextTabStop
        } else {
          line.append(ch)
          columnNumber += 1
        }

        offset += 1
      }

      line.toString()
    }

}
