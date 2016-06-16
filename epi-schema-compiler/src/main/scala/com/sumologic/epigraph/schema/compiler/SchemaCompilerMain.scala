/* Created by yegor on 6/8/16. */

package com.sumologic.epigraph.schema.compiler

import java.io.{File, IOException}

import com.intellij.lang.ParserDefinition
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiFile
import com.sumologic.epigraph.schema.parser.SchemaParserDefinition
import com.sumologic.epigraph.schema.parser.psi.SchemaFile
import org.intellij.grammar.LightPsi
import org.jetbrains.annotations.Nullable

import scala.collection.GenTraversableOnce
import scala.collection.JavaConversions._


object SchemaCompilerMain {

  val paths: Seq[String] = Seq(
    "epi-builtin-types/src/main/schema/epigraph/builtinTypes.es",
    "epi-schema/src/main/schema/epigraph/schema/names.es",
    "epi-schema/src/main/schema/epigraph/schema/types.es",
    "epi-schema/src/main/scala/epigraph/schema/Documented.es",
    "epi-schema-compiler/src/test/schema/example/compilerExamples.es"
  )

  val spd: SchemaParserDefinition = new SchemaParserDefinition

  implicit val ctx: CContext = new CContext

  //      import pprint.Config.Colors._
  implicit private val PPConfig = pprint.Config(
    width = 120, colors = pprint.Colors(fansi.Color.Green, fansi.Color.LightBlue)
  )

  def main(args: Array[String]) {

    val schemaFiles: Seq[SchemaFile] = parseSourceFiles(paths.map(new File(_)))
    handleErrors()

    val cSchemaFiles: Seq[CSchemaFile] = schemaFiles.map(new CSchemaFile(_))
    printSchemaFiles(cSchemaFiles)

    registerDefinedTypes(cSchemaFiles)
    //pprint.pprintln(ctx.types.keys.toSeq)

    resolveTypeRefs(cSchemaFiles)
    pprint.pprintln(ctx.types.toMap)
    handleErrors()

    applySupplementingTypeDefs()
    applySupplements(cSchemaFiles) // FIXME track injecting `supplement`s
    printSchemaFiles(cSchemaFiles)

  }

  def parseSourceFiles(files: Seq[File]): Seq[SchemaFile] = {

    val schemaFiles: Seq[SchemaFile] = files.par.flatMap { file =>
      parseFile(file, spd) match {
        case sf: SchemaFile =>
          //println(DebugUtil.psiToString(sf, true, true).trim())
          Seq(sf)
        case _ =>
          ctx.errors.add(new CError(file.getCanonicalPath, CErrorPosition.NA, "Couldn't parse"))
          Nil
      }
    }.seq

    schemaFiles.foreach { sf => ctx.errors.addAll(ParseErrorsDumper.collectParseErrors(sf)) }

    schemaFiles
  }

  def registerDefinedTypes(cSchemaFiles: Seq[CSchemaFile]): Unit = {
    cSchemaFiles foreach { csf =>
      csf.types foreach { ct =>
        val old: CTypeDef = ctx.types.putIfAbsent(ct.name, ct).asInstanceOf[CTypeDef] //FIXME (make types smart object?)
        if (old != null) ctx.errors.add(
          new CError(
            csf.filename,
            csf.lnu.pos(ct.name.psi),
            s"Type '${ct.name.name}' already defined at '${old.csf.location(old.psi.getQid)}'"
          )
        )
      }
    }
  }

  def resolveTypeRefs(cSchemaFiles: Seq[CSchemaFile]): Unit = {
    cSchemaFiles.par foreach { csf =>
      csf.typerefs foreach { ctr =>
        ctr.name match { // TODO clean-up

          case fqn: CTypeFqn =>
            @Nullable val refType = ctx.types.get(ctr.name)
            if (refType == null) {
              ctx.errors.add(
                new CError(
                  csf.filename,
                  csf.lnu.pos(ctr.psi),
                  s"Not found: type '${ctr.name.name}'"
                )
              )
            } else {
              ctr.resolveTo(refType)
            }

          case altn: CAnonListTypeName =>
            val alt = ctx.types.computeIfAbsent(
              altn, new java.util.function.Function[CTypeName, CAnonListType] {
                override def apply(t: CTypeName): CAnonListType = {
                  t match {
                    case altn: CAnonListTypeName => new CAnonListType(altn)
                    case _ => throw new RuntimeException // TODO
                  }
                }
              }
            )
            ctr.resolveTo(alt)

          case amtn: CAnonMapTypeName =>
            val amt = ctx.types.computeIfAbsent(
              amtn, new java.util.function.Function[CTypeName, CAnonMapType] {
                override def apply(t: CTypeName): CAnonMapType = {
                  t match {
                    case amtn: CAnonMapTypeName => new CAnonMapType(amtn)
                    case _ => throw new RuntimeException // TODO
                  }
                }
              }
            )
            ctr.resolveTo(amt)

        }
      }
    }
  }

  def applySupplementingTypeDefs(): Unit = {
    ctx.types.elements foreach {
      case ctd: CTypeDef =>
        ctd.declaredSupplementees foreach { ctr =>
          ctr.resolved match {
            case typeDef: CTypeDef => typeDef.injectedSupertypes.add(ctd) // TODO capture injector source?
            case _ => throw new RuntimeException // TODO exception
          }
        }
      case _ => // ignore anon lists/maps
    }
  }

  def applySupplements(cSchemaFiles: Seq[CSchemaFile]): Unit = {
    cSchemaFiles foreach { csf =>
      csf.supplements foreach { cs =>
        val sourceTypeDef = cs.source.resolved match {
          case ctd: CTypeDef => ctd
          case _ => ???
        }
        cs.targets.map(_.resolved) foreach {
          case typeDef: CTypeDef => typeDef.injectedSupertypes.add(sourceTypeDef)
          case _ => ???
        }
      }
    }
  }

  def handleErrors(): Unit = { // FIXME it should not exit but return error code
    if (ctx.errors.nonEmpty) {
      renderErrors(ctx)
      System.exit(1)
    }
  }

  def renderErrors(ctx: CContext): Unit = {
    ctx.errors foreach { cerr =>
      pprint.pprintln(cerr)
    }
  }

  def printSchemaFiles(schemaFiles: GenTraversableOnce[CSchemaFile]): Unit = {
    schemaFiles foreach { csf =>
      print(csf.filename + ": ")
      pprint.pprintln(csf.types)
    }
  }

  @throws[IOException]
  def parseFile(file: File, parserDefinition: ParserDefinition): PsiFile = {
    val name: String = file.getCanonicalPath
    val text: String = FileUtil.loadFile(file)
    LightPsi.parseFile(name, text, parserDefinition)
  }

}
