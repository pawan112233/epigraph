// This is a generated file. Not intended for manual editing.
package io.epigraph.lang.parser.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface EpigraphOpOutputListMultiPolyBranch extends PsiElement {

  @NotNull
  List<EpigraphOpOutputListSinglePolyBranch> getOpOutputListSinglePolyBranchList();

  @NotNull
  PsiElement getParenLeft();

  @Nullable
  PsiElement getParenRight();

}