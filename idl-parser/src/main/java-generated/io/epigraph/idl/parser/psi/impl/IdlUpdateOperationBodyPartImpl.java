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

public class IdlUpdateOperationBodyPartImpl extends ASTWrapperPsiElement implements IdlUpdateOperationBodyPart {

  public IdlUpdateOperationBodyPartImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull IdlVisitor visitor) {
    visitor.visitUpdateOperationBodyPart(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof IdlVisitor) accept((IdlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public IdlAnnotation getAnnotation() {
    return findChildByClass(IdlAnnotation.class);
  }

  @Override
  @Nullable
  public IdlOpParam getOpParam() {
    return findChildByClass(IdlOpParam.class);
  }

  @Override
  @Nullable
  public IdlOperationInput getOperationInput() {
    return findChildByClass(IdlOperationInput.class);
  }

  @Override
  @Nullable
  public IdlOperationOutput getOperationOutput() {
    return findChildByClass(IdlOperationOutput.class);
  }

}