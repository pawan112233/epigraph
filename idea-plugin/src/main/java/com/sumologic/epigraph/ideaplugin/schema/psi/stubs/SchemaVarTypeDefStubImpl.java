package com.sumologic.epigraph.ideaplugin.schema.psi.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.sumologic.epigraph.ideaplugin.schema.lexer.SchemaElementTypes;
import com.sumologic.epigraph.ideaplugin.schema.psi.SchemaVarTypeDef;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
public class SchemaVarTypeDefStubImpl extends SchemaTypeDefStubBaseImpl<SchemaVarTypeDef> implements SchemaVarTypeDefStub {
  SchemaVarTypeDefStubImpl(StubElement parent,
                           String name,
                           String namespace,
                           @Nullable final List<SerializedFqnTypeRef> extendsTypeRefs) {
    super(parent, (IStubElementType) SchemaElementTypes.S_VAR_TYPE_DEF, name, namespace, extendsTypeRefs);
  }
}
