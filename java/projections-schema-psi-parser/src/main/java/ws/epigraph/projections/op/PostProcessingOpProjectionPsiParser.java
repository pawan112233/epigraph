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

package ws.epigraph.projections.op;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ws.epigraph.projections.op.output.*;
import ws.epigraph.psi.PsiProcessingContext;
import ws.epigraph.psi.PsiProcessingException;
import ws.epigraph.refs.TypesResolver;
import ws.epigraph.schema.parser.psi.SchemaOpEntityProjection;
import ws.epigraph.schema.parser.psi.SchemaOpFieldProjection;
import ws.epigraph.schema.parser.psi.SchemaOpModelProjection;
import ws.epigraph.schema.parser.psi.SchemaOpUnnamedOrRefEntityProjection;
import ws.epigraph.types.DataTypeApi;
import ws.epigraph.types.DatumTypeApi;

import java.util.function.Function;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class PostProcessingOpProjectionPsiParser implements OpProjectionPsiParser {
  private final @Nullable Function<PsiProcessingContext, OpProjectionTraversal> traversalFactory;
  private final @Nullable Function<PsiProcessingContext, OpProjectionTransformer> transformerFactory;

  public PostProcessingOpProjectionPsiParser(
      final @Nullable Function<PsiProcessingContext, OpProjectionTraversal> traversalFactory,
      final @Nullable Function<PsiProcessingContext, OpProjectionTransformer> transformerFactory) {

    this.traversalFactory = traversalFactory;
    this.transformerFactory = transformerFactory;
  }

  @Override
  public @NotNull OpEntityProjection parseEntityProjection(
      final @NotNull DataTypeApi dataType,
      final boolean flagged,
      final @NotNull SchemaOpEntityProjection psi,
      final @NotNull TypesResolver typesResolver,
      final @NotNull OpPsiProcessingContext context) throws PsiProcessingException {

    OpEntityProjection res =
        OpBasicProjectionPsiParser.parseEntityProjection(dataType, flagged, psi, typesResolver, context);

    return processEntityProjection(res, context);
  }

  @Override
  public @NotNull OpFieldProjection parseFieldProjection(
      final @NotNull DataTypeApi fieldType,
      final boolean flagged,
      final @NotNull SchemaOpFieldProjection psi,
      final @NotNull TypesResolver resolver,
      final @NotNull OpPsiProcessingContext context) throws PsiProcessingException {

    OpFieldProjection res =
        OpBasicProjectionPsiParser.parseFieldProjection(fieldType, flagged, psi, resolver, context);

    OpEntityProjection transformedEp = processEntityProjection(res.entityProjection(), context);

    return new OpFieldProjection(transformedEp, res.location());
  }

  @Override
  public @NotNull OpEntityProjection parseUnnamedOrRefEntityProjection(
      final @NotNull DataTypeApi dataType,
      final boolean flagged,
      final @NotNull SchemaOpUnnamedOrRefEntityProjection psi,
      final @NotNull TypesResolver typesResolver,
      final @NotNull OpPsiProcessingContext context) throws PsiProcessingException {

    OpEntityProjection res =
        OpBasicProjectionPsiParser.parseUnnamedOrRefEntityProjection(dataType, flagged, psi, typesResolver, context);

    return processEntityProjection(res, context);
  }

  @Override
  public @NotNull OpModelProjection<?, ?, ?, ?> parseModelProjection(
      final @NotNull DatumTypeApi type,
      final boolean flagged,
      final @NotNull SchemaOpModelProjection psi,
      final @NotNull TypesResolver typesResolver,
      final @NotNull OpPsiProcessingContext context) throws PsiProcessingException {

    OpModelProjection<?,?,?,?> res =
        OpBasicProjectionPsiParser.parseModelProjection(type, flagged, psi, typesResolver, context);

    return processModelProjection(res, context);
  }

  private @NotNull OpEntityProjection processEntityProjection(
      @NotNull OpEntityProjection ep,
      @NotNull OpPsiProcessingContext context) {

    if (traversalFactory != null) {
      OpProjectionTraversal traversal = traversalFactory.apply(context);
      traversal.traverse(ep);
    }

    if (transformerFactory == null)
      return ep;
    else {
      OpProjectionTransformer transformer = transformerFactory.apply(context);
      OpProjectionTransformationMap transformationMap = new OpProjectionTransformationMap();
      OpEntityProjection transformedMp = transformer.transform(transformationMap, ep, null);

      context.referenceContext().transform(transformationMap);
      return transformedMp;
    }
  }

  private @NotNull OpModelProjection<?,?,?,?> processModelProjection(
      @NotNull OpModelProjection<?,?,?,?> mp,
      @NotNull OpPsiProcessingContext context) {

    if (traversalFactory != null) {
      OpProjectionTraversal traversal = traversalFactory.apply(context);
      traversal.traverse(mp);
    }

    if (transformerFactory == null)
      return mp;
    else {
      OpProjectionTransformer transformer = transformerFactory.apply(context);
      OpProjectionTransformationMap transformationMap = new OpProjectionTransformationMap();
      OpModelProjection<?, ?, ?, ?> transformedMp = transformer.transform(transformationMap, mp);

      context.referenceContext().transform(transformationMap);
      return transformedMp;
    }
  }
}
