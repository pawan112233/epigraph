package io.epigraph.lang.parser.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import io.epigraph.lang.parser.psi.EpigraphTypeDef;
import io.epigraph.lang.parser.psi.EpigraphPrimitiveTypeDef;
import io.epigraph.lang.parser.psi.TypeKind;
import io.epigraph.lang.parser.psi.stubs.EpigraphPrimitiveTypeDefStub;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
abstract public class EpigraphPrimitiveTypeDefImplBase extends EpigraphTypeDefImplBase<EpigraphPrimitiveTypeDefStub, EpigraphPrimitiveTypeDef> implements EpigraphTypeDef {
  EpigraphPrimitiveTypeDefImplBase(@NotNull ASTNode node) {
    super(node);
  }

  EpigraphPrimitiveTypeDefImplBase(@NotNull EpigraphPrimitiveTypeDefStub stub, @NotNull IStubElementType nodeType) {
    super(stub, nodeType);
  }

  @NotNull
  @Override
  public TypeKind getKind() {
    return TypeKind.PRIMITIVE;
  }
}
