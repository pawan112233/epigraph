// This is a generated file. Not intended for manual editing.
package io.epigraph.lang.parser.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface EpigraphAnonList extends EpigraphTypeRef {

  @Nullable
  EpigraphValueTypeRef getValueTypeRef();

  @Nullable
  PsiElement getBracketLeft();

  @Nullable
  PsiElement getBracketRight();

  @NotNull
  PsiElement getList();

}