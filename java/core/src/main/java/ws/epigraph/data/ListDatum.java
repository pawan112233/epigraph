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

/* Created by yegor on 8/3/16. */

package ws.epigraph.data;

import ws.epigraph.types.ListType;
import ws.epigraph.util.Unmodifiable;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Function;


public interface ListDatum extends Datum {

  @Override
  @NotNull ListType type();

  @Override
  @NotNull ListDatum.Raw _raw();

  @Override
  @NotNull ListDatum.Imm toImmutable();

  int size(); // TODO isEmpty()?


  abstract class Impl extends Datum.Impl<ListType> implements ListDatum {

    protected Impl(@NotNull ListType type) { super(type); }

  }


  interface Raw extends ListDatum, Datum.Raw {

    @Override
    @NotNull ListDatum.Imm.Raw toImmutable();

    @NotNull List<@NotNull ? extends Data> elements(); // TODO or Iterable? or Collection? rename to data()?

  }


  interface Static extends ListDatum, Datum.Static {

    @Override
    @NotNull ListDatum.Imm.Static toImmutable();

  }


  interface Imm extends ListDatum, Datum.Imm {

    @Override
    @NotNull ListDatum.Imm.Raw _raw();


    final class Raw extends ListDatum.Impl implements ListDatum.Imm, ListDatum.Raw, Datum.Imm.Raw {

      private final List<? extends Data.Imm> elements;

      private final @NotNull Val.Imm.Raw value = new Val.Imm.Raw.DatumVal(this);

      private final int hashCode;

      public Raw(@NotNull ListDatum.Builder.Raw mutable) {
        super(mutable.type());
        elements = Unmodifiable.list(mutable.elements(), Data::toImmutable);
        hashCode = Objects.hash(type(), elements);
      }

      @Override
      public int size() { return elements.size(); }

      @Override
      public @NotNull List<@NotNull ? extends Data.Imm> elements() { return elements; }

      @Override
      public @NotNull ListDatum.Imm.Raw toImmutable() { return this; }

      @Override
      public @NotNull ListDatum.Imm.Raw _raw() { return this; }

      @Override
      public @NotNull Val.Imm.Raw asValue() { return value; }

      @Override
      public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListDatum)) return false;
        if (o instanceof Immutable && hashCode != o.hashCode()) return false;
        ListDatum that = (ListDatum) o;
        return type().equals(that.type()) && elements.equals(that._raw().elements());
      }

      @Override
      public final int hashCode() { return hashCode; }

    }


    interface Static extends ListDatum.Imm, ListDatum.Static, Datum.Imm.Static {

      @Override
      @NotNull ListDatum.Imm.Static toImmutable();

      @Override
      @NotNull ListDatum.Imm.Raw _raw();


      // TODO additional sub-classes for Union and Datum element type based lists?
      abstract class Impl<MyImmDatum extends ListDatum.Imm.Static, MyImmVal extends Val.Imm.Static>
          extends ListDatum.Impl implements ListDatum.Imm.Static {

        private final @NotNull ListDatum.Imm.Raw raw;

        private final @NotNull MyImmVal value;

        protected Impl(
            @NotNull ListType type,
            @NotNull ListDatum.Imm.Raw raw,
            @NotNull Function<Val.Imm.@NotNull Raw, @NotNull MyImmVal> immValConstructor
        ) {
          super(type);
          // TODO check types are compatible
          this.raw = raw; // TODO validate raw internals is kosher?
          this.value = immValConstructor.apply(new Val.Imm.Raw.DatumVal(this));
        }

        @Override
        public int size() { return raw.size(); }

        @Override
        public @NotNull MyImmDatum toImmutable() { return (MyImmDatum) this; }  // TODO or make abstract and implement in final static impl?

        @Override
        public final @NotNull ListDatum.Imm.Raw _raw() { return raw; }

        @Override
        public @NotNull MyImmVal asValue() { return value; }

        @Override
        public final int hashCode() { return raw.hashCode(); }

        @Override
        public final boolean equals(Object obj) { return raw.equals(obj); }

      }


    }


  }


  abstract class Builder extends ListDatum.Impl implements Datum.Builder {

    protected Builder(@NotNull ListType type) { super(type); }

    @Override
    public abstract @NotNull ListDatum.Builder.Raw _raw();


    public static final class Raw extends ListDatum.Builder implements ListDatum.Raw, Datum.Builder.Raw {

      private final @NotNull List<@NotNull Data> elements = new DataList<>(type());

      private final @NotNull Val.Builder.Raw value = new Val.Builder.Raw.DatumVal(this);

      public Raw(ListType type) { super(type); }

      @Override
      public @NotNull List<@NotNull Data> elements() { return elements; }

      @Override
      public int size() { return elements.size(); }

      // TODO add mut methods here

      @Override
      public @NotNull ListDatum.Imm.Raw toImmutable() { return new ListDatum.Imm.Raw(this); }

      @Override
      public @NotNull ListDatum.Builder.Raw _raw() { return this; }

      @Override
      public @NotNull Val.Builder.Raw asValue() { return value; }

      @Override
      public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListDatum)) return false;
        ListDatum that = (ListDatum) o;
        return type().equals(that.type()) && elements.equals(that._raw().elements());
      }

      @Override
      public final int hashCode() { return Objects.hash(type(), elements); }


      private static class DataList<E extends Data> extends AbstractList<E> implements RandomAccess {

        private final @NotNull List<@NotNull E> list = new ArrayList<>();

        private final @NotNull ListType listType;

        public DataList(@NotNull ListType listType) { this.listType = listType; }

        @Override
        public E get(int index) { return list.get(index); }

        @Override
        public int size() { return list.size(); }

        @Override
        public E set(int index, E element) { return list.set(index, validate(element)); }

        @Override
        public void add(int index, E element) { list.add(index, validate(element)); }

        @Override
        public E remove(int index) { return list.remove(index); }

        private E validate(E element) throws IllegalArgumentException {
          return listType.elementType.checkAssignable(element);
        }

      }


    }


    public static abstract class Static<
        MyImmDatum extends ListDatum.Imm.Static,
        MyBuilderVal extends Val.Builder.Static
        > extends ListDatum.Builder implements ListDatum.Static, Datum.Builder.Static<MyImmDatum> {

      private final @NotNull ListDatum.Builder.Raw raw;

      private final @NotNull MyBuilderVal value;

      private final @NotNull Function<ListDatum.Imm.Raw, MyImmDatum> immutableConstructor;

      protected Static(
          @NotNull ListType type,
          @NotNull ListDatum.Builder.Raw raw,
          @NotNull Function<ListDatum.Imm.Raw, MyImmDatum> immutableConstructor,
          @NotNull Function<Val.Builder.@NotNull Raw, @NotNull MyBuilderVal> builderValConstructor
      ) {
        super(type); // TODO take static type separately?
        if (raw.type() != type) // TODO shared assertEqual(Type, Type): Type method
          throw new IllegalArgumentException( // TODO move mut and imm checks to shared static methods
              "Incompatible raw and static types (TODO details)"
          );
        this.raw = raw; // TODO validate raw data is kosher?
        this.value = builderValConstructor.apply(new Val.Builder.Raw.DatumVal(this));
        this.immutableConstructor = immutableConstructor;
      }

      @Override
      public int size() { return raw.size(); }

      @Override
      public @NotNull MyImmDatum toImmutable() { return immutableConstructor.apply(_raw().toImmutable()); }

      @Override
      public final @NotNull ListDatum.Builder.Raw _raw() { return raw; }

      @Override
      public @NotNull MyBuilderVal asValue() { return value; }

      @Override
      public final int hashCode() { return raw.hashCode(); }

      @Override
      public final boolean equals(Object obj) { return raw.equals(obj); }

    }


  }


}