package com.sumologic.epigraph.ideaplugin.schema.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.TokenType;
import com.intellij.util.IncorrectOperationException;
import com.sumologic.epigraph.ideaplugin.schema.brains.Fqn;
import com.sumologic.epigraph.ideaplugin.schema.brains.ReferenceFactory;
import com.sumologic.epigraph.ideaplugin.schema.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
public class SchemaPsiImplUtil {

  @NotNull
  public static Fqn getFqn(SchemaFqn e) {
    List<SchemaFqnSegment> fqnSegmentList = e.getFqnSegmentList();
    String[] segments = new String[fqnSegmentList.size()];
    int idx = 0;

    for (SchemaFqnSegment segment : fqnSegmentList) {
      segments[idx++] = segment.getName();
    }

    return new Fqn(segments);
  }

  @Nullable
  public static String getName(SchemaTypeDef schemaTypeDef) {
    PsiElement id = schemaTypeDef.getId();
    return id == null ? null : id.getText();
  }

  @Nullable
  public static PsiElement setName(SchemaTypeDef schemaTypeDef, String name) {
    PsiElement id = schemaTypeDef.getId();
    if (id == null) return null;
    else {
      PsiElement newId = SchemaElementFactory.createId(schemaTypeDef.getProject(), name);
      id.replace(newId);
      return id;
    }
  }

  @Nullable
  public static PsiElement getNameIdentifier(@NotNull SchemaTypeDef schemaTypeDef) {
    return schemaTypeDef.getId();
  }

  public static int getTextOffset(@NotNull SchemaTypeDef schemaTypeDef) {
    PsiElement nameIdentifier = schemaTypeDef.getNameIdentifier();
    return nameIdentifier == null ? 0 : nameIdentifier.getTextOffset();
  }

  public static void delete(@NotNull SchemaTypeDef schemaTypeDef) throws IncorrectOperationException {
    final ASTNode parentNode = schemaTypeDef.getParent().getNode();
    assert parentNode != null;

    ASTNode node = schemaTypeDef.getNode();
    ASTNode prev = node.getTreePrev();
    ASTNode next = node.getTreeNext();
    parentNode.removeChild(node);
    if ((prev == null || prev.getElementType() == TokenType.WHITE_SPACE) && next != null &&
        next.getElementType() == TokenType.WHITE_SPACE) {
      parentNode.removeChild(next);
    }
  }

//  public static PsiElement setName(SchemaFqnTypeRef fqnTypeRef, String name) {
//    SchemaFqn oldFqn = fqnTypeRef.getFqn();
//    SchemaFqn newFqn = SchemaElementFactory.createFqn(fqnTypeRef.getProject(), name);
//    oldFqn.replace(newFqn);
//    return oldFqn;
//  }

  @Nullable
  public static SchemaFqnTypeRef getFqnTypeRef(SchemaFqnSegment segment) {
    PsiElement fqn = segment.getParent();
    if (fqn instanceof SchemaFqn) {
      PsiElement fqnParent = fqn.getParent();
      if (fqnParent instanceof SchemaFqnTypeRef) {
        return (SchemaFqnTypeRef) fqnParent;
      }
    }

    return null;
  }

  @Nullable
  public static String getName(SchemaFqnSegment segment) {
    return getNameIdentifier(segment).getText();
  }

  public static PsiElement setName(SchemaFqnSegment segment, String name) {
    PsiElement oldId = segment.getId();
    PsiElement newId = SchemaElementFactory.createId(segment.getProject(), name);
    return oldId.replace(newId);
  }

  @NotNull
  public static PsiElement getNameIdentifier(SchemaFqnSegment segment) {
    return segment.getId();
  }

  public static boolean isLast(SchemaFqnSegment segment) {
    PsiElement parent = segment.getParent();
    if (parent instanceof SchemaFqn) {
      SchemaFqn schemaFqn = (SchemaFqn) parent;
      List<SchemaFqnSegment> segmentList = schemaFqn.getFqnSegmentList();
      return segment == getLast(segmentList);
    }
    return false;
  }

  @Nullable
  public static PsiReference getReference(SchemaFqnSegment segment) {
    if (!isLast(segment)) return null; // build reference to schema file(s)?

    SchemaFqnTypeRef fqnTypeRef = getFqnTypeRef(segment);
    if (fqnTypeRef == null) return null;

    return ReferenceFactory.getReference(segment, fqnTypeRef);
  }

  // field decl

  @Nullable
  public static String getName(SchemaFieldDecl fieldDecl) {
    return getNameIdentifier(fieldDecl).getText();
  }

  public static PsiElement setName(SchemaFieldDecl fieldDecl, String name) {
    PsiElement oldId = fieldDecl.getId();
    PsiElement newId = SchemaElementFactory.createId(fieldDecl.getProject(), name);
    return oldId.replace(newId);
  }

  @NotNull
  public static PsiElement getNameIdentifier(SchemaFieldDecl fieldDecl) {
    return fieldDecl.getId();
  }

  // tag decl

  @Nullable
  public static String getName(SchemaTagDecl tagDecl) {
    return getNameIdentifier(tagDecl).getText();
  }

  public static PsiElement setName(SchemaTagDecl tagDecl, String name) {
    PsiElement oldId = tagDecl.getId();
    PsiElement newId = SchemaElementFactory.createId(tagDecl.getProject(), name);
    return oldId.replace(newId);
  }

  @NotNull
  public static PsiElement getNameIdentifier(SchemaTagDecl tagDecl) {
    return tagDecl.getId();
  }

  // multiMember decl

  @Nullable
  public static String getName(SchemaMultiMemberDecl multiMemberDecl) {
    return getNameIdentifier(multiMemberDecl).getText();
  }

  public static PsiElement setName(SchemaMultiMemberDecl multiMemberDecl, String name) {
    PsiElement oldId = multiMemberDecl.getId();
    PsiElement newId = SchemaElementFactory.createId(multiMemberDecl.getProject(), name);
    return oldId.replace(newId);
  }

  @NotNull
  public static PsiElement getNameIdentifier(SchemaMultiMemberDecl multiMemberDecl) {
    return multiMemberDecl.getId();
  }

  // enumMember decl

  @Nullable
  public static String getName(SchemaEnumMemberDecl enumMemberDecl) {
    return getNameIdentifier(enumMemberDecl).getText();
  }

  public static PsiElement setName(SchemaEnumMemberDecl enumMemberDecl, String name) {
    PsiElement oldId = enumMemberDecl.getId();
    PsiElement newId = SchemaElementFactory.createId(enumMemberDecl.getProject(), name);
    return oldId.replace(newId);
  }

  @NotNull
  public static PsiElement getNameIdentifier(SchemaEnumMemberDecl enumMemberDecl) {
    return enumMemberDecl.getId();
  }

  // custom param

  @Nullable
  public static String getName(SchemaCustomParam customParam) {
    return getNameIdentifier(customParam).getText();
  }

  public static PsiElement setName(SchemaCustomParam customParam, String name) {
    PsiElement oldId = customParam.getId();
    PsiElement newId = SchemaElementFactory.createId(customParam.getProject(), name);
    return oldId.replace(newId);
  }

  @NotNull
  public static PsiElement getNameIdentifier(SchemaCustomParam customParam) {
    return customParam.getId();
  }

  /////////////

  private static <T> T getLast(List<T> list) {
    if (list == null || list.isEmpty()) return null;
    return list.get(list.size() - 1);
  }
}
