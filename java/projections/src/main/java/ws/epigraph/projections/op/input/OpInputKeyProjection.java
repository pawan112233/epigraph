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

package ws.epigraph.projections.op.input;

import org.jetbrains.annotations.NotNull;
import ws.epigraph.lang.TextLocation;
import ws.epigraph.projections.Annotations;
import ws.epigraph.projections.op.OpKeyPresence;
import ws.epigraph.projections.op.OpKeyProjection;
import ws.epigraph.projections.op.OpParams;

import java.util.Objects;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class OpInputKeyProjection extends OpKeyProjection {
  private final @NotNull OpKeyPresence presence;

  public OpInputKeyProjection(
      final @NotNull OpKeyPresence presence, final @NotNull OpParams params,
      final @NotNull Annotations annotations, final @NotNull TextLocation location) {
    super(params, annotations, location);
    this.presence = presence;
  }

  public OpKeyPresence presence() { return presence; }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    final OpInputKeyProjection that = (OpInputKeyProjection) o;
    return presence == that.presence;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), presence);
  }
}
