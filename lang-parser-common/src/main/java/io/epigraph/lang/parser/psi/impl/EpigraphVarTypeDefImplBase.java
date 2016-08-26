package io.epigraph.lang.parser.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import io.epigraph.lang.parser.psi.EpigraphTypeDef;
import io.epigraph.lang.parser.psi.EpigraphVarTypeDef;
import io.epigraph.lang.parser.psi.TypeKind;
import io.epigraph.lang.parser.psi.stubs.EpigraphVarTypeDefStub;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
abstract public class EpigraphVarTypeDefImplBase extends EpigraphTypeDefImplBase<EpigraphVarTypeDefStub, EpigraphVarTypeDef> implements EpigraphTypeDef {
  EpigraphVarTypeDefImplBase(@NotNull ASTNode node) {
    super(node);
  }

  EpigraphVarTypeDefImplBase(@NotNull EpigraphVarTypeDefStub stub, @NotNull IStubElementType nodeType) {
    super(stub, nodeType);
  }

  @NotNull
  @Override
  public TypeKind getKind() {
    return TypeKind.VAR;
  }
}