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

public class UrlMapDatumEntryImpl extends ASTWrapperPsiElement implements UrlMapDatumEntry {

  public UrlMapDatumEntryImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull UrlVisitor visitor) {
    visitor.visitMapDatumEntry(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof UrlVisitor) accept((UrlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public UrlDataValue getDataValue() {
    return findChildByClass(UrlDataValue.class);
  }

  @Override
  @NotNull
  public UrlDatum getDatum() {
    return findNotNullChildByClass(UrlDatum.class);
  }

  @Override
  @NotNull
  public PsiElement getColon() {
    return findNotNullChildByType(U_COLON);
  }

  @Override
  @Nullable
  public PsiElement getComma() {
    return findChildByType(U_COMMA);
  }

}
