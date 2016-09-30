package com.sumologic.epigraph.ideaplugin.schema.features.structureview;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import io.epigraph.schema.parser.psi.*;
import com.sumologic.epigraph.ideaplugin.schema.presentation.SchemaPresentationUtil;
import com.sumologic.epigraph.ideaplugin.schema.presentation.StaticItemPresentation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
class SchemaStructureViewElement implements StructureViewTreeElement {
  private final NavigatablePsiElement element;

  SchemaStructureViewElement(PsiElement element) {
    this.element = (NavigatablePsiElement) element;
  }

  @Override
  public Object getValue() {
    return element;
  }

  @NotNull
  @Override
  public ItemPresentation getPresentation() {
    ItemPresentation presentation = SchemaPresentationUtil.getPresentation(element, true);
    // remove location
    return new StaticItemPresentation(
        presentation.getPresentableText(),
        null,
        presentation.getIcon(false)
    );
  }

  @NotNull
  @Override
  public TreeElement[] getChildren() {
    final Collection<PsiElement> children = new ArrayList<>();

    if (element instanceof AnnotationsHolder) {
      children.addAll(((AnnotationsHolder) element).getAnnotationsList());
    }

    if (element instanceof SchemaFile) {
      SchemaDefs defs = ((SchemaFile) element).getDefs();
      if (defs != null) {
        children.addAll(defs.getTypeDefWrapperList().stream().map(SchemaTypeDefWrapper::getElement).collect(Collectors.toList()));
        children.addAll(defs.getSupplementDefList()); // SchemaSupplementDef
      }
    } else if (element instanceof SchemaRecordTypeDef) {
      SchemaRecordTypeBody recordTypeBody = ((SchemaRecordTypeDef) element).getRecordTypeBody();
      if (recordTypeBody != null) {
        children.addAll(recordTypeBody.getAnnotationsList()); // SchemaCustomParam
        children.addAll(recordTypeBody.getFieldDeclList()); // SchemaFieldDecl
      }
    } else if (element instanceof SchemaVarTypeDef) {
      SchemaVarTypeBody varTypeBody = ((SchemaVarTypeDef) element).getVarTypeBody();
      if (varTypeBody != null) {
        children.addAll(varTypeBody.getAnnotationsList());
        children.addAll(varTypeBody.getVarTagDeclList()); // SchemaVarTagDecl
      }
    } else if (element instanceof SchemaEnumTypeDef) {
      SchemaEnumTypeBody enumTypeBody = ((SchemaEnumTypeDef) element).getEnumTypeBody();
      if (enumTypeBody != null) {
        children.addAll(enumTypeBody.getAnnotationsList());
        children.addAll(enumTypeBody.getEnumMemberDeclList()); // SchemaEnumMemberDecl
      }
    } else if (element instanceof SchemaMapTypeDef) {
      SchemaMapTypeBody mapTypeBody = ((SchemaMapTypeDef) element).getMapTypeBody();
      if (mapTypeBody != null) children.addAll(mapTypeBody.getAnnotationsList());
    } else if (element instanceof SchemaListTypeDef) {
      SchemaListTypeBody listTypeBody = ((SchemaListTypeDef) element).getListTypeBody();
      if (listTypeBody != null) children.addAll(listTypeBody.getAnnotationsList());
    } else if (element instanceof SchemaPrimitiveTypeDef) {
      SchemaPrimitiveTypeBody primitiveTypeBody = ((SchemaPrimitiveTypeDef) element).getPrimitiveTypeBody();
      if (primitiveTypeBody != null) children.addAll(primitiveTypeBody.getAnnotationsList());
    }

    if (children.isEmpty()) return StructureViewTreeElement.EMPTY_ARRAY;

    return children.stream().map(SchemaStructureViewElement::new).toArray(TreeElement[]::new);
  }

  boolean isAlwaysLeaf() {
    return getChildren().length == 0; // too expensive?
  }

  boolean isAutoExpand() {
    return element instanceof SchemaFile;
  }

  @Override
  public void navigate(boolean requestFocus) {
    element.navigate(requestFocus);
  }

  @Override
  public boolean canNavigate() {
    return element.canNavigate();
  }

  @Override
  public boolean canNavigateToSource() {
    return element.canNavigateToSource();
  }


}
