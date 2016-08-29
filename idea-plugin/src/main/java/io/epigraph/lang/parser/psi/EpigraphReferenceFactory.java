package io.epigraph.lang.parser.psi;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.sumologic.epigraph.ideaplugin.schema.brains.NamespaceManager;
import com.sumologic.epigraph.ideaplugin.schema.brains.SchemaFqnReference;
import com.sumologic.epigraph.ideaplugin.schema.brains.EpigraphFqnReferenceResolver;
import com.sumologic.epigraph.ideaplugin.schema.brains.SchemaVarTagReference;
import com.sumologic.epigraph.ideaplugin.schema.index.SchemaSearchScopeUtil;
import io.epigraph.lang.parser.Fqn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.sumologic.epigraph.ideaplugin.schema.brains.NamespaceManager.getNamespace;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 * @see <a href="https://github.com/SumoLogic/epigraph/wiki/References%20implementation#reference-resolution-algorithm">Reference resolution algorithm</a>
 */
public class EpigraphReferenceFactory {
  @Nullable
  public static PsiReference getFqnReference(@NotNull EpigraphFqnSegment segment) {
    EpigraphFqnReferenceResolver resolver = getFqnReferenceResolver(segment);

    return resolver == null ? null : new SchemaFqnReference(segment, resolver);
  }

  @Nullable
  public static EpigraphFqnReferenceResolver getFqnReferenceResolver(@NotNull EpigraphFqnSegment segment) {
    PsiFile file = segment.getContainingFile();
    if (!(file instanceof SchemaFile)) return null;

    final boolean isImport = PsiTreeUtil.getParentOfType(segment, EpigraphImportStatement.class) != null;

    return getFqnReferenceResolver((SchemaFile) file, segment.getFqn(), isImport);
  }

  @Nullable
  public static EpigraphFqnReferenceResolver getFqnReferenceResolver(@NotNull SchemaFile file, @NotNull Fqn fqn, boolean isImport) {
    if (fqn.isEmpty()) return null;

    final List<Fqn> prefixes = new ArrayList<>();
    boolean isSingleSegment = fqn.size() == 1;

    final String first = fqn.first();
    assert first != null;


    if (!isImport) {
      prefixes.addAll(
          // imports ending with our first segment, with last segment removed
          NamespaceManager.getImportedNamespaces(file).stream()
              .filter(f -> first.equals(f.last()))
              .map(Fqn::removeLastSegment)
              .collect(Collectors.toList())
      );
    }

    if (isSingleSegment) {
      if (!isImport) {
        // add all default namespaces
        Collections.addAll(prefixes, NamespaceManager.DEFAULT_NAMESPACES);
      }

      // current namespace
      Fqn currentNamespace = getNamespace(file);
      if (currentNamespace != null) {
        prefixes.add(currentNamespace);
      }

    } else {
      prefixes.add(Fqn.EMPTY);
    }

    // deduplicate, preserving order
    Set<Fqn> dedupPrefixes = new LinkedHashSet<>(prefixes);
    prefixes.clear();
    prefixes.addAll(dedupPrefixes);

    return new EpigraphFqnReferenceResolver(prefixes, fqn, SchemaSearchScopeUtil.getSearchScope(file));
  }

  @Nullable
  public static PsiReference getVarTagReference(@NotNull EpigraphVarTagRef varTagRef) {
    EpigraphVarTypeDef varTypeDef = PsiTreeUtil.getParentOfType(varTagRef, EpigraphVarTypeDef.class);
    return varTypeDef == null ? null : new SchemaVarTagReference(varTypeDef, varTagRef.getQid());
  }
}
