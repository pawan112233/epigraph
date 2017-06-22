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

import ws.epigraph.java.JavaGenNames.{lqbct, lqbrn, lqdrn2, lqn2}
import ws.epigraph.java.NewlineStringInterpolator.NewlineHelper
import ws.epigraph.java.service.projections.req.OperationInfoBaseNamespaceProvider
import ws.epigraph.java.service.projections.req.input.ReqInputFieldProjectionGen
import ws.epigraph.java.{GenContext, JavaGen, JavaGenUtils, ObjectGenContext}
import ws.epigraph.lang.Qn
import ws.epigraph.schema.ResourceDeclaration
import ws.epigraph.schema.operations.CustomOperationDeclaration

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
class AbstractCustomOperationGen(
  val baseNamespace: Qn,
  val rd: ResourceDeclaration,
  val op: CustomOperationDeclaration,
  val ctx: GenContext) extends AbstractOperationGen {

  protected val inputFieldProjectionGenOpt: Option[ReqInputFieldProjectionGen] =
    Option(op.inputProjection()).map { inputProjection =>
      new ReqInputFieldProjectionGen(
        new OperationInfoBaseNamespaceProvider(operationInfo),
        rd.fieldName(),
        inputProjection,
        None,
        Qn.EMPTY,
        ctx
      )
    }

  override def children: Iterable[JavaGen] = super.children ++ inputFieldProjectionGenOpt.toIterable

  override protected def generate: String = {
    val sctx = new ObjectGenContext(ctx)

    val outputType = JavaGenUtils.toCType(op.outputType())
    val nsString = namespace.toString
    val resultBuilderCtor = lqbct(outputType, nsString)

    sctx.addImport("org.jetbrains.annotations.Nullable")
    val shortDataType = sctx.addImport(lqdrn2(outputType, nsString), namespace)
    val shortBuilderType = sctx.addImport(lqbrn(outputType, nsString), namespace)

    inputFieldProjectionGenOpt match {
      case Some(inputFieldProjectionGen) =>

        sctx.addImport(inputFieldProjectionGen.fullClassName)

        val inputType = JavaGenUtils.toCType(op.inputType())
        val inputTypeClass = lqn2(inputType, nsString)

        pathProjectionGenOpt match {

          case Some(pathProjectionGen) =>
            sctx.addImport(pathProjectionGen.fullClassName)
            sctx.addMethod(/*@formatter:off*/sn"""\
@Override
public @NotNull CompletableFuture<ReadOperationResponse<$shortDataType>> process(@NotNull CustomOperationRequest request) {
  $shortBuilderType builder = $resultBuilderCtor;
  ${pathProjectionGen.shortClassName} path = new ${pathProjectionGen.shortClassName}(request.path());
  $inputTypeClass data = request.data() == null ? null : ${AbstractOperationGen.dataExpr(inputType, nsString, "request.data()")};
  ${inputFieldProjectionGen.shortClassName} inputProjection = request.inputProjection() == null ? null : new ${inputFieldProjectionGen.shortClassName}(request.inputProjection());
  ${outputFieldProjectionGen.shortClassName} outputProjection = new ${outputFieldProjectionGen.shortClassName}(request.outputProjection());
  return process(builder, data, path, inputProjection, outputProjection).thenApply(ReadOperationResponse::new);
}
"""/*@formatter:off*/
        )

        sctx.addMethod(/*@formatter:off*/sn"""\
/**
 * Process custom request
 *
 * @param resultBuilder result builder, initially empty
 * @param inputData input data, may be {@code null} if not specified
 * @param path request path
 * @param inputProjection request input projection, may be {@code null} if not specified
 * @param outputProjection request output projection
 *
 * @return future of the result
 */
protected abstract @NotNull CompletableFuture<$shortDataType> process(
  @NotNull $shortBuilderType resultBuilder,
  @Nullable $inputTypeClass inputData,
  @NotNull ${pathProjectionGen.shortClassName} path,
  @Nullable ${inputFieldProjectionGen.shortClassName} inputProjection,
  @NotNull ${outputFieldProjectionGen.shortClassName} outputProjection
);
"""/*@formatter:off*/
        )

      case None =>
        sctx.addMethod(/*@formatter:off*/sn"""\
@Override
public @NotNull CompletableFuture<ReadOperationResponse<$shortDataType>> process(@NotNull CustomOperationRequest request) {
  $shortBuilderType builder = $resultBuilderCtor;
  $inputTypeClass data = request.data() == null ? null : ${AbstractOperationGen.dataExpr(inputType, nsString, "request.data()")};
  ${inputFieldProjectionGen.shortClassName} inputProjection = request.inputProjection() == null ? null : new ${inputFieldProjectionGen.shortClassName}(request.inputProjection());
  ${outputFieldProjectionGen.shortClassName} outputProjection = new ${outputFieldProjectionGen.shortClassName}(request.outputProjection());
  return process(builder, data, inputProjection, outputProjection).thenApply(ReadOperationResponse::new);
}
"""/*@formatter:off*/
        )

        sctx.addMethod(/*@formatter:off*/sn"""\
/**
 * Process custom request
 *
 * @param resultBuilder result builder, initially empty
 * @param inputData input data, may be {@code null} if not specified
 * @param inputProjection request input projection, may be {@code null} if not specified
 * @param outputProjection request output projection
 *
 * @return future of the result
 */
protected abstract @NotNull CompletableFuture<$shortDataType> process(
  @NotNull $shortBuilderType resultBuilder,
  @Nullable $inputTypeClass inputData,
  @Nullable ${inputFieldProjectionGen.shortClassName} inputProjection,
  @NotNull ${outputFieldProjectionGen.shortClassName} outputProjection
);
"""/*@formatter:off*/
        )
    }

        // case when input projection and data are not supported =================================================

      case None =>
        pathProjectionGenOpt match {

          case Some(pathProjectionGen) =>
            sctx.addImport(pathProjectionGen.fullClassName)
            sctx.addMethod(/*@formatter:off*/sn"""\
@Override
public @NotNull CompletableFuture<ReadOperationResponse<$shortDataType>> process(@NotNull CustomOperationRequest request) {
  $shortBuilderType builder = $resultBuilderCtor;
  ${pathProjectionGen.shortClassName} path = new ${pathProjectionGen.shortClassName}(request.path());
  ${outputFieldProjectionGen.shortClassName} outputProjection = new ${outputFieldProjectionGen.shortClassName}(request.outputProjection());
  return process(builder, path, inputProjection, outputProjection).thenApply(ReadOperationResponse::new);
}
"""/*@formatter:off*/
        )

        sctx.addMethod(/*@formatter:off*/sn"""\
/**
 * Process custom request
 *
 * @param resultBuilder result builder, initially empty
 * @param path request path
 * @param outputProjection request output projection
 *
 * @return future of the result
 */
protected abstract @NotNull CompletableFuture<$shortDataType> process(
  @NotNull $shortBuilderType resultBuilder,
  @NotNull ${pathProjectionGen.shortClassName} path,
  @NotNull ${outputFieldProjectionGen.shortClassName} outputProjection
);
"""/*@formatter:off*/
        )

      case None =>
        sctx.addMethod(/*@formatter:off*/sn"""\
@Override
public @NotNull CompletableFuture<ReadOperationResponse<$shortDataType>> process(@NotNull CustomOperationRequest request) {
  $shortBuilderType builder = $resultBuilderCtor;
  ${outputFieldProjectionGen.shortClassName} outputProjection = new ${outputFieldProjectionGen.shortClassName}(request.outputProjection());
  return process(builder, inputProjection, outputProjection).thenApply(ReadOperationResponse::new);
}
"""/*@formatter:off*/
        )

        sctx.addMethod(/*@formatter:off*/sn"""\
/**
 * Process custom request
 *
 * @param resultBuilder result builder, initially empty
 * @param outputProjection request output projection
 *
 * @return future of the result
 */
protected abstract @NotNull CompletableFuture<$shortDataType> process(@NotNull $shortBuilderType resultBuilder, @NotNull ${outputFieldProjectionGen.shortClassName} outputProjection);
"""/*@formatter:off*/
        )
    }

    }


    generate(sctx)
  }
}
