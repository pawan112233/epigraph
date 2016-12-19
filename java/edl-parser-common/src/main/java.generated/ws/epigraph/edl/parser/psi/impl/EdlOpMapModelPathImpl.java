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

public class EdlOpMapModelPathImpl extends ASTWrapperPsiElement implements EdlOpMapModelPath {

  public EdlOpMapModelPathImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull EdlVisitor visitor) {
    visitor.visitOpMapModelPath(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof EdlVisitor) accept((EdlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public EdlOpPathKeyProjection getOpPathKeyProjection() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, EdlOpPathKeyProjection.class));
  }

  @Override
  @Nullable
  public EdlOpVarPath getOpVarPath() {
    return PsiTreeUtil.getChildOfType(this, EdlOpVarPath.class);
  }

  @Override
  @NotNull
  public PsiElement getSlash() {
    return notNullChild(findChildByType(S_SLASH));
  }

}
