package io.epigraph.projections.op.output;

import de.uka.ilkd.pp.DataLayouter;
import de.uka.ilkd.pp.PrettyPrintable;
import io.epigraph.types.DatumType;
import io.epigraph.util.pp.DataPrettyPrinter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public abstract class OpOutputModelProjection<M extends DatumType>
    implements PrettyPrintable {
  @NotNull
  protected final M model;
  protected final boolean includeInDefault;
  @Nullable
  protected final Set<OpParam> params;

  public OpOutputModelProjection(@NotNull M model,
                                 boolean includeInDefault,
                                 @Nullable Set<OpParam> params) {
    this.model = model;
    this.includeInDefault = includeInDefault;
    this.params = params;
  }

  public M model() { return model; }

  public boolean includeInDefault() { return includeInDefault; }

  public @Nullable Set<OpParam> params() { return params; }

  @Override
  public <Exc extends Exception> void prettyPrint(DataLayouter<Exc> l) throws Exc {
    prettyPrintModel(l);
    prettyPrintParamsBlock(l);
  }

  protected <Exc extends Exception> void prettyPrintModel(DataLayouter<Exc> l) throws Exc {
    if (includeInDefault) l.print("+");
    l.print(model.name().toString());
  }

  protected <Exc extends Exception> void prettyPrintParamsBlock(DataLayouter<Exc> l) throws Exc {
    if (params != null && !params.isEmpty()) {
      l.beginCInd().print(" {");
      prettyPrintParams(l, params);
      l.end().brk().print("}");
    }
  }

  protected <Exc extends Exception> void prettyPrintParams(DataLayouter<Exc> l, @NotNull Collection<OpParam> params)
      throws Exc {
    for (OpParam param : params) {
      l.brk().print(param);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OpOutputModelProjection<?> that = (OpOutputModelProjection<?>) o;
    return includeInDefault == that.includeInDefault &&
           Objects.equals(model, that.model) &&
           Objects.equals(params, that.params);
  }

  @Override
  public int hashCode() { return Objects.hash(model, includeInDefault, params); }

  @Override
  public String toString() { return DataPrettyPrinter.prettyPrint(this); }
}