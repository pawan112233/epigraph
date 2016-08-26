// This is a generated file. Not intended for manual editing.
package io.epigraph.lang.parser.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static io.epigraph.lang.lexer.EpigraphElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import io.epigraph.lang.parser.psi.*;

public class EpigraphDataValueImpl extends ASTWrapperPsiElement implements EpigraphDataValue {

  public EpigraphDataValueImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull EpigraphVisitor visitor) {
    visitor.visitDataValue(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof EpigraphVisitor) accept((EpigraphVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public EpigraphDataPrimitive getDataPrimitive() {
    return PsiTreeUtil.getChildOfType(this, EpigraphDataPrimitive.class);
  }

  @Override
  @Nullable
  public EpigraphDataValue getDataValue() {
    return PsiTreeUtil.getChildOfType(this, EpigraphDataValue.class);
  }

  @Override
  @NotNull
  public List<EpigraphFqnTypeRef> getFqnTypeRefList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, EpigraphFqnTypeRef.class);
  }

  @Override
  @Nullable
  public PsiElement getNull() {
    return findChildByType(E_NULL);
  }

}