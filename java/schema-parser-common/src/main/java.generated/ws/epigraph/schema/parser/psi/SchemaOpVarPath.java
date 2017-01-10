// This is a generated file. Not intended for manual editing.
package ws.epigraph.schema.parser.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface SchemaOpVarPath extends PsiElement {

  @NotNull
  SchemaOpModelPath getOpModelPath();

  @NotNull
  List<SchemaOpModelPathProperty> getOpModelPathPropertyList();

  @Nullable
  SchemaTagName getTagName();

  @Nullable
  PsiElement getColon();

  @Nullable
  PsiElement getCurlyLeft();

  @Nullable
  PsiElement getCurlyRight();

}