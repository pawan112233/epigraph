// This is a generated file. Not intended for manual editing.
package com.sumologic.epigraph.schema.parser.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;

public interface SchemaVarTagRef extends PsiNameIdentifierOwner {

  @NotNull
  PsiElement getId();

  PsiElement setName(String name);

  @Nullable
  PsiElement getNameIdentifier();

  @Nullable
  PsiReference getReference();

}
