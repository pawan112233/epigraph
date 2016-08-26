package io.epigraph.lang.parser.psi.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import io.epigraph.lang.parser.psi.EpigraphEnumTypeDef;
import io.epigraph.lang.lexer.EpigraphElementTypes;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
public class EpigraphEnumTypeDefStubImpl extends EpigraphTypeDefStubBaseImpl<EpigraphEnumTypeDef> implements EpigraphEnumTypeDefStub {
  EpigraphEnumTypeDefStubImpl(StubElement parent) {
    super(parent, (IStubElementType) EpigraphElementTypes.E_ENUM_TYPE_DEF);
  }
}