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

import com.intellij.diagram.DiagramCategory;
import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.DiagramRelationshipInfoAdapter;
import com.intellij.diagram.DiagramRelationshipManager;
import com.intellij.diagram.presentation.DiagramLineType;
import com.intellij.psi.PsiNamedElement;
import ws.epigraph.schema.parser.psi.SchemaQnTypeRef;
import ws.epigraph.schema.parser.psi.SchemaMetaDecl;
import ws.epigraph.schema.parser.psi.SchemaTypeDef;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class SchemaDiagramRelationshipManager implements DiagramRelationshipManager<PsiNamedElement> {
  public static final String ZERO_OR_MORE_CARDINALITY = "*";

  public final static DiagramRelationshipInfo EXTENDS =
      new DiagramRelationshipInfoAdapter("extends", DiagramLineType.SOLID
      ) {
        @Override
        public Shape getStartArrow() {
          return DELTA;
        }
      };

  public final static DiagramRelationshipInfo META =
      new DiagramRelationshipInfoAdapter("meta", DiagramLineType.DOTTED, "meta"
      ) {
        @Override
        public Shape getStartArrow() {
          return STANDARD;
        }
      };

//  public final static DiagramRelationshipInfo MEMBER =
//      new DiagramRelationshipInfoAdapter("member", DiagramLineType.SOLID
//      ) {
//        @Override
//        public Shape getStartArrow() {
//          return DIAMOND;
//        }
//      };

  public static DiagramRelationshipInfo member(String name, String fromLabel, String toLabel) {
    return new DiagramRelationshipInfoAdapter("member", DiagramLineType.SOLID, name, fromLabel, toLabel, 1) {
      @Override
      public Shape getStartArrow() {
        return DIAMOND;
      }

      @Override
      public Shape getEndArrow() {
        return DELTA_SMALL;
      }
    };
  }

  @Nullable
  @Override
  public DiagramRelationshipInfo getDependencyInfo(PsiNamedElement e1,
                                                   PsiNamedElement e2,
                                                   DiagramCategory diagramCategory) {
    if (e1 instanceof SchemaTypeDef && e2 instanceof SchemaTypeDef) {
      SchemaTypeDef td1 = (SchemaTypeDef) e1;
      SchemaTypeDef td2 = (SchemaTypeDef) e2;

      if (td2.extendsParents().contains(e1)) return EXTENDS;

      SchemaMetaDecl metaDecl = td1.getMetaDecl();
      if (metaDecl != null) {
        SchemaQnTypeRef typeRef = metaDecl.getQnTypeRef();
        if (typeRef != null) {
          SchemaTypeDef typeDef = typeRef.resolve();
          if (e2.equals(typeDef)) return META;
        }
      }
    }
    return null;
  }

  @Override
  public DiagramCategory[] getContentCategories() {
    return SchemaDiagramNodeContentManager.CATEGORIES;
  }
}
