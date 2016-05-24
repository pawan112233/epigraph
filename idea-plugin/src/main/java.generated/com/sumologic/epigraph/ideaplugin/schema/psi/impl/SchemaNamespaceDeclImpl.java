// This is a generated file. Not intended for manual editing.
package com.sumologic.epigraph.ideaplugin.schema.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.sumologic.epigraph.ideaplugin.schema.lexer.SchemaElementTypes.*;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.sumologic.epigraph.ideaplugin.schema.psi.stubs.SchemaNamespaceDeclStub;
import com.sumologic.epigraph.ideaplugin.schema.psi.*;
import com.sumologic.epigraph.ideaplugin.schema.brains.Fqn;
import com.intellij.psi.stubs.IStubElementType;

public class SchemaNamespaceDeclImpl extends StubBasedPsiElementBase<SchemaNamespaceDeclStub> implements SchemaNamespaceDecl {

  public SchemaNamespaceDeclImpl(ASTNode node) {
    super(node);
  }

  public SchemaNamespaceDeclImpl(SchemaNamespaceDeclStub stub, IStubElementType nodeType) {
    super(stub, nodeType);
  }

  public void accept(@NotNull SchemaVisitor visitor) {
    visitor.visitNamespaceDecl(this);
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
  @Nullable
  public SchemaFqn getFqn() {
    return findChildByClass(SchemaFqn.class);
  }

  @Override
  @Nullable
  public PsiElement getCurlyLeft() {
    return findChildByType(S_CURLY_LEFT);
  }

  @Override
  @Nullable
  public PsiElement getCurlyRight() {
    return findChildByType(S_CURLY_RIGHT);
  }

  @Override
  @NotNull
  public PsiElement getNamespace() {
    return findNotNullChildByType(S_NAMESPACE);
  }

  @Nullable
  public Fqn getFqn2() {
    return SchemaPsiImplUtil.getFqn2(this);
  }

  @NotNull
  public String toString() {
    return SchemaPsiImplUtil.toString(this);
  }

}
