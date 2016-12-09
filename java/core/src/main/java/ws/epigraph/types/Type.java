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

/* Created by yegor on 7/21/16. */

package ws.epigraph.types;

import ws.epigraph.data.Data;
import ws.epigraph.names.TypeName;
import ws.epigraph.util.Unmodifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public abstract class Type { // TODO split into interface and impl

  private final @NotNull TypeName name;

  private final @NotNull List<@NotNull ? extends Type> immediateSupertypes;

  private @Nullable Collection<@NotNull ? extends Tag> tags = null;

  private @Nullable Map<@NotNull String, @NotNull ? extends Tag> tagsMap = null;

  protected Type(@NotNull TypeName name, @NotNull List<@NotNull ? extends Type> immediateSupertypes) {
    this.name = name;
    this.immediateSupertypes = Unmodifiable.list(immediateSupertypes);

    // assert none of the immediate supertypes is a supertype of another one
    if (immediateSupertypes.stream().anyMatch(is -> is.supertypes().stream().anyMatch(immediateSupertypes::contains)))
      throw new IllegalArgumentException();
  }

  public abstract @NotNull TypeKind kind();

  public @NotNull TypeName name() { return name; }

  /**
   * @return immediate (i.e. not transitive) supertypes of this type, in the order of increasing priority
   */
  public @NotNull List<@NotNull ? extends Type> immediateSupertypes() { return immediateSupertypes; }

  private @Nullable Collection<? extends Type> supertypes = null;

  /**
   * @return linearized supertypes of this type, in order of decreasing priority
   */
  public @NotNull Collection<@NotNull ? extends Type> supertypes() {
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

  /** @see Class#isAssignableFrom(Class) */
  public boolean isAssignableFrom(@NotNull Type type) { return type.equals(this) || type.supertypes().contains(this); }

  /** @see Class#isInstance(Object) */
  public boolean isInstance(@Nullable Data data) { return data != null && isAssignableFrom(data.type()); }

  public abstract @NotNull Collection<@NotNull ? extends Tag> immediateTags();

  public abstract @NotNull Data.Builder createDataBuilder();

  public final @NotNull Collection<@NotNull ? extends Tag> tags() {
    // TODO produce better ordering of the tags (i.e. supertypes first, in the order of supertypes and their tags declaration)
    if (tags == null) { // TODO move initialization to constructor (if possible?)
      LinkedList<Tag> acc = new LinkedList<>(immediateTags());
      for (Type st : supertypes()) {
        st.tags().stream().filter(sf ->
            acc.stream().noneMatch(af -> af.name.equals(sf.name))
        ).forEachOrdered(acc::add);
      }
      tags = Unmodifiable.collection(new LinkedHashSet<>(acc));
      assert tags.size() == acc.size(); // assert there was no duplicates in the acc
    }
    return tags;
  }

  public final @NotNull Map<@NotNull String, @NotNull ? extends Tag> tagsMap() {
    if (tagsMap == null) tagsMap = Unmodifiable.map(tags(), t -> t.name, t -> t);
    return tagsMap;
  }

  public <D extends Data> D checkAssignable(@NotNull D data) throws IllegalArgumentException { // TODO accept nulls?
    if (!isInstance(data)) throw new IllegalArgumentException("TODO");
    return data;
  }

  /** Ensures specified type is a subtype of this type. */
  public <T extends Type> T checkAssignable(@NotNull T type) throws IllegalArgumentException {
    if (!isAssignableFrom(type))
      throw new IllegalArgumentException("Type '" + type.name() + "' is not compatible with type '" + name() + "'");
    return type;
  }


  public interface Raw {

    //@NotNull Data.Mut createMutableData();

  }


  public interface Static<MyImmData extends Data.Imm.Static, MyDataBuilder extends Data.Builder.Static<MyImmData>> {

    @NotNull MyDataBuilder createDataBuilder();

  }


  public static class Tag {

    public final @NotNull String name;

    public final @NotNull DatumType type;

    public Tag(@NotNull String name, @NotNull DatumType type) {
      this.name = name;
      this.type = type;
    }

    public @NotNull String name() { return name; }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      final Tag tag = (Tag) o;
      return Objects.equals(name, tag.name) &&
             Objects.equals(type, tag.type);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, type);
    }
  }


}
