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

/* Created by yegor on 7/22/16. */

package ws.epigraph.types;

import org.jetbrains.annotations.Nullable;
import ws.epigraph.names.QualifiedTypeName;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class NamedListType extends ListType {

  protected NamedListType(
      @NotNull QualifiedTypeName name,
      @NotNull List<@NotNull ? extends NamedListType> immediateNamedSupertypes,
      @NotNull DataType elementType,
      @Nullable DatumType immediateMetaType
  ) { super(name, immediateNamedSupertypes, elementType, immediateMetaType); }

  // TODO .Raw

  // TODO .Static

}
