package io.epigraph.lang.parser.psi.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import io.epigraph.lang.parser.psi.EpigraphListTypeDef;
import io.epigraph.lang.lexer.EpigraphElementTypes;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
public class EpigraphListTypeDefStubImpl extends EpigraphTypeDefStubBaseImpl<EpigraphListTypeDef> implements EpigraphListTypeDefStub {
  EpigraphListTypeDefStubImpl(StubElement parent) {
    super(parent, (IStubElementType) EpigraphElementTypes.E_LIST_TYPE_DEF);
  }
}