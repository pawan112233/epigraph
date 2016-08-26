// This is a generated file. Not intended for manual editing.
package io.epigraph.lang.parser.psi.impl;

import io.epigraph.lang.parser.psi.stubs.EpigraphListTypeDefStub;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static io.epigraph.lang.lexer.EpigraphElementTypes.*;
import io.epigraph.lang.parser.psi.*;
import com.intellij.psi.stubs.IStubElementType;

public class EpigraphListTypeDefImpl extends EpigraphListTypeDefImplBase implements EpigraphListTypeDef {

  public EpigraphListTypeDefImpl(ASTNode node) {
    super(node);
  }

  public EpigraphListTypeDefImpl(EpigraphListTypeDefStub stub, IStubElementType nodeType) {
    super(stub, nodeType);
  }

  public void accept(@NotNull SchemaVisitor visitor) {
    visitor.visitListTypeDef(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof SchemaVisitor) accept((SchemaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public SchemaAnonList getAnonList() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, SchemaAnonList.class));
  }

  @Override
  @Nullable
  public SchemaExtendsDecl getExtendsDecl() {
    return PsiTreeUtil.getChildOfType(this, SchemaExtendsDecl.class);
  }

  @Override
  @Nullable
  public SchemaListTypeBody getListTypeBody() {
    return PsiTreeUtil.getChildOfType(this, SchemaListTypeBody.class);
  }

  @Override
  @Nullable
  public SchemaMetaDecl getMetaDecl() {
    return PsiTreeUtil.getChildOfType(this, SchemaMetaDecl.class);
  }

  @Override
  @Nullable
  public SchemaQid getQid() {
    return PsiTreeUtil.getChildOfType(this, SchemaQid.class);
  }

  @Override
  @Nullable
  public SchemaSupplementsDecl getSupplementsDecl() {
    return PsiTreeUtil.getChildOfType(this, SchemaSupplementsDecl.class);
  }

  @Override
  @Nullable
  public PsiElement getAbstract() {
    return findChildByType(E_ABSTRACT);
  }

}
