// This is a generated file. Not intended for manual editing.
package ws.epigraph.schema.parser.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiNameIdentifierOwner;

public class SchemaVisitor extends PsiElementVisitor {

  public void visitAnnotation(@NotNull SchemaAnnotation o) {
    visitPsiNamedElement(o);
  }

  public void visitAnonList(@NotNull SchemaAnonList o) {
    visitTypeRef(o);
  }

  public void visitAnonMap(@NotNull SchemaAnonMap o) {
    visitTypeRef(o);
  }

  public void visitCreateOperationBodyPart(@NotNull SchemaCreateOperationBodyPart o) {
    visitPsiElement(o);
  }

  public void visitCreateOperationDef(@NotNull SchemaCreateOperationDef o) {
    visitPsiElement(o);
  }

  public void visitCustomOperationBodyPart(@NotNull SchemaCustomOperationBodyPart o) {
    visitPsiElement(o);
  }

  public void visitCustomOperationDef(@NotNull SchemaCustomOperationDef o) {
    visitPsiElement(o);
  }

  public void visitData(@NotNull SchemaData o) {
    visitPsiElement(o);
  }

  public void visitDataEntry(@NotNull SchemaDataEntry o) {
    visitPsiElement(o);
  }

  public void visitDataValue(@NotNull SchemaDataValue o) {
    visitPsiElement(o);
  }

  public void visitDatum(@NotNull SchemaDatum o) {
    visitPsiElement(o);
  }

  public void visitDefs(@NotNull SchemaDefs o) {
    visitPsiElement(o);
  }

  public void visitDeleteOperationBodyPart(@NotNull SchemaDeleteOperationBodyPart o) {
    visitPsiElement(o);
  }

  public void visitDeleteOperationDef(@NotNull SchemaDeleteOperationDef o) {
    visitPsiElement(o);
  }

  public void visitEnumDatum(@NotNull SchemaEnumDatum o) {
    visitDatum(o);
  }

  public void visitEnumMemberDecl(@NotNull SchemaEnumMemberDecl o) {
    visitAnnotationsHolder(o);
    // visitPsiNamedElement(o);
  }

  public void visitEnumTypeBody(@NotNull SchemaEnumTypeBody o) {
    visitAnnotationsHolder(o);
  }

  public void visitEnumTypeDef(@NotNull SchemaEnumTypeDef o) {
    visitTypeDef(o);
  }

  public void visitExtendsDecl(@NotNull SchemaExtendsDecl o) {
    visitPsiElement(o);
  }

  public void visitFieldDecl(@NotNull SchemaFieldDecl o) {
    visitAnnotationsHolder(o);
    // visitPsiNamedElement(o);
  }

  public void visitImportStatement(@NotNull SchemaImportStatement o) {
    visitPsiElement(o);
  }

  public void visitImports(@NotNull SchemaImports o) {
    visitPsiElement(o);
  }

  public void visitListDatum(@NotNull SchemaListDatum o) {
    visitDatum(o);
  }

  public void visitListTypeBody(@NotNull SchemaListTypeBody o) {
    visitAnnotationsHolder(o);
  }

  public void visitListTypeDef(@NotNull SchemaListTypeDef o) {
    visitTypeDef(o);
  }

  public void visitMapDatum(@NotNull SchemaMapDatum o) {
    visitDatum(o);
  }

  public void visitMapDatumEntry(@NotNull SchemaMapDatumEntry o) {
    visitPsiElement(o);
  }

  public void visitMapTypeBody(@NotNull SchemaMapTypeBody o) {
    visitAnnotationsHolder(o);
  }

  public void visitMapTypeDef(@NotNull SchemaMapTypeDef o) {
    visitTypeDef(o);
  }

  public void visitMetaDecl(@NotNull SchemaMetaDecl o) {
    visitPsiElement(o);
  }

  public void visitNamespaceDecl(@NotNull SchemaNamespaceDecl o) {
    visitPsiElement(o);
  }

  public void visitNullDatum(@NotNull SchemaNullDatum o) {
    visitDatum(o);
  }

  public void visitOpDeleteFieldProjection(@NotNull SchemaOpDeleteFieldProjection o) {
    visitPsiElement(o);
  }

  public void visitOpDeleteFieldProjectionBodyPart(@NotNull SchemaOpDeleteFieldProjectionBodyPart o) {
    visitPsiElement(o);
  }

  public void visitOpDeleteFieldProjectionEntry(@NotNull SchemaOpDeleteFieldProjectionEntry o) {
    visitPsiElement(o);
  }

  public void visitOpDeleteKeyProjection(@NotNull SchemaOpDeleteKeyProjection o) {
    visitPsiElement(o);
  }

  public void visitOpDeleteKeyProjectionPart(@NotNull SchemaOpDeleteKeyProjectionPart o) {
    visitPsiElement(o);
  }

  public void visitOpDeleteListModelProjection(@NotNull SchemaOpDeleteListModelProjection o) {
    visitPsiElement(o);
  }

  public void visitOpDeleteMapModelProjection(@NotNull SchemaOpDeleteMapModelProjection o) {
    visitPsiElement(o);
  }

  public void visitOpDeleteModelProjection(@NotNull SchemaOpDeleteModelProjection o) {
    visitPsiElement(o);
  }

  public void visitOpDeleteModelProperty(@NotNull SchemaOpDeleteModelProperty o) {
    visitPsiElement(o);
  }

  public void visitOpDeleteMultiTagProjection(@NotNull SchemaOpDeleteMultiTagProjection o) {
    visitPsiElement(o);
  }

  public void visitOpDeleteMultiTagProjectionItem(@NotNull SchemaOpDeleteMultiTagProjectionItem o) {
    visitPsiElement(o);
  }

  public void visitOpDeleteRecordModelProjection(@NotNull SchemaOpDeleteRecordModelProjection o) {
    visitPsiElement(o);
  }

  public void visitOpDeleteSingleTagProjection(@NotNull SchemaOpDeleteSingleTagProjection o) {
    visitPsiElement(o);
  }

  public void visitOpDeleteVarMultiTail(@NotNull SchemaOpDeleteVarMultiTail o) {
    visitPsiElement(o);
  }

  public void visitOpDeleteVarMultiTailItem(@NotNull SchemaOpDeleteVarMultiTailItem o) {
    visitPsiElement(o);
  }

  public void visitOpDeleteVarPolymorphicTail(@NotNull SchemaOpDeleteVarPolymorphicTail o) {
    visitPsiElement(o);
  }

  public void visitOpDeleteVarProjection(@NotNull SchemaOpDeleteVarProjection o) {
    visitPsiElement(o);
  }

  public void visitOpDeleteVarSingleTail(@NotNull SchemaOpDeleteVarSingleTail o) {
    visitPsiElement(o);
  }

  public void visitOpFieldPath(@NotNull SchemaOpFieldPath o) {
    visitPsiElement(o);
  }

  public void visitOpFieldPathBodyPart(@NotNull SchemaOpFieldPathBodyPart o) {
    visitPsiElement(o);
  }

  public void visitOpFieldPathEntry(@NotNull SchemaOpFieldPathEntry o) {
    visitPsiElement(o);
  }

  public void visitOpInputDefaultValue(@NotNull SchemaOpInputDefaultValue o) {
    visitPsiElement(o);
  }

  public void visitOpInputFieldProjection(@NotNull SchemaOpInputFieldProjection o) {
    visitPsiElement(o);
  }

  public void visitOpInputFieldProjectionBodyPart(@NotNull SchemaOpInputFieldProjectionBodyPart o) {
    visitPsiElement(o);
  }

  public void visitOpInputFieldProjectionEntry(@NotNull SchemaOpInputFieldProjectionEntry o) {
    visitPsiElement(o);
  }

  public void visitOpInputKeyProjection(@NotNull SchemaOpInputKeyProjection o) {
    visitPsiElement(o);
  }

  public void visitOpInputKeyProjectionPart(@NotNull SchemaOpInputKeyProjectionPart o) {
    visitPsiElement(o);
  }

  public void visitOpInputListModelProjection(@NotNull SchemaOpInputListModelProjection o) {
    visitPsiElement(o);
  }

  public void visitOpInputMapModelProjection(@NotNull SchemaOpInputMapModelProjection o) {
    visitPsiElement(o);
  }

  public void visitOpInputModelMeta(@NotNull SchemaOpInputModelMeta o) {
    visitPsiElement(o);
  }

  public void visitOpInputModelProjection(@NotNull SchemaOpInputModelProjection o) {
    visitPsiElement(o);
  }

  public void visitOpInputModelProperty(@NotNull SchemaOpInputModelProperty o) {
    visitPsiElement(o);
  }

  public void visitOpInputMultiTagProjection(@NotNull SchemaOpInputMultiTagProjection o) {
    visitPsiElement(o);
  }

  public void visitOpInputMultiTagProjectionItem(@NotNull SchemaOpInputMultiTagProjectionItem o) {
    visitPsiElement(o);
  }

  public void visitOpInputRecordModelProjection(@NotNull SchemaOpInputRecordModelProjection o) {
    visitPsiElement(o);
  }

  public void visitOpInputSingleTagProjection(@NotNull SchemaOpInputSingleTagProjection o) {
    visitPsiElement(o);
  }

  public void visitOpInputVarMultiTail(@NotNull SchemaOpInputVarMultiTail o) {
    visitPsiElement(o);
  }

  public void visitOpInputVarMultiTailItem(@NotNull SchemaOpInputVarMultiTailItem o) {
    visitPsiElement(o);
  }

  public void visitOpInputVarPolymorphicTail(@NotNull SchemaOpInputVarPolymorphicTail o) {
    visitPsiElement(o);
  }

  public void visitOpInputVarProjection(@NotNull SchemaOpInputVarProjection o) {
    visitPsiElement(o);
  }

  public void visitOpInputVarSingleTail(@NotNull SchemaOpInputVarSingleTail o) {
    visitPsiElement(o);
  }

  public void visitOpMapModelPath(@NotNull SchemaOpMapModelPath o) {
    visitPsiElement(o);
  }

  public void visitOpModelPath(@NotNull SchemaOpModelPath o) {
    visitPsiElement(o);
  }

  public void visitOpModelPathProperty(@NotNull SchemaOpModelPathProperty o) {
    visitPsiElement(o);
  }

  public void visitOpOutputFieldProjection(@NotNull SchemaOpOutputFieldProjection o) {
    visitPsiElement(o);
  }

  public void visitOpOutputFieldProjectionBodyPart(@NotNull SchemaOpOutputFieldProjectionBodyPart o) {
    visitPsiElement(o);
  }

  public void visitOpOutputFieldProjectionEntry(@NotNull SchemaOpOutputFieldProjectionEntry o) {
    visitPsiElement(o);
  }

  public void visitOpOutputKeyProjection(@NotNull SchemaOpOutputKeyProjection o) {
    visitPsiElement(o);
  }

  public void visitOpOutputKeyProjectionPart(@NotNull SchemaOpOutputKeyProjectionPart o) {
    visitPsiElement(o);
  }

  public void visitOpOutputListModelProjection(@NotNull SchemaOpOutputListModelProjection o) {
    visitPsiElement(o);
  }

  public void visitOpOutputMapModelProjection(@NotNull SchemaOpOutputMapModelProjection o) {
    visitPsiElement(o);
  }

  public void visitOpOutputModelMeta(@NotNull SchemaOpOutputModelMeta o) {
    visitPsiElement(o);
  }

  public void visitOpOutputModelProjection(@NotNull SchemaOpOutputModelProjection o) {
    visitPsiElement(o);
  }

  public void visitOpOutputModelProperty(@NotNull SchemaOpOutputModelProperty o) {
    visitPsiElement(o);
  }

  public void visitOpOutputMultiTagProjection(@NotNull SchemaOpOutputMultiTagProjection o) {
    visitPsiElement(o);
  }

  public void visitOpOutputMultiTagProjectionItem(@NotNull SchemaOpOutputMultiTagProjectionItem o) {
    visitPsiElement(o);
  }

  public void visitOpOutputRecordModelProjection(@NotNull SchemaOpOutputRecordModelProjection o) {
    visitPsiElement(o);
  }

  public void visitOpOutputSingleTagProjection(@NotNull SchemaOpOutputSingleTagProjection o) {
    visitPsiElement(o);
  }

  public void visitOpOutputVarMultiTail(@NotNull SchemaOpOutputVarMultiTail o) {
    visitPsiElement(o);
  }

  public void visitOpOutputVarMultiTailItem(@NotNull SchemaOpOutputVarMultiTailItem o) {
    visitPsiElement(o);
  }

  public void visitOpOutputVarPolymorphicTail(@NotNull SchemaOpOutputVarPolymorphicTail o) {
    visitPsiElement(o);
  }

  public void visitOpOutputVarProjection(@NotNull SchemaOpOutputVarProjection o) {
    visitPsiElement(o);
  }

  public void visitOpOutputVarSingleTail(@NotNull SchemaOpOutputVarSingleTail o) {
    visitPsiElement(o);
  }

  public void visitOpParam(@NotNull SchemaOpParam o) {
    visitPsiElement(o);
  }

  public void visitOpPathKeyProjection(@NotNull SchemaOpPathKeyProjection o) {
    visitPsiElement(o);
  }

  public void visitOpPathKeyProjectionBody(@NotNull SchemaOpPathKeyProjectionBody o) {
    visitPsiElement(o);
  }

  public void visitOpPathKeyProjectionPart(@NotNull SchemaOpPathKeyProjectionPart o) {
    visitPsiElement(o);
  }

  public void visitOpRecordModelPath(@NotNull SchemaOpRecordModelPath o) {
    visitPsiElement(o);
  }

  public void visitOpVarPath(@NotNull SchemaOpVarPath o) {
    visitPsiElement(o);
  }

  public void visitOperationDef(@NotNull SchemaOperationDef o) {
    visitPsiElement(o);
  }

  public void visitOperationDeleteProjection(@NotNull SchemaOperationDeleteProjection o) {
    visitPsiElement(o);
  }

  public void visitOperationInputProjection(@NotNull SchemaOperationInputProjection o) {
    visitPsiElement(o);
  }

  public void visitOperationInputType(@NotNull SchemaOperationInputType o) {
    visitPsiElement(o);
  }

  public void visitOperationMethod(@NotNull SchemaOperationMethod o) {
    visitPsiElement(o);
  }

  public void visitOperationName(@NotNull SchemaOperationName o) {
    visitPsiElement(o);
  }

  public void visitOperationOutputProjection(@NotNull SchemaOperationOutputProjection o) {
    visitPsiElement(o);
  }

  public void visitOperationOutputType(@NotNull SchemaOperationOutputType o) {
    visitPsiElement(o);
  }

  public void visitOperationPath(@NotNull SchemaOperationPath o) {
    visitPsiElement(o);
  }

  public void visitPrimitiveDatum(@NotNull SchemaPrimitiveDatum o) {
    visitDatum(o);
  }

  public void visitPrimitiveTypeBody(@NotNull SchemaPrimitiveTypeBody o) {
    visitAnnotationsHolder(o);
  }

  public void visitPrimitiveTypeDef(@NotNull SchemaPrimitiveTypeDef o) {
    visitTypeDef(o);
  }

  public void visitQid(@NotNull SchemaQid o) {
    visitPsiElement(o);
  }

  public void visitQn(@NotNull SchemaQn o) {
    visitPsiElement(o);
  }

  public void visitQnSegment(@NotNull SchemaQnSegment o) {
    visitPsiNameIdentifierOwner(o);
  }

  public void visitQnTypeRef(@NotNull SchemaQnTypeRef o) {
    visitTypeRef(o);
  }

  public void visitReadOperationBodyPart(@NotNull SchemaReadOperationBodyPart o) {
    visitPsiElement(o);
  }

  public void visitReadOperationDef(@NotNull SchemaReadOperationDef o) {
    visitPsiElement(o);
  }

  public void visitRecordDatum(@NotNull SchemaRecordDatum o) {
    visitDatum(o);
  }

  public void visitRecordDatumEntry(@NotNull SchemaRecordDatumEntry o) {
    visitPsiElement(o);
  }

  public void visitRecordTypeBody(@NotNull SchemaRecordTypeBody o) {
    visitAnnotationsHolder(o);
  }

  public void visitRecordTypeDef(@NotNull SchemaRecordTypeDef o) {
    visitTypeDef(o);
  }

  public void visitResourceDef(@NotNull SchemaResourceDef o) {
    visitPsiElement(o);
  }

  public void visitResourceName(@NotNull SchemaResourceName o) {
    visitPsiElement(o);
  }

  public void visitResourceType(@NotNull SchemaResourceType o) {
    visitPsiElement(o);
  }

  public void visitRetroDecl(@NotNull SchemaRetroDecl o) {
    visitPsiElement(o);
  }

  public void visitSupplementDef(@NotNull SchemaSupplementDef o) {
    visitPsiElement(o);
  }

  public void visitSupplementsDecl(@NotNull SchemaSupplementsDecl o) {
    visitPsiElement(o);
  }

  public void visitTagName(@NotNull SchemaTagName o) {
    visitPsiElement(o);
  }

  public void visitTypeDefWrapper(@NotNull SchemaTypeDefWrapper o) {
    visitPsiElement(o);
  }

  public void visitTypeRef(@NotNull SchemaTypeRef o) {
    visitPsiElement(o);
  }

  public void visitUpdateOperationBodyPart(@NotNull SchemaUpdateOperationBodyPart o) {
    visitPsiElement(o);
  }

  public void visitUpdateOperationDef(@NotNull SchemaUpdateOperationDef o) {
    visitPsiElement(o);
  }

  public void visitValueTypeRef(@NotNull SchemaValueTypeRef o) {
    visitPsiElement(o);
  }

  public void visitVarTagDecl(@NotNull SchemaVarTagDecl o) {
    visitAnnotationsHolder(o);
    // visitPsiNamedElement(o);
  }

  public void visitVarTagRef(@NotNull SchemaVarTagRef o) {
    visitPsiNameIdentifierOwner(o);
  }

  public void visitVarTypeBody(@NotNull SchemaVarTypeBody o) {
    visitAnnotationsHolder(o);
  }

  public void visitVarTypeDef(@NotNull SchemaVarTypeDef o) {
    visitTypeDef(o);
  }

  public void visitAnnotationsHolder(@NotNull AnnotationsHolder o) {
    visitElement(o);
  }

  public void visitPsiNameIdentifierOwner(@NotNull PsiNameIdentifierOwner o) {
    visitElement(o);
  }

  public void visitPsiNamedElement(@NotNull PsiNamedElement o) {
    visitElement(o);
  }

  public void visitTypeDef(@NotNull SchemaTypeDef o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
