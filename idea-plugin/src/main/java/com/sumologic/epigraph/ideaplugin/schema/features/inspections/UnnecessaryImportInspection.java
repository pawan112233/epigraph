package com.sumologic.epigraph.ideaplugin.schema.features.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.containers.MultiMap;
import com.sumologic.epigraph.ideaplugin.schema.brains.ImportsManager;
import com.sumologic.epigraph.ideaplugin.schema.features.actions.fixes.OptimizeImportsQuickFix;
import com.sumologic.epigraph.schema.parser.Fqn;
import com.sumologic.epigraph.schema.parser.psi.SchemaImportStatement;
import com.sumologic.epigraph.schema.parser.psi.SchemaImports;
import com.sumologic.epigraph.schema.parser.psi.SchemaVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:konstantin@sumologic.com">Konstantin Sobolev</a>
 */
public class UnnecessaryImportInspection extends LocalInspectionTool {
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new SchemaVisitor() {
      @Override
      public void visitImports(@NotNull SchemaImports schemaImports) {
        super.visitImports(schemaImports);

        List<SchemaImportStatement> imports = schemaImports.getImportStatementList();
        MultiMap<Fqn, SchemaImportStatement> importsByFqn = ImportsManager.getImportsByFqn(imports);

        for (Map.Entry<Fqn, Collection<SchemaImportStatement>> entry : importsByFqn.entrySet()) {
          entry.getValue().stream()
              .filter(is -> ImportsManager.DEFAULT_IMPORTS_LIST.contains(entry.getKey()))
              .forEach(is -> holder.registerProblem(is,
                  InspectionBundle.message("import.unnecessary.problem.descriptor"),
                  ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                  OptimizeImportsQuickFix.INSTANCE));
        }
      }
    };
  }
}
