package com.sumologic.epigraph.ideaplugin.schema.psi.stubs;

import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.util.io.StringRef;
import com.sumologic.epigraph.ideaplugin.schema.psi.SchemaFile;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
public class SchemaFileStubImpl extends PsiFileStubImpl<SchemaFile> implements SchemaFileStub {
  private final StringRef namespace;

  public SchemaFileStubImpl(SchemaFile file, StringRef namespace) {
    super(file);
    this.namespace = namespace;
  }

  @Override
  public String getNamespace() {
    return StringRef.toString(namespace);
  }

  @Override
  public IStubFileElementType getType() {
    return SchemaStubElementTypes.SCHEMA_FILE;
  }
}
