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

package ws.epigraph.projections.req.delete;

import ws.epigraph.lang.TextLocation;
import ws.epigraph.projections.Annotations;
import ws.epigraph.projections.gen.GenMapModelProjection;
import ws.epigraph.projections.req.ReqParams;
import ws.epigraph.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class ReqDeleteMapModelProjection
    extends ReqDeleteModelProjection<ReqDeleteMapModelProjection, MapType>
    implements GenMapModelProjection<
    ReqDeleteVarProjection,
    ReqDeleteTagProjectionEntry,
    ReqDeleteModelProjection<?, ?>,
    ReqDeleteMapModelProjection,
    MapType
    > {

  @Nullable
  private final List<ReqDeleteKeyProjection> keys;
  @NotNull
  private final ReqDeleteVarProjection valuesProjection;

  public ReqDeleteMapModelProjection(
      @NotNull MapType model,
      @NotNull ReqParams params,
      @NotNull Annotations annotations,
      @Nullable List<ReqDeleteKeyProjection> keys,
      @NotNull ReqDeleteVarProjection valuesProjection,
      @NotNull TextLocation location) {
    super(model, params, annotations, location);
    this.keys = keys;
    this.valuesProjection = valuesProjection;
  }

  @NotNull
  public ReqDeleteVarProjection itemsProjection() { return valuesProjection; }

  @Nullable
  public List<ReqDeleteKeyProjection> keys() { return keys; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    ReqDeleteMapModelProjection that = (ReqDeleteMapModelProjection) o;
    return Objects.equals(keys, that.keys) &&
           Objects.equals(valuesProjection, that.valuesProjection);
  }

  @Override
  public int hashCode() { return Objects.hash(super.hashCode(), keys, valuesProjection); }
}