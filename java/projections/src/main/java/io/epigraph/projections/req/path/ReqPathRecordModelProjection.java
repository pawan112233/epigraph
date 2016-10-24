package io.epigraph.projections.req.path;

import io.epigraph.lang.TextLocation;
import io.epigraph.projections.Annotations;
import io.epigraph.projections.ProjectionUtils;
import io.epigraph.projections.gen.GenRecordModelProjection;
import io.epigraph.projections.req.ReqParams;
import io.epigraph.types.RecordType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class ReqPathRecordModelProjection
    extends ReqPathModelProjection<ReqPathRecordModelProjection, RecordType>
    implements GenRecordModelProjection<
    ReqPathVarProjection,
    ReqPathTagProjectionEntry,
    ReqPathModelProjection<?, ?>,
    ReqPathRecordModelProjection,
    ReqPathFieldProjectionEntry,
    ReqPathFieldProjection,
    RecordType
    > {

  private static final ThreadLocal<IdentityHashMap<ReqPathRecordModelProjection, ReqPathRecordModelProjection>>
      equalsVisited = new ThreadLocal<>();

  @NotNull
  private Map<String, ReqPathFieldProjectionEntry> fieldProjections;

  public ReqPathRecordModelProjection(
      @NotNull RecordType model,
      boolean required,
      @NotNull ReqParams params,
      @NotNull Annotations annotations,
      @Nullable ReqPathRecordModelProjection metaProjection,
      @NotNull ReqPathFieldProjectionEntry fieldProjection,
      @NotNull TextLocation location) {
    super(model, required, params, annotations, metaProjection, location);
    this.fieldProjections = Collections.singletonMap(fieldProjection.field().name(), fieldProjection);

    Collection<@NotNull ? extends RecordType.Field> fields = model.fields();
    ProjectionUtils.checkFieldsBelongsToModel(fieldProjections.keySet(), model);
  }

  @NotNull
  public Map<String, ReqPathFieldProjectionEntry> fieldProjections() { return fieldProjections; }

  @Nullable
  public ReqPathFieldProjectionEntry fieldProjection(@NotNull String fieldName) {
    return fieldProjections.get(fieldName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    ReqPathRecordModelProjection that = (ReqPathRecordModelProjection) o;

    IdentityHashMap<ReqPathRecordModelProjection, ReqPathRecordModelProjection> visitedMap = equalsVisited.get();
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
