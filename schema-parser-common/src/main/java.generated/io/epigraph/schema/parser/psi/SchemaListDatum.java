// This is a generated file. Not intended for manual editing.
package io.epigraph.schema.parser.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface SchemaListDatum extends SchemaDatum {

  @NotNull
  List<SchemaDataValue> getDataValueList();

  @Nullable
  SchemaFqnTypeRef getFqnTypeRef();

  @NotNull
  PsiElement getBracketLeft();

  @Nullable
  PsiElement getBracketRight();

}