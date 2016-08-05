/* Created by yegor on 7/21/16. */

package io.epigraph.types;

import io.epigraph.datum.Data;
import io.epigraph.datum.Val;
import io.epigraph.names.TypeName;
import io.epigraph.util.LazyInitializer;
import io.epigraph.util.Unmodifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;


public abstract class Type { // TODO split into interface and impl

  private final TypeName name;

  private final List<? extends Type> immediateSupertypes;

  protected Type(TypeName name, List<? extends Type> immediateSupertypes) {
    this.name = name;
    this.immediateSupertypes = Unmodifiable.list(immediateSupertypes);

    // assert none of the immediate supertypes is a supertype of another one
    if (immediateSupertypes.stream().anyMatch(is -> is.supertypes().stream().anyMatch(immediateSupertypes::contains)))
      throw new IllegalArgumentException();


  }

  public @NotNull TypeName name() { return name; }

  /**
   * @return immediate (i.e. not transitive) supertypes of this type, in the order of increasing priority
   */
  public @NotNull List<? extends Type> immediateSupertypes() { return immediateSupertypes; }

  private @Nullable Collection<? extends Type> supertypes = null;

  /**
   * @return linearized supertypes of this type, in order of decreasing priority
   */
  public Collection<? extends Type> supertypes() {
    if (supertypes == null) { // TODO move initialization to constructor?
      LinkedList<Type> acc = new LinkedList<>();
      for (Type is : immediateSupertypes) {
        assert !acc.contains(is);
        acc.addFirst(is);
        is.supertypes().stream().filter(iss -> !acc.contains(iss)).forEachOrdered(acc::addFirst);
      }
      supertypes = Unmodifiable.collection(new LinkedHashSet<>(acc));
      assert supertypes.size() == acc.size(); // assert there was no duplicates in the acc
    }
    return supertypes;
  }

  public boolean doesExtend(Type type) { return this.equals(type) || supertypes().contains(type); }

  public boolean isAssignableFrom(Type type) { return type.doesExtend(this); }

  private final LazyInitializer<ListType> listOf = new LazyInitializer<>(listOfTypeSupplier()); // FIXME race?

  protected abstract @NotNull Supplier<ListType> listOfTypeSupplier(); // e.g. () -> new AnonListType(false, this)

  public ListType listOf() { return listOf.get(); }

  @NotNull
  public abstract List<Tag> immediateTags();

  @NotNull
  public abstract List<Tag> tags(); // FIXME do we need this method?

  public abstract @NotNull Data.Mut createMutableData(); // { return new Data.Mut(this); }


  public interface Raw {}


  public interface Static<MyType extends Type & Type.Static<MyType>> {}


  public static class Tag {

    public final String name;

    public final DatumType type;

    public Tag(String name, DatumType type) {
      this.name = name;
      this.type = type;
    }

    public @NotNull Val.Mut createMutableValue() { return this.type.createMutableValue(); }

  }

//  public static interface Tagged { // TODO remove?
//
//    public Tag tag();
//
//  }

}