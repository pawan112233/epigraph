package io.epigraph.projections.op.path;

import io.epigraph.lang.TextLocation;
import io.epigraph.projections.Annotations;
import io.epigraph.projections.ProjectionUtils;
import io.epigraph.projections.gen.GenRecordModelProjection;
import io.epigraph.projections.op.OpParams;
import io.epigraph.types.RecordType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class OpPathRecordModelProjection
    extends OpPathModelProjection<OpPathRecordModelProjection, RecordType>
    implements GenRecordModelProjection<
    OpPathVarProjection,
    OpPathTagProjectionEntry,
    OpPathModelProjection<?, ?>,
    OpPathRecordModelProjection,
    OpPathFieldProjectionEntry,
    OpPathFieldProjection,
    RecordType
    > {

  private static final
  ThreadLocal<IdentityHashMap<OpPathRecordModelProjection, OpPathRecordModelProjection>> equalsVisited =
      new ThreadLocal<>();

  @NotNull
  private Map<String, OpPathFieldProjectionEntry> fieldProjections;

  public OpPathRecordModelProjection(
      @NotNull RecordType model,
      @NotNull OpParams params,
      @NotNull Annotations annotations,
      @Nullable OpPathRecordModelProjection metaProjection,
      @Nullable OpPathFieldProjectionEntry fieldProjection,
      @NotNull TextLocation location) {
    super(model, params, annotations, metaProjection, location);

    this.fieldProjections = fieldProjection == null ?
                            Collections.emptyMap() :
                            Collections.singletonMap(fieldProjection.field().name(), fieldProjection);

    ProjectionUtils.checkFieldsBelongsToModel(fieldProjections.keySet(), model);
  }

  @NotNull
  public Map<String, OpPathFieldProjectionEntry> fieldProjections() { return fieldProjections; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    OpPathRecordModelProjection that = (OpPathRecordModelProjection) o;

    IdentityHashMap<OpPathRecordModelProjection, OpPathRecordModelProjection> visitedMap = equalsVisited.get();
    boolean mapWasNull = visitedMap == null;
    if (mapWasNull) {
      visitedMap = new IdentityHashMap<>();
      equalsVisited.set(visitedMap);
    } else {
      if (that == visitedMap.get(this)) return true;
      if (visitedMap.containsKey(this)) return false;
    }
    visitedMap.put(this, that);
    boolean res = Objects.equals(fieldProjections, that.fieldProjections);
    if (mapWasNull) equalsVisited.remove();
    return res;
  }

  @Override
  public int hashCode() {
    return super.hashCode() * 31 + fieldProjections.size();
  }
}
