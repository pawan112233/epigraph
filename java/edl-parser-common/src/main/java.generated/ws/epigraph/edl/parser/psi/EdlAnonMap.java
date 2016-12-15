// This is a generated file. Not intended for manual editing.
package ws.epigraph.edl.parser.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface EdlAnonMap extends EdlTypeRef {

  @Nullable
  EdlTypeRef getTypeRef();

  @Nullable
  EdlValueTypeRef getValueTypeRef();

  @Nullable
  PsiElement getBracketLeft();

  @Nullable
  PsiElement getBracketRight();

  @Nullable
  PsiElement getComma();

  @NotNull
  PsiElement getMap();

}