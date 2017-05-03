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

package ws.epigraph.server.http;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ws.epigraph.data.*;
import ws.epigraph.errors.ErrorValue;
import ws.epigraph.printers.DataPrinter;
import ws.epigraph.projections.req.output.*;
import ws.epigraph.service.AmbiguousPathException;
import ws.epigraph.util.HttpStatusCode;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public final class DataPathRemover {
  private DataPathRemover() {} // todo move somewhere?

  public static @NotNull PathRemovalResult removePath(
      @NotNull ReqOutputVarProjection projection,
      @NotNull Data data,
      int steps) throws AmbiguousPathException {

    projection = projection.normalizedForType(data.type());

    if (steps == 0) return new PathRemovalResult(projection, data);

    switch (projection.tagProjections().size()) {
      case 0:
        return PathRemovalResult.NULL;
      case 1:
        final ReqOutputTagProjectionEntry tpe =
            projection.tagProjections().values().iterator().next();

        final Val val = data._raw().tagValues().get(tpe.tag().name());
        if (val == null)
          return PathRemovalResult.NULL;

        return removePath(
            tpe.projection(),
            val,
            steps - 1
        );
      default:
        throw new AmbiguousPathException();
    }
  }

  public static @NotNull PathRemovalResult removePath(
      @NotNull ReqOutputModelProjection<?, ?, ?> mp,
      @NotNull Val val,
      int steps) throws AmbiguousPathException {

    if (val.getError() != null) return new PathRemovalResult(mp, val.getError()); // error on any segment = fail?
    final @Nullable Datum datum = val.getDatum();
    return (datum == null) ? PathRemovalResult.NULL : removePath(mp, datum, steps);
  }

  public static @NotNull PathRemovalResult removePath(
      @NotNull ReqOutputModelProjection<?, ?, ?> mp,
      @NotNull Datum datum,
      int steps) throws AmbiguousPathException {

    mp = mp.normalizedForType(datum.type());

    if (steps == 0) return new PathRemovalResult(mp, datum);

    switch (mp.type().kind()) {
      case UNION:
        throw new IllegalArgumentException("Unsupported model kind: " + mp.type().kind());
      case RECORD:
        ReqOutputRecordModelProjection rmp = (ReqOutputRecordModelProjection) mp;
        Map<String, ReqOutputFieldProjectionEntry> fieldProjections = rmp.fieldProjections();


        switch (fieldProjections.size()) {
          case 0:
            return PathRemovalResult.NULL;
          case 1:
            ReqOutputFieldProjectionEntry entry = fieldProjections.values().iterator().next();
            Data fieldData = ((RecordDatum) datum)._raw().fieldsData().get(entry.field().name());
            if (fieldData == null)
              return new PathRemovalResult(
                  mp,
                  new ErrorValue(
                      HttpStatusCode.NOT_FOUND, String.format("field '%s' not found", entry.field().name())
                  )
              );
            return removePath(entry.fieldProjection().varProjection(), fieldData, steps - 1);
          default:
            throw new AmbiguousPathException();
        }

      case MAP:
        ReqOutputMapModelProjection mmp = (ReqOutputMapModelProjection) mp;
        List<ReqOutputKeyProjection> keys = mmp.keys();
        Map<Datum.@NotNull Imm, @NotNull ? extends Data> mapElements = ((MapDatum) datum)._raw().elements();

        if (keys == null) {
          switch (mapElements.size()) {
            case 0:
              return PathRemovalResult.NULL;
            case 1:
              return removePath(mmp.itemsProjection(), mapElements.values().iterator().next(), steps - 1);
            default:
              throw new AmbiguousPathException();
          }
        } else {
          switch (keys.size()) {
            case 0:
              return PathRemovalResult.NULL;
            case 1:
              final ReqOutputKeyProjection kp = keys.iterator().next();
              final Datum.Imm key = kp.value().toImmutable();
              final Data value = mapElements.get(key);

              if (value == null) {
                String keyString = "";
                try {
                  StringWriter sw = new StringWriter();
                  DataPrinter<IOException> printer = DataPrinter.toString(80, false, sw);
                  printer.print(key);
                  keyString = "key '" + sw.toString() + "' ";
                } catch (IOException ignored) {}

                return new PathRemovalResult(
                    mp,
                    new ErrorValue(
                        HttpStatusCode.NOT_FOUND, keyString + "not found"
                    )
                );
              }

              return removePath(mmp.itemsProjection(), value, steps - 1);
            default:
              throw new AmbiguousPathException();
          }
        }

      case LIST:
        ReqOutputListModelProjection lmp = (ReqOutputListModelProjection) mp;
        final List<@NotNull ? extends Data> listElements = ((ListDatum) datum)._raw().elements();

        switch (listElements.size()) {
          case 0:
            return new PathRemovalResult(
                mp,
                new ErrorValue(HttpStatusCode.NOT_FOUND, "List item not found")
            );
          case 1:
            return removePath(lmp.itemsProjection(), listElements.get(0), steps - 1);
          default:
            throw new AmbiguousPathException();
        }

      case ENUM:
        throw new IllegalArgumentException("Unsupported model kind: " + mp.type().kind());
      case PRIMITIVE:
        throw new AmbiguousPathException();
      default:
        throw new IllegalArgumentException("Unsupported model kind: " + mp.type().kind());
    }
  }

  public static class PathRemovalResult {
    public static final PathRemovalResult NULL = new PathRemovalResult(null, null, null, null, null);

    public final @Nullable ReqOutputVarProjection dataProjection;
    public final @Nullable Data data;
    public final @Nullable ReqOutputModelProjection<?, ?, ?> datumProjection;
    public final @Nullable Datum datum;
    public final @Nullable ErrorValue error;

    public PathRemovalResult(
        @Nullable ReqOutputVarProjection projection,
        @Nullable Data data,
        @Nullable ReqOutputModelProjection<?, ?, ?> datumProjection,
        @Nullable Datum datum,
        @Nullable ErrorValue error) {
      dataProjection = projection;
      this.data = data;
      this.datumProjection = datumProjection;
      this.datum = datum;
      this.error = error;
    }

    public PathRemovalResult(@NotNull ReqOutputVarProjection dataProjection, @Nullable Data data) {
      this(
          dataProjection,
          data,
          null,
          null,
          null
      );
    }

    public PathRemovalResult(@NotNull ReqOutputModelProjection<?, ?, ?> datumProjection, @Nullable Datum datum) {
      this(
          null,
          null,
          datumProjection,
          datum,
          null
      );
    }

    public PathRemovalResult(@NotNull ReqOutputModelProjection<?, ?, ?> mp, @Nullable ErrorValue error) {
      this(
          null,
          null,
          mp,
          null,
          error
      );
    }
  }
}
