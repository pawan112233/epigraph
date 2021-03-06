/*
 * Copyright 2017 Sumo Logic
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

package ws.epigraph.projections.abs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ws.epigraph.lang.TextLocation;
import ws.epigraph.projections.gen.GenModelProjection;
import ws.epigraph.projections.gen.GenProjection;
import ws.epigraph.projections.gen.ProjectionReferenceName;
import ws.epigraph.types.DatumTypeApi;
import ws.epigraph.types.TypeApi;
import ws.epigraph.types.TypeKind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public abstract class AbstractModelProjection<
//    P extends GenProjection<? extends P, TP, EP, /*MP*/?>,
    P extends GenProjection<? extends P, TP, EP, /*MP*/?>,
    EP extends AbstractEntityProjection<? extends P, EP, /*TP*/?, /*MP*/?>,
    TP extends AbstractTagProjectionEntry<TP, /*MP*/?>,
    MP extends AbstractModelProjection<P, /*EP*/?, TP, /*MP*/?, /*SMP*/? extends MP, ?>,
    SMP extends AbstractModelProjection<P, /*EP*/?, TP, /*MP*/?, SMP, ?>,
    M extends DatumTypeApi>
    extends AbstractProjection<P, SMP, TP, EP, MP>
    implements GenModelProjection<EP, TP, MP, SMP, M> {

  protected /*final*/ @Nullable MP metaProjection;

  protected AbstractModelProjection(
      @NotNull M model,
      boolean flag,
      @Nullable MP metaProjection,
      @Nullable List<SMP> polymorphicTails,
      @NotNull TextLocation location,
      @NotNull Function<SMP, TP> singleTagFactory
  ) {
    super(
        model,
        flag,
        Collections.emptyMap(),
        polymorphicTails,
        location
    );

    if (model.kind() == TypeKind.ENTITY)
      throw new IllegalArgumentException("Model projection can't be created for entity type " + model.name());

    this.metaProjection = metaProjection;

    initSelfTag(singleTagFactory);
  }

  protected AbstractModelProjection(
      @NotNull M model,
      @NotNull TextLocation location,
      @NotNull Function<SMP, TP> singleTagFactory) {

    super(model, location);
    metaProjection = null;
    initSelfTag(singleTagFactory);
  }

  private void initSelfTag(@NotNull Function<SMP, TP> singleTagFactory) {
    TP tp = singleTagFactory.apply(self());
    //noinspection AssignmentToSuperclassField
    this.tagProjections = Collections.singletonMap(tp.tag().name(), tp);
    validateSelfTagProjection();
  }

  @SuppressWarnings("unchecked")
  @Override
  public @NotNull M type() { return (M) super.type(); }

  @Override
  public @Nullable MP metaProjection() { return metaProjection; }

  @SuppressWarnings("unchecked")
  @Override
  protected SMP merge(
      final @NotNull TypeApi effectiveType,
      final @NotNull List<SMP> projections,
      final boolean normalizeTags,
      final boolean mergedFlag,
      final @Nullable List<SMP> mergedTails) {

    return merge(
        (M) effectiveType,
        mergedFlag,
        projections,
        mergeMetaProjection((M) effectiveType, projections),
        mergedTails
    );
  }

  @SuppressWarnings("unchecked")
  private MP mergeMetaProjection(final M model, final @NotNull List<SMP> projections) {
    List<MP> metaProjectionsList = new ArrayList<>();

    for (final GenModelProjection<?, ?, ?, ?, ?> p : projections) {
      AbstractModelProjection<P, EP, TP, MP, SMP, ?> mp = (AbstractModelProjection<P, EP, TP, MP, SMP, ?>) p;
      final @Nullable MP meta = mp.metaProjection();
      if (meta != null) metaProjectionsList.add(meta);
    }

    final MP mergedMetaProjection;
    if (metaProjectionsList.isEmpty()) mergedMetaProjection = null;
    else {
      final MP metaProjection = metaProjectionsList.get(0);
      DatumTypeApi metaModel = model.metaType();
      assert metaModel != null; // since we have a projection for it

      //noinspection ConstantConditions
      mergedMetaProjection = (MP) ((AbstractModelProjection<P, EP, TP, MP, SMP, M>) metaProjection)
          .merge(metaModel, (List<SMP>) metaProjectionsList)
          .normalizedForType(metaModel);
    }
    return mergedMetaProjection;
  }

  protected abstract SMP merge(
      @NotNull M model,
      boolean mergedFlag,
      @NotNull List<SMP> modelProjections,
      @Nullable MP mergedMetaProjection,
      @Nullable List<SMP> mergedTails);


  @SuppressWarnings("unchecked")
  @Override
  public void resolve(final @Nullable ProjectionReferenceName name, final @NotNull SMP value) {
    preResolveCheck(value);
    this.metaProjection = (MP) value.metaProjection();

    super.resolve(name, value);

    validateSelfTagProjection();
  }

  private void validateSelfTagProjection() {
    TP singleTagProjection = singleTagProjection();
    assert singleTagProjection != null;
    assert this == singleTagProjection.modelProjection();
  }
}
