// This is a generated file. Not intended for manual editing.
package io.epigraph.idl.parser.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static io.epigraph.idl.lexer.IdlElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import io.epigraph.idl.parser.psi.*;

public class IdlOpInputSingleTagProjectionImpl extends ASTWrapperPsiElement implements IdlOpInputSingleTagProjection {

  public IdlOpInputSingleTagProjectionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull IdlVisitor visitor) {
    visitor.visitOpInputSingleTagProjection(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof IdlVisitor) accept((IdlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public IdlOpInputModelProjection getOpInputModelProjection() {
    return findNotNullChildByClass(IdlOpInputModelProjection.class);
  }

  @Override
  @Nullable
  public IdlQid getQid() {
    return findChildByClass(IdlQid.class);
  }

  @Override
  @Nullable
  public PsiElement getColon() {
    return findChildByType(I_COLON);
  }

  @Override
  @Nullable
  public PsiElement getPlus() {
    return findChildByType(I_PLUS);
  }

}
