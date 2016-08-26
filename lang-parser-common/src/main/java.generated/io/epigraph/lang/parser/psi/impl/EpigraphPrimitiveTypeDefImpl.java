// This is a generated file. Not intended for manual editing.
package io.epigraph.lang.parser.psi.impl;

import io.epigraph.lang.parser.psi.stubs.EpigraphPrimitiveTypeDefStub;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static io.epigraph.lang.lexer.EpigraphElementTypes.*;
import io.epigraph.lang.parser.psi.*;
import com.intellij.psi.stubs.IStubElementType;

public class EpigraphPrimitiveTypeDefImpl extends EpigraphPrimitiveTypeDefImplBase implements EpigraphPrimitiveTypeDef {

  public EpigraphPrimitiveTypeDefImpl(ASTNode node) {
    super(node);
  }

  public EpigraphPrimitiveTypeDefImpl(EpigraphPrimitiveTypeDefStub stub, IStubElementType nodeType) {
    super(stub, nodeType);
  }

  public void accept(@NotNull SchemaVisitor visitor) {
    visitor.visitPrimitiveTypeDef(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof SchemaVisitor) accept((SchemaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public SchemaExtendsDecl getExtendsDecl() {
    return PsiTreeUtil.getChildOfType(this, SchemaExtendsDecl.class);
  }

  @Override
  @Nullable
  public SchemaMetaDecl getMetaDecl() {
    return PsiTreeUtil.getChildOfType(this, SchemaMetaDecl.class);
  }

  @Override
  @Nullable
  public SchemaPrimitiveTypeBody getPrimitiveTypeBody() {
    return PsiTreeUtil.getChildOfType(this, SchemaPrimitiveTypeBody.class);
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

  @Override
  @Nullable
  public PsiElement getBooleanT() {
    return findChildByType(E_BOOLEAN_T);
  }

  @Override
  @Nullable
  public PsiElement getDoubleT() {
    return findChildByType(E_DOUBLE_T);
  }

  @Override
  @Nullable
  public PsiElement getIntegerT() {
    return findChildByType(E_INTEGER_T);
  }

  @Override
  @Nullable
  public PsiElement getLongT() {
    return findChildByType(E_LONG_T);
  }

  @Override
  @Nullable
  public PsiElement getStringT() {
    return findChildByType(E_STRING_T);
  }

  @NotNull
  public PrimitiveTypeKind getPrimitiveTypeKind() {
    return EpigraphPsiImplUtil.getPrimitiveTypeKind(this);
  }

}
