// This is a generated file. Not intended for manual editing.
package io.epigraph.url.parser.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface UrlReqOutputTrunkSingleTagProjection extends PsiElement {

  @NotNull
  List<UrlReqAnnotation> getReqAnnotationList();

  @Nullable
  UrlReqOutputModelMeta getReqOutputModelMeta();

  @NotNull
  UrlReqOutputTrunkModelProjection getReqOutputTrunkModelProjection();

  @NotNull
  List<UrlReqParam> getReqParamList();

  @Nullable
  UrlTagName getTagName();

  @Nullable
  PsiElement getColon();

  @Nullable
  PsiElement getPlus();

}