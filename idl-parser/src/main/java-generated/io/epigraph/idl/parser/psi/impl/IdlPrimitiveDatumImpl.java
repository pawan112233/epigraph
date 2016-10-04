// This is a generated file. Not intended for manual editing.
package io.epigraph.idl.parser.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static io.epigraph.idl.lexer.IdlElementTypes.*;
import io.epigraph.idl.parser.psi.*;

public class IdlPrimitiveDatumImpl extends IdlDatumImpl implements IdlPrimitiveDatum {

  public IdlPrimitiveDatumImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull IdlVisitor visitor) {
    visitor.visitPrimitiveDatum(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof IdlVisitor) accept((IdlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public IdlTypeRef getTypeRef() {
    return findChildByClass(IdlTypeRef.class);
  }

  @Override
  @Nullable
  public PsiElement getAt() {
    return findChildByType(I_AT);
  }

  @Override
  @Nullable
  public PsiElement getBoolean() {
    return findChildByType(I_BOOLEAN);
  }

  @Override
  @Nullable
  public PsiElement getNumber() {
    return findChildByType(I_NUMBER);
  }

  @Override
  @Nullable
  public PsiElement getString() {
    return findChildByType(I_STRING);
  }

}
