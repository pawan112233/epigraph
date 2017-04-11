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

package ws.epigraph.projections.op.input;

import org.jetbrains.annotations.NotNull;
import ws.epigraph.lang.TextLocation;
import ws.epigraph.projections.ProjectionUtils;
import ws.epigraph.projections.ReferenceContext;
import ws.epigraph.projections.gen.ProjectionReferenceName;
import ws.epigraph.psi.PsiProcessingContext;
import ws.epigraph.types.*;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class OpInputReferenceContext
    extends ReferenceContext<OpInputVarProjection, OpInputModelProjection<?, ?, ?, ?>> {

  public OpInputReferenceContext(
      final @NotNull ProjectionReferenceName referencesNamespace,
      final ReferenceContext<OpInputVarProjection, OpInputModelProjection<?, ?, ?, ?>> parent,
      final @NotNull PsiProcessingContext context) {
    super(referencesNamespace, parent, context);
  }

  @Override
  protected @NotNull OpInputVarProjection newVarReference(
      final @NotNull TypeApi type,
      final @NotNull TextLocation location) {
    return new OpInputVarProjection(type, location);
  }

  @Override
  protected OpInputModelProjection<?, ?, ?, ?> newRecordModelReference(
      final @NotNull RecordTypeApi type, final @NotNull TextLocation location) {
    return new OpInputRecordModelProjection(type, location);
  }

  @Override
  protected OpInputModelProjection<?, ?, ?, ?> newMapModelReference(
      final @NotNull MapTypeApi type, final @NotNull TextLocation location) {
    return new OpInputMapModelProjection(type, location);
  }

  @Override
  protected OpInputModelProjection<?, ?, ?, ?> newListModelReference(
      final @NotNull ListTypeApi type, final @NotNull TextLocation location) {
    return new OpInputListModelProjection(type, location);
  }

  @Override
  protected OpInputModelProjection<?, ?, ?, ?> newPrimitiveModelReference(
      final @NotNull PrimitiveTypeApi type, final @NotNull TextLocation location) {
    return new OpInputPrimitiveModelProjection(type, location);
  }

  @Override
  protected @NotNull OpInputVarProjection toSelfVar(final @NotNull OpInputModelProjection<?, ?, ?, ?> mRef) {
    final DatumTypeApi modelType = mRef.type();
    return new OpInputVarProjection(
        modelType,
        ProjectionUtils.singletonLinkedHashMap(
            modelType.self().name(),
            new OpInputTagProjectionEntry(
                modelType.self(),
                mRef,
                TextLocation.UNKNOWN
            )
        ),
        false,
        null,
        TextLocation.UNKNOWN
    );
  }
}