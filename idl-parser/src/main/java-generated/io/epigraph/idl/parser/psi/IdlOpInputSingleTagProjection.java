// This is a generated file. Not intended for manual editing.
package io.epigraph.idl.parser.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface IdlOpInputSingleTagProjection extends PsiElement {

  @NotNull
  IdlOpInputModelProjection getOpInputModelProjection();

  @Nullable
  IdlQid getQid();

  @Nullable
  PsiElement getColon();

  @Nullable
  PsiElement getPlus();

}