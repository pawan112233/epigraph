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

package ws.epigraph.projections.req.output;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ws.epigraph.lang.TextLocation;
import ws.epigraph.projections.Annotations;
import ws.epigraph.projections.RecordModelProjectionHelper;
import ws.epigraph.projections.gen.GenRecordModelProjection;
import ws.epigraph.projections.gen.ProjectionReferenceName;
import ws.epigraph.projections.req.ReqParams;
import ws.epigraph.types.DatumTypeApi;
import ws.epigraph.types.FieldApi;
import ws.epigraph.types.RecordTypeApi;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ws.epigraph.projections.RecordModelProjectionHelper.reattachFields;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class ReqOutputRecordModelProjection
    extends ReqOutputModelProjection<ReqOutputModelProjection<?, ?, ?>, ReqOutputRecordModelProjection, RecordTypeApi>
    implements GenRecordModelProjection<
    ReqOutputVarProjection,
    ReqOutputTagProjectionEntry,
    ReqOutputModelProjection<?, ?, ?>,
    ReqOutputRecordModelProjection,
    ReqOutputFieldProjectionEntry,
    ReqOutputFieldProjection,
    RecordTypeApi
    > {

  private /*final*/ @NotNull Map<String, ReqOutputFieldProjectionEntry> fieldProjections;

  public ReqOutputRecordModelProjection(
      @NotNull RecordTypeApi model,
      boolean required,
      @NotNull ReqParams params,
      @NotNull Annotations annotations,
      @Nullable ReqOutputModelProjection<?, ?, ?> metaProjection,
      @NotNull Map<String, ReqOutputFieldProjectionEntry> fieldProjections,
      @Nullable List<ReqOutputRecordModelProjection> tails,
      @NotNull TextLocation location) {
    super(model, required, params, annotations, metaProjection, tails, location);
    this.fieldProjections = fieldProjections;

    RecordModelProjectionHelper.checkFields(fieldProjections, model);
  }

  public ReqOutputRecordModelProjection(final @NotNull RecordTypeApi model, final @NotNull TextLocation location) {
    super(model, location);
    fieldProjections = Collections.emptyMap();
  }

  @Override
  public @NotNull Map<String, ReqOutputFieldProjectionEntry> fieldProjections() {
    assert isResolved();
    return fieldProjections;
  }

  @Override
  public @Nullable ReqOutputFieldProjectionEntry fieldProjection(@NotNull String fieldName) {
    return fieldProjections().get(fieldName);
  }

  @Override
  protected ReqOutputRecordModelProjection merge(
      final @NotNull RecordTypeApi model,
      final boolean mergedRequired,
      final @NotNull List<ReqOutputRecordModelProjection> modelProjections,
      final @NotNull ReqParams mergedParams,
      final @NotNull Annotations mergedAnnotations,
      final @Nullable ReqOutputModelProjection<?, ?, ?> mergedMetaProjection,
      final @Nullable List<ReqOutputRecordModelProjection> mergedTails,
      final boolean keepPhantomTails) {


    Map<FieldApi, ReqOutputFieldProjection> mergedFieldProjections =
        RecordModelProjectionHelper.mergeFieldProjections(modelProjections, keepPhantomTails);

    Map<String, ReqOutputFieldProjectionEntry> mergedFieldEntries = new LinkedHashMap<>();
    for (final Map.Entry<FieldApi, ReqOutputFieldProjection> entry : mergedFieldProjections.entrySet()) {
      mergedFieldEntries.put(
          entry.getKey().name(),
          new ReqOutputFieldProjectionEntry(
              entry.getKey(),
              entry.getValue(),
              TextLocation.UNKNOWN
          )
      );
    }

    return new ReqOutputRecordModelProjection(
        model,
        mergedRequired,
        mergedParams,
        mergedAnnotations,
        mergedMetaProjection,
        mergedFieldEntries,
        mergedTails,
        TextLocation.UNKNOWN
    );
  }

  @Override
  protected @NotNull ReqOutputRecordModelProjection postNormalizedForType(
      final @NotNull DatumTypeApi targetType,
      final boolean keepPhantomTails,
      final @NotNull ReqOutputRecordModelProjection n) {
    RecordTypeApi targetRecordType = (RecordTypeApi) targetType;
    
    final Map<String, ReqOutputFieldProjection> normalizedFields =
        RecordModelProjectionHelper.normalizeFields(targetRecordType, n, keepPhantomTails);
    
    final Map<String, ReqOutputFieldProjectionEntry> normalizedFieldEntries = reattachFields(
        targetRecordType,
        normalizedFields,
        ReqOutputFieldProjectionEntry::new
    );

    return new ReqOutputRecordModelProjection(
        n.type(),
        n.required(),
        n.params(),
        n.annotations(),
        n.metaProjection(),
        normalizedFieldEntries,
        n.polymorphicTails(),
        TextLocation.UNKNOWN
    );
  }

  @Override
  public void resolve(final @Nullable ProjectionReferenceName name, final @NotNull ReqOutputRecordModelProjection value) {
    super.resolve(name, value);
    fieldProjections = value.fieldProjections();
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o) && RecordModelProjectionHelper.equals(this, o);
  }

  @Override
  public int hashCode() {
    return super.hashCode() * 31 + fieldProjections.size();
  }
}
