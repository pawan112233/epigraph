// This is a generated file. Not intended for manual editing.
package io.epigraph.idl.parser.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface IdlReqOutputComaSingleTagProjection extends PsiElement {

  @NotNull
  List<IdlReqAnnotation> getReqAnnotationList();

  @NotNull
  IdlReqOutputComaModelProjection getReqOutputComaModelProjection();

  @Nullable
  IdlReqOutputModelMeta getReqOutputModelMeta();

  @NotNull
  List<IdlReqParam> getReqParamList();

  @Nullable
  IdlTagName getTagName();

  @Nullable
  PsiElement getColon();

  @Nullable
  PsiElement getPlus();

}