// This is a generated file. Not intended for manual editing.
package io.epigraph.lang.parser.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface EpigraphMapTypeBody extends CustomParamsHolder {

  @NotNull
  List<EpigraphCustomParam> getCustomParamList();

  @NotNull
  PsiElement getCurlyLeft();

  @Nullable
  PsiElement getCurlyRight();

}