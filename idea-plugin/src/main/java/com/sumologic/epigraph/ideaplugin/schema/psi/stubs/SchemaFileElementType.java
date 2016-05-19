package com.sumologic.epigraph.ideaplugin.schema.psi.stubs;

import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBuilder;
import com.intellij.psi.stubs.DefaultStubBuilder;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.util.io.StringRef;
import com.sumologic.epigraph.ideaplugin.schema.SchemaLanguage;
import com.sumologic.epigraph.ideaplugin.schema.brains.NamespaceManager;
import com.sumologic.epigraph.ideaplugin.schema.psi.SchemaFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
public class SchemaFileElementType extends IStubFileElementType<SchemaFileStub> {
  public SchemaFileElementType() {
    super("epigraph_schema.FILE", SchemaLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getExternalId() {
    return "epigraph_schema.FILE";
  }

  @Override
  public StubBuilder getBuilder() {
    return new DefaultStubBuilder() {
      @NotNull
      @Override
      protected StubElement createStubForFile(@NotNull PsiFile file) {
        if (file instanceof SchemaFile) {
          SchemaFile schemaFile = (SchemaFile) file;
          String namespace = NamespaceManager.getNamespace(schemaFile);
          return new SchemaFileStubImpl(schemaFile, StringRef.fromNullableString(namespace));
        } else return super.createStubForFile(file);
      }
    };
  }

  @Override
  public void serialize(@NotNull SchemaFileStub stub, @NotNull StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getNamespace());
  }

  @NotNull
  @Override
  public SchemaFileStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
    StringRef namespace = dataStream.readName();
    return new SchemaFileStubImpl(null, namespace);
  }

  // TODO indexStub by namespace?
}
