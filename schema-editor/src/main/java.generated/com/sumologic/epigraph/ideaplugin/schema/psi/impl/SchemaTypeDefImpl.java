// This is a generated file. Not intended for manual editing.
package com.sumologic.epigraph.ideaplugin.schema.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.sumologic.epigraph.ideaplugin.schema.psi.*;

public class SchemaTypeDefImpl extends TypeDefSchemaElementImpl implements SchemaTypeDef {

  public SchemaTypeDefImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull SchemaVisitor visitor) {
    visitor.visitTypeDef(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof SchemaVisitor) accept((SchemaVisitor)visitor);
    else super.accept(visitor);
  }

  @Nullable
  public String getName() {
    return SchemaPsiImplUtil.getName(this);
  }

  @Nullable
  public PsiElement setName(String name) {
    return SchemaPsiImplUtil.setName(this, name);
  }

  @Nullable
  public PsiElement getNameIdentifier() {
    return SchemaPsiImplUtil.getNameIdentifier(this);
  }

  public int getTextOffset() {
    return SchemaPsiImplUtil.getTextOffset(this);
  }

}