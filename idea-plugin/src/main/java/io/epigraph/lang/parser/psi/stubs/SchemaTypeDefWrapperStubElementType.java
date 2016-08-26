package io.epigraph.lang.parser.psi.stubs;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.*;
import io.epigraph.lang.schema.SchemaLanguage;
import io.epigraph.lang.parser.psi.SchemaTypeDefWrapper;
import io.epigraph.lang.parser.psi.impl.SchemaTypeDefWrapperImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
public class SchemaTypeDefWrapperStubElementType extends IStubElementType<EpigraphTypeDefWrapperStub, SchemaTypeDefWrapper> {
  public SchemaTypeDefWrapperStubElementType(@NotNull @NonNls String debugName) {
    super(debugName, SchemaLanguage.INSTANCE);
  }

  @Override
  public boolean shouldCreateStub(ASTNode node) {
    return false;
  }

  @Override
  public SchemaTypeDefWrapper createPsi(@NotNull EpigraphTypeDefWrapperStub stub) {
    return new SchemaTypeDefWrapperImpl(stub, this);
  }

  @Override
  public EpigraphTypeDefWrapperStub createStub(@NotNull SchemaTypeDefWrapper typeDef, StubElement parentStub) {
    return new EpigraphTypeDefWrapperStubImpl(parentStub);
  }

  @NotNull
  @Override
  public String getExternalId() {
    return "epigraph_schema.typedef";
  }

  @Override
  public void serialize(@NotNull EpigraphTypeDefWrapperStub stub, @NotNull StubOutputStream dataStream) throws IOException {
  }

  @NotNull
  @Override
  public EpigraphTypeDefWrapperStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new EpigraphTypeDefWrapperStubImpl(parentStub);
  }

  @Override
  public void indexStub(@NotNull EpigraphTypeDefWrapperStub stub, @NotNull IndexSink sink) {

  }
}
