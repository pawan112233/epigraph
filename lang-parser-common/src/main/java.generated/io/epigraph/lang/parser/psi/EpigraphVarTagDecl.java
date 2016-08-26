// This is a generated file. Not intended for manual editing.
package io.epigraph.lang.parser.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.navigation.ItemPresentation;

public interface EpigraphVarTagDecl extends CustomParamsHolder, PsiNamedElement {

  @NotNull
  List<EpigraphCustomParam> getCustomParamList();

  @NotNull
  EpigraphQid getQid();

  @Nullable
  EpigraphTypeRef getTypeRef();

  @Nullable
  PsiElement getAbstract();

  @NotNull
  PsiElement getColon();

  @Nullable
  PsiElement getCurlyLeft();

  @Nullable
  PsiElement getCurlyRight();

  @Nullable
  PsiElement getOverride();

  @Nullable
  String getName();

  PsiElement setName(String name);

  @NotNull
  PsiElement getNameIdentifier();

  int getTextOffset();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  EpigraphVarTypeDef getVarTypeDef();

}