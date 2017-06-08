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

package ws.epigraph.projections.op.delete;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ws.epigraph.projections.Annotation;
import ws.epigraph.projections.Annotations;
import ws.epigraph.projections.ProjectionUtils;
import ws.epigraph.projections.op.OpKeyPresence;
import ws.epigraph.projections.op.OpParam;
import ws.epigraph.projections.op.OpParams;
import ws.epigraph.psi.EpigraphPsiUtil;
import ws.epigraph.psi.PsiProcessingException;
import ws.epigraph.refs.TypeRef;
import ws.epigraph.refs.TypesResolver;
import ws.epigraph.schema.TypeRefs;
import ws.epigraph.schema.parser.psi.*;
import ws.epigraph.types.*;
import ws.epigraph.types.TypeKind;

import java.text.MessageFormat;
import java.util.*;

import static ws.epigraph.projections.ProjectionsParsingUtil.checkTailType;
import static ws.epigraph.projections.ProjectionsParsingUtil.getDatumType;
import static ws.epigraph.projections.ProjectionsParsingUtil.getUnionType;
import static ws.epigraph.projections.SchemaProjectionPsiParserUtil.*;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public final class OpDeleteProjectionsPsiParser {

  private OpDeleteProjectionsPsiParser() {}

  public static OpDeleteVarProjection parseVarProjection(
      @NotNull DataTypeApi dataType,
      boolean canDelete,
      @NotNull SchemaOpDeleteVarProjection psi,
      @NotNull TypesResolver typesResolver,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {

    final SchemaOpDeleteNamedVarProjection namedVarProjection = psi.getOpDeleteNamedVarProjection();
    if (namedVarProjection == null) {
      final SchemaOpDeleteUnnamedOrRefVarProjection unnamedOrRefVarProjection =
          psi.getOpDeleteUnnamedOrRefVarProjection();

      if (unnamedOrRefVarProjection == null)
        throw new PsiProcessingException(
            "Incomplete var projection definition",
            psi,
            context.errors()
        );

      return parseUnnamedOrRefVarProjection(
          dataType,
          canDelete,
          unnamedOrRefVarProjection,
          typesResolver,
          context
      );
    } else {
      // named var projection
      final String projectionName = namedVarProjection.getQid().getCanonicalName();

      final @Nullable SchemaOpDeleteUnnamedOrRefVarProjection unnamedOrRefVarProjection =
          namedVarProjection.getOpDeleteUnnamedOrRefVarProjection();

      if (unnamedOrRefVarProjection == null)
        throw new PsiProcessingException(
            String.format("Incomplete var projection '%s' definition", projectionName),
            psi,
            context.errors()
        );

      final OpDeleteVarProjection reference = context.referenceContext()
          .varReference(dataType.type(), projectionName, false, EpigraphPsiUtil.getLocation(psi));

      final OpDeleteVarProjection value = parseUnnamedOrRefVarProjection(
          dataType,
          canDelete,
          unnamedOrRefVarProjection,
          typesResolver,
          context
      );

      context.referenceContext()
          .resolve(projectionName, value, EpigraphPsiUtil.getLocation(unnamedOrRefVarProjection), context);

      return reference;
    }
  }

  public static OpDeleteVarProjection parseUnnamedOrRefVarProjection(
      @NotNull DataTypeApi dataType,
      boolean canDelete,
      @NotNull SchemaOpDeleteUnnamedOrRefVarProjection psi,
      @NotNull TypesResolver typesResolver,
      @NotNull OpDeletePsiProcessingContext context)
      throws PsiProcessingException {

    final SchemaOpDeleteVarProjectionRef varProjectionRef = psi.getOpDeleteVarProjectionRef();
    if (varProjectionRef == null) {
      // usual var projection
      final SchemaOpDeleteUnnamedVarProjection unnamedVarProjection = psi.getOpDeleteUnnamedVarProjection();
      if (unnamedVarProjection == null)
        throw new PsiProcessingException("Incomplete var projection definition", psi, context.errors());
      else return parseUnnamedVarProjection(
          dataType,
          canDelete,
          unnamedVarProjection,
          typesResolver,
          context
      );
    } else {
      // var projection reference
      final SchemaQid varProjectionRefPsi = varProjectionRef.getQid();
      if (varProjectionRefPsi == null)
        throw new PsiProcessingException(
            "Incomplete var projection definition: name not specified",
            psi,
            context.errors()
        );

      final String projectionName = varProjectionRefPsi.getCanonicalName();
      return context.referenceContext()
          .varReference(dataType.type(), projectionName, true, EpigraphPsiUtil.getLocation(psi));

    }
  }

  public static OpDeleteVarProjection parseUnnamedVarProjection(
      @NotNull DataTypeApi dataType,
      boolean canDelete,
      @NotNull SchemaOpDeleteUnnamedVarProjection psi,
      @NotNull TypesResolver typesResolver,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {

    final TypeApi type = dataType.type();
    final LinkedHashMap<String, OpDeleteTagProjectionEntry> tagProjections = new LinkedHashMap<>();

    @Nullable SchemaOpDeleteSingleTagProjection singleTagProjectionPsi = psi.getOpDeleteSingleTagProjection();

    if (singleTagProjectionPsi == null) {
      @Nullable SchemaOpDeleteMultiTagProjection multiTagProjection = psi.getOpDeleteMultiTagProjection();
      assert multiTagProjection != null;
      // parse list of tags
      @NotNull Iterable<SchemaOpDeleteMultiTagProjectionItem> tagProjectionPsiList =
          multiTagProjection.getOpDeleteMultiTagProjectionItemList();

      for (SchemaOpDeleteMultiTagProjectionItem tagProjectionPsi : tagProjectionPsiList) {
        final TagApi tag =
            getTag(type, tagProjectionPsi.getTagName(), dataType.defaultTag(), tagProjectionPsi, context);

        tagProjections.put(
            tag.name(),
            new OpDeleteTagProjectionEntry(
                tag,
                parseModelProjection(
                    tag.type(),
                    tagProjectionPsi.getOpDeleteModelProjection(),
                    typesResolver,
                    context
                ),
                EpigraphPsiUtil.getLocation(tagProjectionPsi)
            )
        );
      }
    } else {
      // leaf nodes can always be deleted
      if (isLeaf(singleTagProjectionPsi.getOpDeleteModelProjection()))
        canDelete = true;

      TagApi tag = findTag(
          type,
          singleTagProjectionPsi.getTagName(),
          dataType.defaultTag(),
          singleTagProjectionPsi,
          context
      );
      if (tag != null || !singleTagProjectionPsi.getText().isEmpty()) { // todo use isLeaf instead?
        if (tag == null) // will throw proper error
          tag = getTag(
              type,
              singleTagProjectionPsi.getTagName(),
              dataType.defaultTag(),
              singleTagProjectionPsi,
              context
          );

        tagProjections.put(
            tag.name(),
            new OpDeleteTagProjectionEntry(
                tag,
                parseModelProjection(
                    tag.type(),
                    singleTagProjectionPsi.getOpDeleteModelProjection(),
                    typesResolver,
                    context
                ),
                EpigraphPsiUtil.getLocation(singleTagProjectionPsi)
            )
        );
      }
    }

    // parse tails
    final List<OpDeleteVarProjection> tails;
    @Nullable SchemaOpDeleteVarPolymorphicTail psiTail = psi.getOpDeleteVarPolymorphicTail();
    if (psiTail == null) tails = null;
    else {
      tails = new ArrayList<>();

      @Nullable SchemaOpDeleteVarSingleTail singleTail = psiTail.getOpDeleteVarSingleTail();
      if (singleTail == null) {
        @Nullable SchemaOpDeleteVarMultiTail multiTail = psiTail.getOpDeleteVarMultiTail();
        assert multiTail != null;
        for (SchemaOpDeleteVarMultiTailItem tailItem : multiTail.getOpDeleteVarMultiTailItemList()) {
          @NotNull SchemaTypeRef tailTypeRef = tailItem.getTypeRef();
          @NotNull SchemaOpDeleteVarProjection psiTailProjection = tailItem.getOpDeleteVarProjection();
          @NotNull OpDeleteVarProjection tailProjection =
              buildTailProjection(dataType, canDelete, tailTypeRef, psiTailProjection, typesResolver, context);
          tails.add(tailProjection);
        }
      } else {
        @NotNull SchemaTypeRef tailTypeRef = singleTail.getTypeRef();
        @NotNull SchemaOpDeleteVarProjection psiTailProjection = singleTail.getOpDeleteVarProjection();
        @NotNull OpDeleteVarProjection tailProjection =
            buildTailProjection(dataType, canDelete, tailTypeRef, psiTailProjection, typesResolver, context);
        tails.add(tailProjection);
      }

    }

    try {
      return new OpDeleteVarProjection(
          type,
          canDelete,
          tagProjections,
          singleTagProjectionPsi == null || tagProjections.size() != 1,
          tails,
          EpigraphPsiUtil.getLocation(psi)
      );
    } catch (IllegalArgumentException e) {
      throw new PsiProcessingException(e, psi, context.errors());
    }
  }

  private static boolean isLeaf(SchemaOpDeleteModelProjection psi) {
    if (psi.getOpDeleteNamedModelProjection() != null) return false;
    final SchemaOpDeleteUnnamedOrRefModelProjection urp = psi.getOpDeleteUnnamedOrRefModelProjection();
    if (urp == null) return false; // bad syntax
    if (urp.getOpDeleteModelProjectionRef() != null) return false; // ?? assuming referenced model is non-empty
    final SchemaOpDeleteUnnamedModelProjection mp = urp.getOpDeleteUnnamedModelProjection();
    if (mp == null) return false; // bad syntax

    final SchemaOpDeleteRecordModelProjection rmp = mp.getOpDeleteRecordModelProjection();
    if (rmp != null)
      return rmp.getOpDeleteFieldProjectionEntryList().isEmpty();

    final SchemaOpDeleteMapModelProjection mmp = mp.getOpDeleteMapModelProjection();
    if (mmp != null)
      return mmp.getOpDeleteVarProjection() == null;

    final SchemaOpDeleteListModelProjection lmp = mp.getOpDeleteListModelProjection();
    //noinspection SimplifiableIfStatement
    if (lmp != null)
      return lmp.getOpDeleteVarProjection() == null;

    return true;
  }

  private static @NotNull OpParams parseModelParams(
      @NotNull Collection<SchemaOpDeleteModelProperty> modelProperties,
      @NotNull TypesResolver resolver,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {

    return parseParams(
        modelProperties.stream().map(SchemaOpDeleteModelProperty::getOpParam),
        resolver,
        context.inputPsiProcessingContext()
    );

  }

  private static @NotNull Annotations parseModelAnnotations(
      @NotNull Collection<SchemaOpDeleteModelProperty> modelProperties,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {

    return parseAnnotations(
        modelProperties.stream().map(SchemaOpDeleteModelProperty::getAnnotation),
        context
    );
  }

  private static @NotNull OpDeleteVarProjection buildTailProjection(
      @NotNull DataTypeApi dataType,
      boolean canDelete,
      @NotNull SchemaTypeRef tailTypeRefPsi,
      @NotNull SchemaOpDeleteVarProjection psiTailProjection,
      @NotNull TypesResolver typesResolver,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {

    @NotNull TypeRef tailTypeRef = TypeRefs.fromPsi(tailTypeRefPsi, context);
    @NotNull EntityTypeApi tailType = getUnionType(tailTypeRef, typesResolver, tailTypeRefPsi, context);
    checkTailType(tailType, dataType, tailTypeRefPsi, context);
    return parseVarProjection(
        tailType.dataType(dataType.defaultTag()),
        canDelete,
        psiTailProjection,
        typesResolver,
        context
    );
  }

  private static @NotNull OpDeleteVarProjection createDefaultVarProjection(
      @NotNull TypeApi type,
      @NotNull TagApi tag,
      boolean canDelete,
      @NotNull PsiElement locationPsi,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {

    return new OpDeleteVarProjection(
        type,
        canDelete,
        ProjectionUtils.singletonLinkedHashMap(
            tag.name(),
            new OpDeleteTagProjectionEntry(
                tag,
                createDefaultModelProjection(
                    tag.type(),
                    OpParams.EMPTY,
                    Annotations.EMPTY,
                    locationPsi,
                    context
                ),
                EpigraphPsiUtil.getLocation(locationPsi)
            )
        ),
        false,
        null,
        EpigraphPsiUtil.getLocation(locationPsi)
    );
  }

  private static @NotNull OpDeleteVarProjection createDefaultVarProjection(
      @NotNull DatumTypeApi type,
      boolean canDelete,
      @NotNull PsiElement locationPsi,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {
    return createDefaultVarProjection(type, type.self(), canDelete, locationPsi, context);
  }

  private static @NotNull OpDeleteVarProjection createDefaultVarProjection(
      @NotNull DataTypeApi type,
      boolean canDelete,
      @NotNull PsiElement locationPsi,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {

    @Nullable TagApi defaultTag = type.defaultTag();
    if (defaultTag == null)
      throw new PsiProcessingException(
          String.format("Can't build default projection for '%s', default tag not specified", type.name()),
          locationPsi,
          context
      );

    return createDefaultVarProjection(type.type(), defaultTag, canDelete, locationPsi, context);
  }

  public static @NotNull OpDeleteModelProjection<?, ?, ?> parseModelProjection(
      @NotNull DatumTypeApi type,
      @NotNull SchemaOpDeleteModelProjection psi,
      @NotNull TypesResolver typesResolver,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {

    return parseModelProjection(
        OpDeleteModelProjection.class,
        type,
        psi,
        typesResolver,
        context
    );
  }

  @SuppressWarnings("unchecked")
  private static @NotNull <MP extends OpDeleteModelProjection<?, ?, ?>>
  /*@NotNull*/ MP parseModelProjection(
      @NotNull Class<MP> modelClass,
      @NotNull DatumTypeApi type,
      @NotNull SchemaOpDeleteModelProjection psi,
      @NotNull TypesResolver typesResolver,
      @NotNull OpDeletePsiProcessingContext context)
      throws PsiProcessingException {

    // this follows `parseVarProjection` logic

    final SchemaOpDeleteNamedModelProjection namedModelProjection = psi.getOpDeleteNamedModelProjection();
    if (namedModelProjection == null) {
      final SchemaOpDeleteUnnamedOrRefModelProjection unnamedOrRefModelProjection =
          psi.getOpDeleteUnnamedOrRefModelProjection();

      if (unnamedOrRefModelProjection == null)
        throw new PsiProcessingException(
            "Incomplete model projection definition",
            psi,
            context.errors()
        );

      return parseUnnamedOrRefModelProjection(
          modelClass,
          type,
          unnamedOrRefModelProjection,
          typesResolver,
          context
      );
    } else {
      // named model projection
      final String projectionName = namedModelProjection.getQid().getCanonicalName();

      final @Nullable SchemaOpDeleteUnnamedOrRefModelProjection unnamedOrRefModelProjection =
          namedModelProjection.getOpDeleteUnnamedOrRefModelProjection();

      if (unnamedOrRefModelProjection == null)
        throw new PsiProcessingException(
            String.format("Incomplete model projection '%s' definition", projectionName),
            psi,
            context.errors()
        );

      final MP reference = (MP) context.referenceContext()
          .modelReference(type, projectionName, false, EpigraphPsiUtil.getLocation(psi));

      final MP value = parseUnnamedOrRefModelProjection(
          modelClass,
          type,
          unnamedOrRefModelProjection,
          typesResolver,
          context
      );

      //noinspection rawtypes
      context.referenceContext().<OpDeleteModelProjection>resolve(
          projectionName,
          value,
          EpigraphPsiUtil.getLocation(unnamedOrRefModelProjection),
          context
      );

      return reference;
    }
  }

  @SuppressWarnings("unchecked")
  private static @NotNull <MP extends OpDeleteModelProjection<?, ?, ?>>
  /*@NotNull*/ MP parseUnnamedOrRefModelProjection(
      @NotNull Class<MP> modelClass,
      @NotNull DatumTypeApi type,
      @NotNull SchemaOpDeleteUnnamedOrRefModelProjection psi,
      @NotNull TypesResolver typesResolver,
      @NotNull OpDeletePsiProcessingContext context)
      throws PsiProcessingException {

    // this follows `parseUnnamedOrRefVarProjection` logic

    final SchemaOpDeleteModelProjectionRef modelProjectionRef = psi.getOpDeleteModelProjectionRef();
    if (modelProjectionRef == null) {
      // usual model projection
      final SchemaOpDeleteUnnamedModelProjection unnamedModelProjection = psi.getOpDeleteUnnamedModelProjection();
      if (unnamedModelProjection == null)
        throw new PsiProcessingException("Incomplete model projection definition", psi, context.errors());
      else return parseUnnamedModelProjection(
          modelClass,
          type,
          unnamedModelProjection,
          typesResolver,
          context
      );
    } else {
      // model projection reference
      final SchemaQid modelProjectionRefPsi = modelProjectionRef.getQid();
      if (modelProjectionRefPsi == null)
        throw new PsiProcessingException(
            "Incomplete model projection definition: name not specified",
            psi,
            context.errors()
        );

      final String projectionName = modelProjectionRefPsi.getCanonicalName();
      return (MP) context.referenceContext().modelReference(
          type,
          projectionName,
          true,
          EpigraphPsiUtil.getLocation(psi)
      );

    }
  }

  @SuppressWarnings("unchecked")
  private static @NotNull <MP extends OpDeleteModelProjection<?, ?, ?>>
  /*@NotNull*/ MP parseUnnamedModelProjection(
      @NotNull Class<MP> modelClass,
      @NotNull DatumTypeApi type,
      @NotNull SchemaOpDeleteUnnamedModelProjection psi,
      @NotNull TypesResolver typesResolver,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {

    final Collection<SchemaOpDeleteModelProperty> modelProperties = psi.getOpDeleteModelPropertyList();

    final OpParams params = parseModelParams(modelProperties, typesResolver, context);
    final Annotations annotations = parseModelAnnotations(modelProperties, context);

    switch (type.kind()) {
      case RECORD:
        assert modelClass.isAssignableFrom(OpDeleteRecordModelProjection.class);
        ensureModelKind(psi, TypeKind.RECORD, context);

        @Nullable SchemaOpDeleteRecordModelProjection recordModelProjectionPsi = psi.getOpDeleteRecordModelProjection();
        if (recordModelProjectionPsi == null)
          return (MP) createDefaultModelProjection(type, params, annotations, psi, context);

        return (MP) parseRecordModelProjection(
            (RecordTypeApi) type,
            params,
            annotations,
            parseModelTails(
                OpDeleteRecordModelProjection.class,
                psi.getOpDeleteModelPolymorphicTail(),
                typesResolver,
                context
            ),
            recordModelProjectionPsi,
            typesResolver,
            context
        );

      case MAP:
        assert modelClass.isAssignableFrom(OpDeleteMapModelProjection.class);
        ensureModelKind(psi, TypeKind.MAP, context);

        @Nullable SchemaOpDeleteMapModelProjection mapModelProjectionPsi = psi.getOpDeleteMapModelProjection();
        if (mapModelProjectionPsi == null)
          return (MP) createDefaultModelProjection(type, params, annotations, psi, context);

        return (MP) parseMapModelProjection(
            (MapTypeApi) type,
            params,
            annotations,
            parseModelTails(
                OpDeleteMapModelProjection.class,
                psi.getOpDeleteModelPolymorphicTail(),
                typesResolver,
                context
            ),
            mapModelProjectionPsi,
            typesResolver,
            context
        );

      case LIST:
        assert modelClass.isAssignableFrom(OpDeleteListModelProjection.class);
        ensureModelKind(psi, TypeKind.LIST, context);

        @Nullable SchemaOpDeleteListModelProjection listModelProjectionPsi = psi.getOpDeleteListModelProjection();
        if (listModelProjectionPsi == null)
          return (MP) createDefaultModelProjection(type, params, annotations, psi, context);

        return (MP) parseListModelProjection(
            (ListTypeApi) type,
            params,
            annotations,
            parseModelTails(
                OpDeleteListModelProjection.class,
                psi.getOpDeleteModelPolymorphicTail(),
                typesResolver,
                context
            ),
            listModelProjectionPsi,
            typesResolver,
            context
        );

      case ENUM:
        throw new PsiProcessingException("Unsupported type kind: " + type.kind(), psi, context);

      case PRIMITIVE:
        assert modelClass.isAssignableFrom(OpDeletePrimitiveModelProjection.class);

        return (MP) parsePrimitiveModelProjection(
            (PrimitiveTypeApi) type,
            params,
            annotations,
            parseModelTails(
                OpDeletePrimitiveModelProjection.class,
                psi.getOpDeleteModelPolymorphicTail(),
                typesResolver,
                context
            ),
            psi
        );

      case ENTITY:
        throw new PsiProcessingException("Unsupported type kind: " + type.kind(), psi, context);

      default:
        throw new PsiProcessingException("Unknown type kind: " + type.kind(), psi, context);
    }
  }

  @Contract("_, null, _, _ -> null")
  private static @Nullable <MP extends OpDeleteModelProjection<?, ?, ?>>
  /*@Nullable*/ List<MP> parseModelTails(
      @NotNull Class<MP> modelClass,
      @Nullable SchemaOpDeleteModelPolymorphicTail tailPsi,
      @NotNull TypesResolver typesResolver,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {

    if (tailPsi == null) return null;
    else {
      List<MP> tails = new ArrayList<>();

      final SchemaOpDeleteModelSingleTail singleTailPsi = tailPsi.getOpDeleteModelSingleTail();
      if (singleTailPsi == null) {
        final SchemaOpDeleteModelMultiTail multiTailPsi = tailPsi.getOpDeleteModelMultiTail();
        assert multiTailPsi != null;
        for (SchemaOpDeleteModelMultiTailItem tailItemPsi : multiTailPsi.getOpDeleteModelMultiTailItemList()) {
          tails.add(
              buildModelTailProjection(
                  modelClass,
                  tailItemPsi.getTypeRef(),
                  tailItemPsi.getOpDeleteModelProjection(),
                  typesResolver,
                  context
              )
          );
        }
      } else {
        tails.add(
            buildModelTailProjection(
                modelClass,
                singleTailPsi.getTypeRef(),
                singleTailPsi.getOpDeleteModelProjection(),
                typesResolver,
                context
            )
        );
      }
      return tails;
    }
  }

  private static @NotNull <MP extends OpDeleteModelProjection<?, ?, ?>>
  /*@NotNull*/ MP buildModelTailProjection(
      @NotNull Class<MP> modelClass,
      @NotNull SchemaTypeRef tailTypeRefPsi,
      @NotNull SchemaOpDeleteModelProjection modelProjectionPsi,
      @NotNull TypesResolver typesResolver,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {

    @NotNull TypeRef tailTypeRef = TypeRefs.fromPsi(tailTypeRefPsi, context);
    @NotNull DatumTypeApi tailType = getDatumType(tailTypeRef, typesResolver, tailTypeRefPsi, context);

    return parseModelProjection(
        modelClass,
        tailType,
        modelProjectionPsi,
        typesResolver,
        context
    );
  }

  private static void ensureModelKind(
      @NotNull SchemaOpDeleteUnnamedModelProjection psi,
      @NotNull TypeKind expectedKind,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {

    // todo move to common
    @Nullable TypeKind actualKind = findProjectionKind(psi);
    if (actualKind != null && expectedKind != actualKind)
      throw new PsiProcessingException(MessageFormat.format(
          "Unexpected projection kind ''{0}'', expected ''{1}''",
          actualKind,
          expectedKind
      ), psi, context);
  }

  private static @Nullable TypeKind findProjectionKind(@NotNull SchemaOpDeleteUnnamedModelProjection psi) {
    // todo move to common
    if (psi.getOpDeleteRecordModelProjection() != null) return TypeKind.RECORD;
    if (psi.getOpDeleteMapModelProjection() != null) return TypeKind.MAP;
    if (psi.getOpDeleteListModelProjection() != null) return TypeKind.LIST;
    return null;
  }

  private static @NotNull OpDeleteModelProjection<?, ?, ?> createDefaultModelProjection(
      @NotNull DatumTypeApi type,
      @NotNull OpParams params,
      @NotNull Annotations annotations,
      @NotNull PsiElement locationPsi,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {

    switch (type.kind()) {
      case RECORD:
        return new OpDeleteRecordModelProjection(
            (RecordTypeApi) type,
            params,
            annotations,
            Collections.emptyMap(),
            null,
            EpigraphPsiUtil.getLocation(locationPsi)
        );
      case MAP:
        MapTypeApi mapType = (MapTypeApi) type;

        final OpDeleteKeyProjection keyProjection =
            new OpDeleteKeyProjection(
                OpKeyPresence.OPTIONAL,
                OpParams.EMPTY,
                Annotations.EMPTY,
                EpigraphPsiUtil.getLocation(locationPsi)
            );

        @NotNull DataTypeApi valueType = mapType.valueType();
        @Nullable TagApi defaultValuesTag = valueType.defaultTag();

        if (defaultValuesTag == null)
          throw new PsiProcessingException(String.format(
              "Can't create default projection for map type '%s, as it's value type '%s' doesn't have a default tag",
              type.name(),
              valueType.name()
          ), locationPsi, context);

        final OpDeleteVarProjection valueVarProjection = createDefaultVarProjection(
            valueType.type(),
            defaultValuesTag,
            false,
            locationPsi,
            context
        );

        return new OpDeleteMapModelProjection(
            mapType,
            params,
            annotations,
            keyProjection,
            valueVarProjection,
            null,
            EpigraphPsiUtil.getLocation(locationPsi)
        );
      case LIST:
        ListTypeApi listType = (ListTypeApi) type;
        @NotNull DataTypeApi elementType = listType.elementType();
        @Nullable TagApi defaultElementsTag = elementType.defaultTag();

        if (defaultElementsTag == null)
          throw new PsiProcessingException(String.format(
              "Can't create default projection for list type '%s, as it's element type '%s' doesn't have a default tag",
              type.name(),
              elementType.name()
          ), locationPsi, context);

        final OpDeleteVarProjection itemVarProjection = createDefaultVarProjection(
            elementType.type(),
            defaultElementsTag,
            false,
            locationPsi,
            context
        );

        return new OpDeleteListModelProjection(
            listType,
            params,
            annotations,
            itemVarProjection,
            null,
            EpigraphPsiUtil.getLocation(locationPsi)
        );
      case ENTITY:
        throw new PsiProcessingException(
            "Was expecting to get datum model kind, got: " + type.kind(),
            locationPsi,
            context
        );
      case ENUM:
        throw new PsiProcessingException("Unsupported type kind: " + type.kind(), locationPsi, context);
      case PRIMITIVE:
        return new OpDeletePrimitiveModelProjection(
            (PrimitiveTypeApi) type,
            params,
            annotations,
            null,
            EpigraphPsiUtil.getLocation(locationPsi)
        );
      default:
        throw new PsiProcessingException("Unknown type kind: " + type.kind(), locationPsi, context);
    }
  }

  public static @NotNull OpDeleteRecordModelProjection parseRecordModelProjection(
      @NotNull RecordTypeApi type,
      @NotNull OpParams params,
      @NotNull Annotations annotations,
      @Nullable List<OpDeleteRecordModelProjection> tails,
      @NotNull SchemaOpDeleteRecordModelProjection psi,
      @NotNull TypesResolver typesResolver,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {

    LinkedHashMap<String, OpDeleteFieldProjectionEntry> fieldProjections = new LinkedHashMap<>();
    @NotNull Iterable<SchemaOpDeleteFieldProjectionEntry> fieldProjectionEntriesPsi =
        psi.getOpDeleteFieldProjectionEntryList();

    for (SchemaOpDeleteFieldProjectionEntry fieldProjectionEntryPsi : fieldProjectionEntriesPsi) {
      final String fieldName = fieldProjectionEntryPsi.getQid().getCanonicalName();
      FieldApi field = type.fieldsMap().get(fieldName);

      if (field == null)
        throw new PsiProcessingException(
            String.format("Can't field projection for '%s', field '%s' not found", type.name(), fieldName),
            fieldProjectionEntryPsi,
            context
        );

      final SchemaOpDeleteFieldProjection fieldProjectionPsi = fieldProjectionEntryPsi.getOpDeleteFieldProjection();
      if (fieldProjectionPsi == null)
        context.addError("Incomplete field '" + fieldName + "' projection", fieldProjectionEntryPsi);
      else {
        fieldProjections.put(
            fieldName,
            new OpDeleteFieldProjectionEntry(
                field,
                parseFieldProjection(
                    field.dataType(),
                    fieldProjectionEntryPsi.getPlus() != null,
                    fieldProjectionPsi,
                    typesResolver,
                    context
                ),
                EpigraphPsiUtil.getLocation(fieldProjectionEntryPsi)
            )
        );
      }
    }

    return new OpDeleteRecordModelProjection(
        type,
        params,
        annotations,
        fieldProjections,
        tails,
        EpigraphPsiUtil.getLocation(psi)
    );
  }

  public static @NotNull OpDeleteFieldProjection parseFieldProjection(
      @NotNull DataTypeApi fieldType,
      boolean canDelete,
      @NotNull SchemaOpDeleteFieldProjection psi,
      @NotNull TypesResolver resolver,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {

//    Collection<OpParam> fieldParamsList = null;
//    @Nullable Map<String, Annotation> fieldAnnotationsMap = null;
//    for (SchemaOpDeleteFieldProjectionBodyPart fieldBodyPart : psi.getOpDeleteFieldProjectionBodyPartList()) {
//      @Nullable SchemaOpParam fieldParamPsi = fieldBodyPart.getOpParam();
//      if (fieldParamPsi != null) {
//        if (fieldParamsList == null) fieldParamsList = new ArrayList<>(3);
//        fieldParamsList.add(parseParameter(fieldParamPsi, resolver, context));
//      }
//
//      fieldAnnotationsMap = parseAnnotation(fieldAnnotationsMap, fieldBodyPart.getAnnotation(), context);
//    }

    return new OpDeleteFieldProjection(
//        OpParams.fromCollection(fieldParamsList),
//        Annotations.fromMap(fieldAnnotationsMap),
        parseVarProjection(fieldType, canDelete, psi.getOpDeleteVarProjection(), resolver, context),
        EpigraphPsiUtil.getLocation(psi)
    );
  }

  public static @NotNull OpDeleteMapModelProjection parseMapModelProjection(
      @NotNull MapTypeApi type,
      @NotNull OpParams params,
      @NotNull Annotations annotations,
      @Nullable List<OpDeleteMapModelProjection> tails,
      @NotNull SchemaOpDeleteMapModelProjection psi,
      @NotNull TypesResolver resolver,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {

    @NotNull OpDeleteKeyProjection keyProjection =
        parseKeyProjection(psi.getOpDeleteKeyProjection(), resolver, context);

    @Nullable SchemaOpDeleteVarProjection valueProjectionPsi = psi.getOpDeleteVarProjection();
    @NotNull OpDeleteVarProjection valueProjection =
        valueProjectionPsi == null
        ? createDefaultVarProjection(
            type.valueType(),
            keyProjection.presence() == OpKeyPresence.REQUIRED,
            psi,
            context
        )
        : parseVarProjection(type.valueType(), psi.getPlus() != null, valueProjectionPsi, resolver, context);

    return new OpDeleteMapModelProjection(
        type,
        params,
        annotations,
        keyProjection,
        valueProjection,
        tails,
        EpigraphPsiUtil.getLocation(psi)
    );
  }

  private static @NotNull OpDeleteKeyProjection parseKeyProjection(
      @NotNull SchemaOpDeleteKeyProjection keyProjectionPsi,
      @NotNull TypesResolver resolver,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {

    final OpKeyPresence presence;

    if (keyProjectionPsi.getForbidden() != null)
      presence = OpKeyPresence.FORBIDDEN;
    else if (keyProjectionPsi.getRequired() != null)
      presence = OpKeyPresence.REQUIRED;
    else
      presence = OpKeyPresence.OPTIONAL;

    List<OpParam> params = null;
    @Nullable Map<String, Annotation> annotationsMap = null;

    for (SchemaOpDeleteKeyProjectionPart keyPart : keyProjectionPsi.getOpDeleteKeyProjectionPartList()) {
      @Nullable SchemaOpParam paramPsi = keyPart.getOpParam();
      if (paramPsi != null) {
        if (params == null) params = new ArrayList<>(3);
        params.add(parseParameter(paramPsi, resolver, context.inputPsiProcessingContext()));
      }

      annotationsMap = parseAnnotation(annotationsMap, keyPart.getAnnotation(), context);
    }

    return new OpDeleteKeyProjection(
        presence,
        params == null ? OpParams.EMPTY : new OpParams(params),
        annotationsMap == null ? Annotations.EMPTY : new Annotations(annotationsMap),
        EpigraphPsiUtil.getLocation(keyProjectionPsi)
    );
  }

  public static @NotNull OpDeleteListModelProjection parseListModelProjection(
      @NotNull ListTypeApi type,
      @NotNull OpParams params,
      @NotNull Annotations annotations,
      @Nullable List<OpDeleteListModelProjection> tails,
      @NotNull SchemaOpDeleteListModelProjection psi,
      @NotNull TypesResolver resolver,
      @NotNull OpDeletePsiProcessingContext context) throws PsiProcessingException {

    OpDeleteVarProjection itemsProjection;
    @Nullable SchemaOpDeleteVarProjection opDeleteVarProjectionPsi = psi.getOpDeleteVarProjection();
    if (opDeleteVarProjectionPsi == null)
      itemsProjection = createDefaultVarProjection(type, true, psi, context);
    else
      itemsProjection =
          parseVarProjection(type.elementType(), psi.getPlus() != null, opDeleteVarProjectionPsi, resolver, context);


    return new OpDeleteListModelProjection(
        type,
        params,
        annotations,
        itemsProjection,
        tails,
        EpigraphPsiUtil.getLocation(psi)
    );
  }

  public static @NotNull OpDeletePrimitiveModelProjection parsePrimitiveModelProjection(
      @NotNull PrimitiveTypeApi type,
      @NotNull OpParams params,
      @NotNull Annotations annotations,
      @Nullable List<OpDeletePrimitiveModelProjection> tails,
      @NotNull PsiElement locationPsi) {

    return new OpDeletePrimitiveModelProjection(
        type,
        params,
        annotations,
        tails,
        EpigraphPsiUtil.getLocation(locationPsi)
    );
  }

}
