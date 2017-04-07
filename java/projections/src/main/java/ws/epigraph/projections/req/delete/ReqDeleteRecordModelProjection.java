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

package ws.epigraph.projections.req.delete;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ws.epigraph.lang.TextLocation;
import ws.epigraph.projections.Annotations;
import ws.epigraph.projections.RecordModelProjectionHelper;
import ws.epigraph.projections.gen.GenRecordModelProjection;
import ws.epigraph.projections.gen.ProjectionReferenceName;
import ws.epigraph.projections.req.ReqParams;
import ws.epigraph.types.RecordTypeApi;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class ReqDeleteRecordModelProjection
    extends ReqDeleteModelProjection<ReqDeleteModelProjection<?, ?, ?>, ReqDeleteRecordModelProjection, RecordTypeApi>
    implements GenRecordModelProjection<
    ReqDeleteVarProjection,
    ReqDeleteTagProjectionEntry,
    ReqDeleteModelProjection<?, ?, ?>,
    ReqDeleteRecordModelProjection,
    ReqDeleteFieldProjectionEntry,
    ReqDeleteFieldProjection,
    RecordTypeApi
    > {

  private /*final*/ @NotNull Map<String, ReqDeleteFieldProjectionEntry> fieldProjections;

  public ReqDeleteRecordModelProjection(
      @NotNull RecordTypeApi model,
      @NotNull ReqParams params,
      @NotNull Annotations annotations,
      @NotNull Map<String, ReqDeleteFieldProjectionEntry> fieldProjections,
      @Nullable List<ReqDeleteRecordModelProjection> tails,
      @NotNull TextLocation location) {
    super(model, params, annotations, tails, location);
    this.fieldProjections = fieldProjections;

    RecordModelProjectionHelper.checkFields(fieldProjections, model);
  }

  public ReqDeleteRecordModelProjection(final @NotNull RecordTypeApi model, final @NotNull TextLocation location) {
    super(model, location);
    fieldProjections = Collections.emptyMap();
  }

  @Override
  public @NotNull Map<String, ReqDeleteFieldProjectionEntry> fieldProjections() {
    assert isResolved();
    return fieldProjections;
  }

  @Override
  public @Nullable ReqDeleteFieldProjectionEntry fieldProjection(@NotNull String fieldName) {
    return fieldProjections().get(fieldName);
  }

  @Override
  public void resolve(final @Nullable ProjectionReferenceName name, final @NotNull ReqDeleteRecordModelProjection value) {
    super.resolve(name, value);
    this.fieldProjections = value.fieldProjections();
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
