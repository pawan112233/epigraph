package io.epigraph.lang.parser.psi.stubs;

import com.intellij.psi.stubs.*;
import io.epigraph.lang.schema.SchemaLanguage;
import io.epigraph.lang.parser.psi.EpigraphNamespaceDecl;
import io.epigraph.lang.parser.psi.impl.EpigraphNamespaceDeclImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
public class EpigraphNamespaceDeclStubElementType extends IStubElementType<EpigraphNamespaceDeclStub, EpigraphNamespaceDecl> {
  public EpigraphNamespaceDeclStubElementType(@NotNull @NonNls String debugName) {
    super(debugName, SchemaLanguage.INSTANCE);
  }

  @Override
  public EpigraphNamespaceDecl createPsi(@NotNull EpigraphNamespaceDeclStub stub) {
    return new EpigraphNamespaceDeclImpl(stub, this);
  }

  @Override
  public EpigraphNamespaceDeclStub createStub(@NotNull EpigraphNamespaceDecl namespaceDecl, StubElement parentStub) {
    return new EpigraphNamespaceDeclStubImpl(parentStub, namespaceDecl.getFqn2());
  }

  @NotNull
  @Override
  public String getExternalId() {
    return "epigraph.namespace";
  }

  @Override
  public void serialize(@NotNull EpigraphNamespaceDeclStub stub, @NotNull StubOutputStream dataStream) throws IOException {
  }

  @NotNull
  @Override
  public EpigraphNamespaceDeclStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void indexStub(@NotNull EpigraphNamespaceDeclStub stub, @NotNull IndexSink sink) {
  }
}