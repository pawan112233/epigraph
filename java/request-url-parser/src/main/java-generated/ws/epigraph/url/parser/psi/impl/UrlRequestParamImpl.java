// This is a generated file. Not intended for manual editing.
package ws.epigraph.url.parser.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static ws.epigraph.url.lexer.UrlElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import ws.epigraph.url.parser.psi.*;

public class UrlRequestParamImpl extends ASTWrapperPsiElement implements UrlRequestParam {

  public UrlRequestParamImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull UrlVisitor visitor) {
    visitor.visitRequestParam(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof UrlVisitor) accept((UrlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public UrlDatum getDatum() {
    return findChildByClass(UrlDatum.class);
  }

  @Override
  @Nullable
  public PsiElement getAmp() {
    return findChildByType(U_AMP);
  }

  @Override
  @Nullable
  public PsiElement getEq() {
    return findChildByType(U_EQ);
  }

  @Override
  @Nullable
  public PsiElement getParamName() {
    return findChildByType(U_PARAM_NAME);
  }

  @Override
  @Nullable
  public PsiElement getQmark() {
    return findChildByType(U_QMARK);
  }

}
