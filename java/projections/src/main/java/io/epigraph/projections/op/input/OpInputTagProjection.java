package io.epigraph.projections.op.input;

import de.uka.ilkd.pp.DataLayouter;
import de.uka.ilkd.pp.PrettyPrintable;
import io.epigraph.types.Type;
import io.epigraph.util.pp.DataPrettyPrinter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class OpInputTagProjection implements PrettyPrintable {
  @NotNull
  private final Type.Tag tag;
  @NotNull
  private final OpInputModelProjection<?, ?> projection;

  public OpInputTagProjection(@NotNull Type.Tag tag,
                              @NotNull OpInputModelProjection<?, ?> projection) {
    this.tag = tag;
    this.projection = projection;
    if (!tag.type.equals(projection.model)) { // or can it be a sub-type?
      throw new IllegalArgumentException(
          String.format("Tag model '%s' is different from tag projection model '%s'", tag.type, projection.model)
      );
    }
  }

  @NotNull
  public Type.Tag tag() { return tag; }

  @NotNull
  public OpInputModelProjection<?, ?> projection() { return projection; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OpInputTagProjection that = (OpInputTagProjection) o;
    return Objects.equals(tag, that.tag) &&
           Objects.equals(projection, that.projection);
  }

  @Override
  public int hashCode() { return Objects.hash(tag); }

  @Override
  public <Exc extends Exception> void prettyPrint(DataLayouter<Exc> l) throws Exc {
    l.beginC().print(tag.name).print(":").brk();
    l.print(projection);
    l.end();
  }

  @Override
  public String toString() { return DataPrettyPrinter.prettyPrint(this); }
}