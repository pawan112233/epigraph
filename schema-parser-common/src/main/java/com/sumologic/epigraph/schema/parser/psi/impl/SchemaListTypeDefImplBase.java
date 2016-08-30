package com.sumologic.epigraph.schema.parser.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.sumologic.epigraph.schema.parser.psi.SchemaListTypeDef;
import com.sumologic.epigraph.schema.parser.psi.SchemaTypeDef;
import com.sumologic.epigraph.schema.parser.psi.TypeKind;
import com.sumologic.epigraph.schema.parser.psi.stubs.SchemaListTypeDefStub;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
abstract public class SchemaListTypeDefImplBase extends SchemaTypeDefImplBase<SchemaListTypeDefStub, SchemaListTypeDef> implements SchemaTypeDef {
  SchemaListTypeDefImplBase(@NotNull ASTNode node) {
    super(node);
  }

  SchemaListTypeDefImplBase(@NotNull SchemaListTypeDefStub stub, @NotNull IStubElementType nodeType) {
    super(stub, nodeType);
  }

  @NotNull
  @Override
  public TypeKind getKind() {
    return TypeKind.LIST;
  }
}
