// This is a generated file. Not intended for manual editing.
package com.sumologic.epigraph.schema.parser.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.sumologic.epigraph.schema.parser.lexer.SchemaElementTypes.*;
import com.sumologic.epigraph.schema.parser.psi.*;

public class SchemaRecordTypeBodyImpl extends CustomParamHolderImpl implements SchemaRecordTypeBody {

  public SchemaRecordTypeBodyImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull SchemaVisitor visitor) {
    visitor.visitRecordTypeBody(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof SchemaVisitor) accept((SchemaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<SchemaCustomParam> getCustomParamList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, SchemaCustomParam.class);
  }

  @Override
  @NotNull
  public List<SchemaFieldDecl> getFieldDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, SchemaFieldDecl.class);
  }

  @Override
  @NotNull
  public PsiElement getCurlyLeft() {
    return notNullChild(findChildByType(S_CURLY_LEFT));
  }

  @Override
  @Nullable
  public PsiElement getCurlyRight() {
    return findChildByType(S_CURLY_RIGHT);
  }

}
