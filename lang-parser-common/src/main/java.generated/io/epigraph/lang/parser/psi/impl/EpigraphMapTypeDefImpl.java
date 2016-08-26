// This is a generated file. Not intended for manual editing.
package io.epigraph.lang.parser.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static io.epigraph.lang.lexer.EpigraphElementTypes.*;
import io.epigraph.lang.parser.psi.*;
import io.epigraph.lang.parser.psi.stubs.EpigraphMapTypeDefStub;
import com.intellij.psi.stubs.IStubElementType;

public class EpigraphMapTypeDefImpl extends EpigraphMapTypeDefImplBase implements EpigraphMapTypeDef {

  public EpigraphMapTypeDefImpl(ASTNode node) {
    super(node);
  }

  public EpigraphMapTypeDefImpl(EpigraphMapTypeDefStub stub, IStubElementType nodeType) {
    super(stub, nodeType);
  }

  public void accept(@NotNull SchemaVisitor visitor) {
    visitor.visitMapTypeDef(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof SchemaVisitor) accept((SchemaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public SchemaAnonMap getAnonMap() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, SchemaAnonMap.class));
  }

  @Override
  @Nullable
  public SchemaExtendsDecl getExtendsDecl() {
    return PsiTreeUtil.getChildOfType(this, SchemaExtendsDecl.class);
  }

  @Override
  @Nullable
  public SchemaMapTypeBody getMapTypeBody() {
    return PsiTreeUtil.getChildOfType(this, SchemaMapTypeBody.class);
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
