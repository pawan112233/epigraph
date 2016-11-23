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

package ws.epigraph.projections.gen;

import ws.epigraph.lang.TextLocation;
import ws.epigraph.projections.Annotations;
import ws.epigraph.types.DatumType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public interface GenModelProjection<MP extends GenModelProjection</*MP*/?, ?>, M extends DatumType> {

  @NotNull M model();

  @Nullable MP metaProjection();

  /**
   * Merges a list of models together
   * <p/>
   * Should work as a 'static' method: current object should not be merged (most probably it is going
   * to be the first item of the list anyways). Such design allows for easier implementations that have to
   * iterate over all the items being merged.
   *
   * @param model            resulting model's type
   * @param modelProjections models to merge
   *
   * @return merged models or {@code null} if {@code modelProjections} is empty
   */
  /* static */
  MP merge(@NotNull M model, @NotNull List<MP> modelProjections);

  @NotNull Annotations annotations();

  @NotNull TextLocation location();
}
