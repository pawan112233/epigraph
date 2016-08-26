package io.epigraph.lang.parser.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.util.IncorrectOperationException;
import io.epigraph.lang.parser.Fqn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
public interface EpigraphTypeDef extends PsiNameIdentifierOwner {
  EpigraphQid getQid();

  @Nullable
  String getName();

  @Nullable
  PsiElement setName(@NotNull String name);

  @Nullable
  PsiElement getNameIdentifier();

  @Nullable
  Fqn getNamespace();

  @Nullable
  Fqn getFqn();

  int getTextOffset();

  @Nullable
  PsiElement getAbstract() ;

  @Nullable
  PsiElement getPolymorphic() ;

  @Nullable
  EpigraphExtendsDecl getExtendsDecl();

  @Nullable
  EpigraphSupplementsDecl getSupplementsDecl();

  @Nullable
  EpigraphMetaDecl getMetaDecl();

  @NotNull
  TypeKind getKind();

  Icon getIcon(int flags);

  @NotNull
  List<EpigraphTypeDef> extendsParents();

  @Override
  void delete() throws IncorrectOperationException;
}