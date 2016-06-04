package com.sumologic.epigraph.ideaplugin.schema.psi.stubs;

import com.intellij.psi.stubs.NamedStub;
import com.sumologic.epigraph.ideaplugin.schema.psi.SchemaTypeDef;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
public interface SchemaTypeDefStubBase<T extends SchemaTypeDef> extends NamedStub<T> {
  @Nullable
  String getNamespace();

  @Nullable
  List<SerializedFqnTypeRef> getExtendsTypeRefs();
}