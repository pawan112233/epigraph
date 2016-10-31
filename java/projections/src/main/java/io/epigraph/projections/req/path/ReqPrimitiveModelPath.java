package io.epigraph.projections.req.path;

import io.epigraph.lang.TextLocation;
import io.epigraph.projections.Annotations;
import io.epigraph.projections.gen.GenPrimitiveModelProjection;
import io.epigraph.projections.req.ReqParams;
import io.epigraph.types.PrimitiveType;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class ReqPrimitiveModelPath
    extends ReqModelPath<ReqPrimitiveModelPath, PrimitiveType<?>>
    implements GenPrimitiveModelProjection<ReqPrimitiveModelPath, PrimitiveType<?>> {

  public ReqPrimitiveModelPath(
      @NotNull PrimitiveType model,
      @NotNull ReqParams params,
      @NotNull Annotations annotations,
      @NotNull TextLocation location) {
    super(model, params, annotations, location);
  }
}