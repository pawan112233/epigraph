// This is a generated file. Not intended for manual editing.
package ws.epigraph.edl.parser.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static ws.epigraph.edl.lexer.EdlElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import ws.epigraph.edl.parser.psi.*;

public class EdlOpOutputModelPropertyImpl extends ASTWrapperPsiElement implements EdlOpOutputModelProperty {

  public EdlOpOutputModelPropertyImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull EdlVisitor visitor) {
    visitor.visitOpOutputModelProperty(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof EdlVisitor) accept((EdlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public EdlAnnotation getAnnotation() {
    return PsiTreeUtil.getChildOfType(this, EdlAnnotation.class);
  }

  @Override
  @Nullable
  public EdlOpOutputModelMeta getOpOutputModelMeta() {
    return PsiTreeUtil.getChildOfType(this, EdlOpOutputModelMeta.class);
  }

  @Override
  @Nullable
  public EdlOpParam getOpParam() {
    return PsiTreeUtil.getChildOfType(this, EdlOpParam.class);
  }

}
