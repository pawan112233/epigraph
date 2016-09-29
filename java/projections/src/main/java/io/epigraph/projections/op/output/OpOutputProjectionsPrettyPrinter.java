package io.epigraph.projections.op.output;

import de.uka.ilkd.pp.Layouter;
import io.epigraph.data.Datum;
import io.epigraph.projections.generic.GenericProjectionsPrettyPrinter;
import io.epigraph.projections.CustomParams;
import io.epigraph.projections.op.OpParam;
import io.epigraph.projections.op.OpParams;
import io.epigraph.projections.op.input.OpInputModelProjection;
import io.epigraph.projections.op.input.OpInputProjectionsPrettyPrinter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class OpOutputProjectionsPrettyPrinter<E extends Exception> extends
    GenericProjectionsPrettyPrinter<OpOutputVarProjection, OpOutputTagProjection, OpOutputModelProjection<?>, E> {

  protected OpOutputProjectionsPrettyPrinter(Layouter<E> layouter) {
    super(layouter);
  }

  @Override
  public void print(@NotNull OpOutputTagProjection tp, int pathSteps) throws E {
    OpOutputModelProjection<?> projection = tp.projection();
    OpOutputModelProjection<?> metaProjection = projection.metaProjection();
    OpParams params = projection.params();
    CustomParams customParams = projection.customParams();

    if (params == null && customParams == null) {
      l.beginCInd();
      if (projection.includeInDefault()) l.print("+");
      l.print(tp.tag().name());

      if (!isPrintoutEmpty(projection)) {
        l.brk();
        print(projection, 0);
      }

      l.end();
    } else {
      l.beginCInd();
      if (projection.includeInDefault()) l.print("+");
      l.print(tp.tag().name());
      l.print(" {");

      if (params != null) print(params);
      if (customParams != null) print(customParams);

      if (metaProjection != null) {
        l.brk().beginIInd(0).print("meta:").brk();
        print(metaProjection, 0);
        l.end();
      }

      if (!isPrintoutEmpty(projection)) {
        l.brk();
        print(projection, 0);
      }

      l.brk(1, -l.getDefaultIndentation()).end().print("}");
    }
  }

  @Override
  public void print(@NotNull OpOutputModelProjection<?> mp, int pathSteps) throws E {
    if (mp instanceof OpOutputRecordModelProjection)
      print((OpOutputRecordModelProjection) mp);
    else if (mp instanceof OpOutputMapModelProjection)
      print((OpOutputMapModelProjection) mp);
    else if (mp instanceof OpOutputListModelProjection)
      print((OpOutputListModelProjection) mp);
  }

  private void print(@NotNull OpOutputRecordModelProjection mp) throws E {
    @Nullable LinkedHashSet<OpOutputFieldProjection> fieldProjections = mp.fieldProjections();

    if (fieldProjections != null) {
      l.print("(").beginCInd();
      boolean first = true;
      for (OpOutputFieldProjection fieldProjection : fieldProjections) {
        if (first) first = false;
        else l.print(",");
        l.brk();

        @NotNull OpOutputVarProjection fieldVarProjection = fieldProjection.projection();
        @Nullable OpParams fieldParams = fieldProjection.params();
        @Nullable CustomParams fieldCustomParams = fieldProjection.customParams();

        if (fieldParams == null && fieldCustomParams == null) {
          l.beginIInd();
          if (fieldProjection.includeInDefault()) l.print("+");
          l.print(fieldProjection.field().name());
          if (!isPrintoutEmpty(fieldVarProjection)) {
            l.brk();
            print(fieldVarProjection, 0);
          }
          l.end();
        } else {
          l.beginCInd();
          if (fieldProjection.includeInDefault()) l.print("+");
          l.print(fieldProjection.field().name());
          l.print(" {");
          if (fieldParams != null) print(fieldParams);
          if (fieldCustomParams != null) print(fieldCustomParams);
          if (!isPrintoutEmpty(fieldVarProjection)) {
            l.brk();
            print(fieldVarProjection, 0);
          }
          l.brk(1, -l.getDefaultIndentation()).end().print("}");
        }

      }
      l.brk(1, -l.getDefaultIndentation()).end().print(")");
    }
  }

  private void print(OpOutputMapModelProjection mp) throws E {
    l.beginIInd();

    { // keys
      @NotNull OpOutputKeyProjection keyProjection = mp.keyProjection();

      l.beginCInd();
      l.print("[");
      boolean commaNeeded = false;

      if (keyProjection.presence() == OpOutputKeyProjection.Presence.FORBIDDEN) {
        l.brk().print("forbidden");
        commaNeeded = true;
      }

      if (keyProjection.presence() == OpOutputKeyProjection.Presence.REQUIRED) {
        if (commaNeeded) l.print(",");
        l.brk().print("required");
        commaNeeded = true;
      }

      @Nullable OpParams keyParams = keyProjection.params();
      if (keyParams != null) {
        print(keyParams, true, !commaNeeded);
        commaNeeded = !keyParams.isEmpty();
      }

      @Nullable CustomParams keyCustomParams = keyProjection.customParams();
      if (keyCustomParams != null) print(keyCustomParams, true, !commaNeeded);

      l.brk(1, -l.getDefaultIndentation()).end().print("]");
    }

    l.print("(").brk();
    print(mp.itemsProjection(), 0);
    l.brk(1, -l.getDefaultIndentation()).end().print(")");
  }

  private void print(OpOutputListModelProjection mp) throws E {
    l.beginIInd();
    l.print("*(").brk();
    print(mp.itemsProjection(), 0);
    l.brk(1, -l.getDefaultIndentation()).end().print(")");
  }

  protected void print(@NotNull OpParams p) throws E {
    print(p, false, true);
  }

  protected void print(@NotNull OpParams p, boolean needCommas, boolean first) throws E {
    l.beginCInd(0);
    for (OpParam param : p.params().values()) {
      if (needCommas) {
        if (first) first = false;
        else l.print(",");
      }
      l.brk();
      print(param);
    }
    l.end();
  }

  private void print(@NotNull OpParam p) throws E {
    OpInputModelProjection<?, ?> projection = p.projection();

    l.beginIInd();
    l.print(";");
    if (projection.required()) l.print("+");
    l.print(p.name()).print(":").brk();
    l.print(projection.model().name().toString());

    OpInputProjectionsPrettyPrinter<E> ipp = new OpInputProjectionsPrettyPrinter<>(l);

    if (!ipp.isPrintoutEmpty(projection)) {
      l.brk();
      ipp.print(projection, 0);
    }

    Datum defaultValue = projection.defaultValue();
    if (defaultValue != null) {
      l.brk().print("=").brk();
      dataPrinter.print(defaultValue);
    }

    CustomParams customParams = projection.customParams();
    if (customParams != null) {
      l.beginCInd();
      l.print(" {");
      print(customParams);
      l.brk(1, -l.getDefaultIndentation()).end().print("}");
    }

    l.end();
  }

  @Override
  public boolean isPrintoutEmpty(@NotNull OpOutputModelProjection<?> mp) {
    if (mp instanceof OpOutputRecordModelProjection) {
      OpOutputRecordModelProjection recordModelProjection = (OpOutputRecordModelProjection) mp;
      @Nullable LinkedHashSet<OpOutputFieldProjection> fieldProjections =
          recordModelProjection.fieldProjections();
      return fieldProjections == null || fieldProjections.isEmpty();
    }

    if (mp instanceof OpOutputMapModelProjection) {
      OpOutputMapModelProjection mapModelProjection = (OpOutputMapModelProjection) mp;
      @NotNull OpOutputKeyProjection keyProjection = mapModelProjection.keyProjection();

      if (keyProjection.presence() != OpOutputKeyProjection.Presence.OPTIONAL) return false;
      if (keyProjection.params() != null) return false;
      if (keyProjection.customParams() != null) return false;

      return isPrintoutEmpty(mapModelProjection.itemsProjection());
    }

    if (mp instanceof OpOutputListModelProjection) {
      OpOutputListModelProjection outputListModelProjection = (OpOutputListModelProjection) mp;
      return isPrintoutEmpty(outputListModelProjection.itemsProjection());
    }

    return true;
  }
}
