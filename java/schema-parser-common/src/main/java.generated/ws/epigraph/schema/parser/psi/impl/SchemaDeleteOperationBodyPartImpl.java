// This is a generated file. Not intended for manual editing.
package ws.epigraph.schema.parser.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static ws.epigraph.schema.lexer.SchemaElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import ws.epigraph.schema.parser.psi.*;

public class SchemaDeleteOperationBodyPartImpl extends ASTWrapperPsiElement implements SchemaDeleteOperationBodyPart {

  public SchemaDeleteOperationBodyPartImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull SchemaVisitor visitor) {
    visitor.visitDeleteOperationBodyPart(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof SchemaVisitor) accept((SchemaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public SchemaAnnotation getAnnotation() {
    return PsiTreeUtil.getChildOfType(this, SchemaAnnotation.class);
  }

  @Override
  @Nullable
  public SchemaOperationDeleteProjection getOperationDeleteProjection() {
    return PsiTreeUtil.getChildOfType(this, SchemaOperationDeleteProjection.class);
  }

  @Override
  @Nullable
  public SchemaOperationOutputProjection getOperationOutputProjection() {
    return PsiTreeUtil.getChildOfType(this, SchemaOperationOutputProjection.class);
  }

  @Override
  @Nullable
  public SchemaOperationOutputType getOperationOutputType() {
    return PsiTreeUtil.getChildOfType(this, SchemaOperationOutputType.class);
  }

  @Override
  @Nullable
  public SchemaOperationPath getOperationPath() {
    return PsiTreeUtil.getChildOfType(this, SchemaOperationPath.class);
  }

}