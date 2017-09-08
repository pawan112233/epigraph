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
import ws.epigraph.projections.VarNormalizationContext;
import ws.epigraph.projections.abs.AbstractVarProjection;
import ws.epigraph.projections.gen.ProjectionReferenceName;
import ws.epigraph.types.TypeApi;
import ws.epigraph.types.TypeKind;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class ReqOutputVarProjection extends AbstractVarProjection<
    ReqOutputVarProjection,
    ReqOutputTagProjectionEntry,
    ReqOutputModelProjection<?, ?, ?>
    > {

  protected /*final*/ boolean flagged;

  public ReqOutputVarProjection(
      @NotNull TypeApi type,
      boolean flagged,
      @NotNull Map<String, ReqOutputTagProjectionEntry> tagProjections,
      boolean parenthesized,
      @Nullable List<ReqOutputVarProjection> polymorphicTails,
      @NotNull TextLocation location) {
    super(type, tagProjections, parenthesized, polymorphicTails, location);

    if (tagProjections.size() > 1 && !parenthesized)
      throw new IllegalArgumentException("'parenthesized' must be 'true' for a multi-tag projection");

    //noinspection ConstantConditions
    this.flagged = flagged || (type.kind() != TypeKind.ENTITY && singleTagProjection().projection().flagged());
  }

  public ReqOutputVarProjection(final @NotNull TypeApi type, final @NotNull TextLocation location) {
    super(type, location);
  }

  public boolean flagged() { return flagged; }

  @Override
  protected ReqOutputVarProjection merge(
      final @NotNull TypeApi effectiveType,
      final @NotNull List<ReqOutputVarProjection> varProjections,
      final @NotNull Map<String, ReqOutputTagProjectionEntry> mergedTags,
      final boolean mergedParenthesized,
      final List<ReqOutputVarProjection> mergedTails) {

    boolean mergedFlagged = varProjections.stream().anyMatch(ReqOutputVarProjection::flagged);
    return new ReqOutputVarProjection(
        effectiveType,
        mergedFlagged,
        mergedTags,
        mergedParenthesized, mergedTails,
        TextLocation.UNKNOWN
    );
  }

  @Override
  protected @NotNull VarNormalizationContext<ReqOutputVarProjection> newNormalizationContext() {
    return new VarNormalizationContext<>(
        t -> new ReqOutputVarProjection(t, location())
    );
  }

  @Override
  public void resolve(final @Nullable ProjectionReferenceName name, final @NotNull ReqOutputVarProjection value) {
    preResolveCheck(value);
    this.flagged = value.flagged();
    super.resolve(name, value);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    final ReqOutputVarProjection that = (ReqOutputVarProjection) o;
    return flagged == that.flagged;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), flagged);
  }
}
