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

public class UrlReqInputMultiTagProjectionImpl extends ASTWrapperPsiElement implements UrlReqInputMultiTagProjection {

  public UrlReqInputMultiTagProjectionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull UrlVisitor visitor) {
    visitor.visitReqInputMultiTagProjection(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof UrlVisitor) accept((UrlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<UrlReqInputMultiTagProjectionItem> getReqInputMultiTagProjectionItemList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, UrlReqInputMultiTagProjectionItem.class);
  }

  @Override
  @NotNull
  public PsiElement getColon() {
    return findNotNullChildByType(U_COLON);
  }

  @Override
  @NotNull
  public PsiElement getParenLeft() {
    return findNotNullChildByType(U_PAREN_LEFT);
  }

  @Override
  @Nullable
  public PsiElement getParenRight() {
    return findChildByType(U_PAREN_RIGHT);
  }

}
