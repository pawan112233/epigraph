/*
 * Copyright 2016 Sumo Logic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ws.epigraph.ideaplugin.schema.diagram;

import com.intellij.diagram.DiagramElementsProvider;
import com.intellij.diagram.actions.DiagramAddElementAction;
import com.intellij.diagram.actions.DiagramDefaultAddElementAction;
import com.intellij.diagram.extras.DiagramExtras;
import com.intellij.diagram.extras.providers.DiagramDnDProvider;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class SchemaDiagramExtras extends DiagramExtras<PsiNamedElement> {
  private static final DiagramDefaultAddElementAction<PsiNamedElement> ADD_ELEMENT_ACTION =
      new SchemaDiagramAddElementAction();

  @SuppressWarnings("unchecked")
  private static final DiagramElementsProvider<PsiNamedElement>[] ELEMENTS_PROVIDERS = new DiagramElementsProvider[]{
      new SchemaDiagramParentElementsProvider(), new SchemaDiagramImplementationElementsProvider()
  };

  private static final DiagramDnDProvider<PsiNamedElement> DND_PROVIDER = new SchemaDiagramDnDProvider();

  @Nullable
  @Override
  public DiagramAddElementAction getAddElementHandler() {
    return ADD_ELEMENT_ACTION;
  }

  @Nullable
  @Override
  public DiagramDnDProvider<PsiNamedElement> getDnDProvider() {
    return DND_PROVIDER;
  }

  @Override
  public DiagramElementsProvider<PsiNamedElement>[] getElementsProviders() {
    return ELEMENTS_PROVIDERS;
  }
}
