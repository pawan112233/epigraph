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

package ws.epigraph.projections.op.input;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ws.epigraph.data.*;
import ws.epigraph.gdata.*;
import ws.epigraph.edl.TypeRefs;
import ws.epigraph.edl.gdata.EdlGDataPsiParser;
import ws.epigraph.edl.parser.psi.*;
import ws.epigraph.lang.TextLocation;
import ws.epigraph.projections.Annotations;
import ws.epigraph.projections.ProjectionUtils;
import ws.epigraph.projections.StepsAndProjection;
import ws.epigraph.projections.op.OpKeyPresence;
import ws.epigraph.projections.op.OpParams;
import ws.epigraph.psi.EpigraphPsiUtil;
import ws.epigraph.psi.PsiProcessingError;
import ws.epigraph.psi.PsiProcessingException;
import ws.epigraph.refs.TypeRef;
import ws.epigraph.refs.TypesResolver;
import ws.epigraph.types.*;
import ws.epigraph.types.TypeKind;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static ws.epigraph.projections.EdlProjectionPsiParserUtil.*;
import static ws.epigraph.projections.ProjectionsParsingUtil.getType;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public final class OpInputProjectionsPsiParser {

  private OpInputProjectionsPsiParser() {}

  public static StepsAndProjection<OpInputVarProjection> parseVarProjection(
      @NotNull DataType dataType,
      @NotNull EdlOpInputVarProjection psi,
      @NotNull TypesResolver typesResolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    final Type type = dataType.type;
    final LinkedHashMap<String, OpInputTagProjectionEntry> tagProjections;

    boolean isDatumType = type.kind() != TypeKind.UNION;

    @Nullable EdlOpInputSingleTagProjection singleTagProjectionPsi = psi.getOpInputSingleTagProjection();
    if (singleTagProjectionPsi == null) {
      @Nullable EdlOpInputMultiTagProjection multiTagProjection = psi.getOpInputMultiTagProjection();
      assert multiTagProjection != null;
      tagProjections = parseMultiTagProjection(dataType, multiTagProjection, typesResolver, errors);
    } else {
      tagProjections = new LinkedHashMap<>();
      Type.Tag tag = findTag(
          type,
          singleTagProjectionPsi.getTagName(),
          dataType.defaultTag,
          singleTagProjectionPsi,
          errors
      );
      if (tag != null || !singleTagProjectionPsi.getText().isEmpty()) {
        final OpInputModelProjection<?, ?, ?> parsedModelProjection;
        if (tag == null) // will throw proper error
          tag = getTag(
              type,
              singleTagProjectionPsi.getTagName(),
              dataType.defaultTag,
              singleTagProjectionPsi,
              errors
          );

        @Nullable EdlOpInputModelProjection modelProjection = singleTagProjectionPsi.getOpInputModelProjection();
        assert modelProjection != null; // can never be null

        @NotNull List<EdlOpInputModelProperty> modelPropertiesPsi =
            singleTagProjectionPsi.getOpInputModelPropertyList();
        parsedModelProjection = parseModelProjection(
            tag.type,
            singleTagProjectionPsi.getPlus() != null || isDatumType, // 'self 'tags on datum projections are required
            getModelDefaultValue(modelPropertiesPsi, errors),
            parseModelParams(modelPropertiesPsi, typesResolver, errors),
            parseModelAnnotations(modelPropertiesPsi, errors),
            parseModelMetaProjection(tag.type, modelPropertiesPsi, typesResolver, errors),
            modelProjection, typesResolver, errors
        ).projection();

        tagProjections.put(
            tag.name(),
            new OpInputTagProjectionEntry(
                tag,
                parsedModelProjection,
                EpigraphPsiUtil.getLocation(singleTagProjectionPsi)
            )
        );
      }
    }

    final List<OpInputVarProjection> tails =
        parseTails(dataType, psi.getOpInputVarPolymorphicTail(), typesResolver, errors);

    try {
      return new StepsAndProjection<>(
          0,
          new OpInputVarProjection(
              type,
              tagProjections,
              singleTagProjectionPsi == null || tagProjections.size() != 1,
              tails,
              EpigraphPsiUtil.getLocation(psi)
          )
      );
    } catch (Exception e) {
      throw new PsiProcessingException(e, psi, errors);
    }
  }

  private static @NotNull LinkedHashMap<String, OpInputTagProjectionEntry> parseMultiTagProjection(
      @NotNull DataType dataType,
      @NotNull EdlOpInputMultiTagProjection multiTagProjection,
      @NotNull TypesResolver typesResolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    final LinkedHashMap<String, OpInputTagProjectionEntry> tagProjections = new LinkedHashMap<>();

    // parse list of tags
    @NotNull List<EdlOpInputMultiTagProjectionItem> tagProjectionPsiList =
        multiTagProjection.getOpInputMultiTagProjectionItemList();

    for (EdlOpInputMultiTagProjectionItem tagProjectionPsi : tagProjectionPsiList) {
      final Type.Tag tag =
          getTag(dataType.type, tagProjectionPsi.getTagName(), dataType.defaultTag, tagProjectionPsi, errors);

      final OpInputModelProjection<?, ?, ?> parsedModelProjection;

      @NotNull DatumType tagType = tag.type;
      @Nullable EdlOpInputModelProjection modelProjection = tagProjectionPsi.getOpInputModelProjection();
      assert modelProjection != null; // todo when it can be null?
      @NotNull List<EdlOpInputModelProperty> modelPropertiesPsi = tagProjectionPsi.getOpInputModelPropertyList();

      parsedModelProjection = parseModelProjection(
          tagType,
          tagProjectionPsi.getPlus() != null,
          getModelDefaultValue(modelPropertiesPsi, errors),
          parseModelParams(modelPropertiesPsi, typesResolver, errors),
          parseModelAnnotations(modelPropertiesPsi, errors),
          parseModelMetaProjection(tagType, modelPropertiesPsi, typesResolver, errors),
          modelProjection, typesResolver, errors
      ).projection();

      tagProjections.put(
          tag.name(),
          new OpInputTagProjectionEntry(
              tag,
              parsedModelProjection,
              EpigraphPsiUtil.getLocation(tagProjectionPsi)
          )
      );
    }

    return tagProjections;
  }

  private static @Nullable List<OpInputVarProjection> parseTails(
      @NotNull DataType dataType,
      @Nullable EdlOpInputVarPolymorphicTail tailPsi,
      @NotNull TypesResolver typesResolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    final List<OpInputVarProjection> tails;

    if (tailPsi == null) tails = null;
    else {
      tails = new ArrayList<>();

      @Nullable EdlOpInputVarSingleTail singleTail = tailPsi.getOpInputVarSingleTail();
      if (singleTail == null) {
        @Nullable EdlOpInputVarMultiTail multiTail = tailPsi.getOpInputVarMultiTail();
        assert multiTail != null;
        for (EdlOpInputVarMultiTailItem tailItem : multiTail.getOpInputVarMultiTailItemList()) {
          @NotNull EdlTypeRef tailTypeRef = tailItem.getTypeRef();
          @NotNull EdlOpInputVarProjection psiTailProjection = tailItem.getOpInputVarProjection();
          @NotNull OpInputVarProjection tailProjection =
              buildTailProjection(
                  dataType,
                  tailTypeRef,
                  psiTailProjection,
                  typesResolver,
                  errors
              );
          tails.add(tailProjection);
        }
      } else {
        @NotNull EdlTypeRef typeRefPsi = singleTail.getTypeRef();
        @NotNull EdlOpInputVarProjection psiTailProjection = singleTail.getOpInputVarProjection();
        @NotNull OpInputVarProjection tailProjection =
            buildTailProjection(dataType, typeRefPsi, psiTailProjection, typesResolver, errors);
        tails.add(tailProjection);
      }

    }

    return tails;
  }

  private static @Nullable GDatum getModelDefaultValue(
      @NotNull List<EdlOpInputModelProperty> modelProperties,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    GDatum result = null;
    for (EdlOpInputModelProperty property : modelProperties) {
      @Nullable EdlOpInputDefaultValue defaultValuePsi = property.getOpInputDefaultValue();
      if (defaultValuePsi != null) {
        if (result == null) {
          @Nullable EdlDatum varValuePsi = defaultValuePsi.getDatum();
          if (varValuePsi != null)
            result = EdlGDataPsiParser.parseDatum(varValuePsi, errors);
        } else {
          errors.add(new PsiProcessingError("Default value should only be specified once", defaultValuePsi));
        }
      }
    }

    return result;
  }

  private static @NotNull OpParams parseModelParams(
      @NotNull List<EdlOpInputModelProperty> modelProperties,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {


    return parseParams(
        modelProperties.stream().map(EdlOpInputModelProperty::getOpParam),
        resolver,
        errors
    );
  }

  private static @NotNull Annotations parseModelAnnotations(
      @NotNull List<EdlOpInputModelProperty> modelProperties,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    return parseAnnotations(
        modelProperties.stream().map(EdlOpInputModelProperty::getAnnotation),
        errors
    );
  }

  private static @Nullable OpInputModelProjection<?, ?, ?> parseModelMetaProjection(
      @NotNull DatumType type,
      @NotNull List<EdlOpInputModelProperty> modelProperties,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors
  ) throws PsiProcessingException {

    @Nullable EdlOpInputModelMeta modelMetaPsi = null;

    for (EdlOpInputModelProperty modelProperty : modelProperties) {
      if (modelMetaPsi == null) {
        modelMetaPsi = modelProperty.getOpInputModelMeta();
      } else {
        errors.add(new PsiProcessingError("Metadata projection should only be specified once", modelProperty));
      }
    }

    if (modelMetaPsi != null) {
      @Nullable DatumType metaType = null; // TODO need a way to extract it from 'type'
      if (metaType == null) {
        errors.add(new PsiProcessingError(
            String.format("Type '%s' doesn't have a metadata, metadata projection can't be specified", type.name()),
            modelMetaPsi
        ));
      } else {
        @NotNull EdlOpInputModelProjection metaProjectionPsi = modelMetaPsi.getOpInputModelProjection();
        return parseModelProjection(
            metaType,
            modelMetaPsi.getPlus() != null,
            null, // TODO do we want to specify defaults for meta?
            OpParams.EMPTY,
            Annotations.EMPTY,
            null, // TODO what if meta-type has it's own meta-type? meta-meta-type projection should go here
            metaProjectionPsi,
            resolver,
            errors
        ).projection();
      }
    }

    return null;
  }

  private static @NotNull OpInputVarProjection buildTailProjection(
      @NotNull DataType dataType,
      @NotNull EdlTypeRef tailTypeRefPsi,
      EdlOpInputVarProjection psiTailProjection,
      @NotNull TypesResolver typesResolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    @NotNull TypeRef tailTypeRef = TypeRefs.fromPsi(tailTypeRefPsi, errors);
    @NotNull Type tailType = getType(tailTypeRef, typesResolver, tailTypeRefPsi, errors);
    return parseVarProjection(
        new DataType(tailType, dataType.defaultTag),
        psiTailProjection,
        typesResolver,
        errors
    ).projection();
  }

  private static @NotNull OpInputVarProjection createDefaultVarProjection(
      @NotNull Type type,
      @NotNull Type.Tag tag,
      boolean required,
      @NotNull PsiElement locationPsi,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {
    return new OpInputVarProjection(
        type,
        ProjectionUtils.singletonLinkedHashMap(
            tag.name(),
            new OpInputTagProjectionEntry(
                tag,
                createDefaultModelProjection(
                    tag.type,
                    required,
                    null,
                    OpParams.EMPTY,
                    Annotations.EMPTY,
                    locationPsi,
                    resolver,
                    errors
                ),
                EpigraphPsiUtil.getLocation(locationPsi)
            )
        ),
        false,
        null,
        EpigraphPsiUtil.getLocation(locationPsi)
    );
  }

  private static @NotNull OpInputVarProjection createDefaultVarProjection(
      @NotNull DatumType type,
      boolean required,
      @NotNull PsiElement locationPsi,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors)
      throws PsiProcessingException {
    return createDefaultVarProjection(type, type.self, required, locationPsi, resolver, errors);
  }

  private static @NotNull OpInputVarProjection createDefaultVarProjection(
      @NotNull DataType type,
      boolean required,
      @NotNull PsiElement locationPsi,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors)
      throws PsiProcessingException {

    @Nullable Type.Tag defaultTag = type.defaultTag;
    if (defaultTag == null)
      throw new PsiProcessingException(
          String.format("Can't build default projection for '%s', default tag not specified", type.name),
          locationPsi,
          errors
      );

    return createDefaultVarProjection(type.type, defaultTag, required, locationPsi, resolver, errors);
  }

  public static @NotNull StepsAndProjection<? extends OpInputModelProjection<?, ?, ?>> parseModelProjection(
      @NotNull DatumType type,
      boolean required,
      @Nullable GDatum defaultValue,
      @NotNull OpParams params,
      @NotNull Annotations annotations,
      @Nullable OpInputModelProjection<?, ?, ?> metaProjection,
      @NotNull EdlOpInputModelProjection psi,
      @NotNull TypesResolver typesResolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    switch (type.kind()) {
      case RECORD:
        @Nullable EdlOpInputRecordModelProjection recordModelProjectionPsi =
            psi.getOpInputRecordModelProjection();

        if (recordModelProjectionPsi == null)
          return new StepsAndProjection<>(
              0,
              createDefaultModelProjection(
                  type,
                  required,
                  defaultValue,
                  params,
                  annotations,
                  psi,
                  typesResolver,
                  errors
              )
          );

        ensureModelKind(psi, TypeKind.RECORD, errors);
        GRecordDatum defaultRecordData = coerceDefault(defaultValue, GRecordDatum.class, psi, errors);

        return parseRecordModelProjection(
            (RecordType) type,
            required,
            defaultRecordData,
            params,
            annotations,
            (OpInputRecordModelProjection) metaProjection,
            recordModelProjectionPsi,
            typesResolver,
            errors
        );

      case MAP:
        @Nullable EdlOpInputMapModelProjection mapModelProjectionPsi = psi.getOpInputMapModelProjection();

        if (mapModelProjectionPsi == null)
          return new StepsAndProjection<>(
              0,
              createDefaultModelProjection(
                  type,
                  required,
                  defaultValue,
                  params,
                  annotations,
                  psi,
                  typesResolver,
                  errors
              )
          );

        ensureModelKind(psi, TypeKind.MAP, errors);
        GMapDatum defaultMapData = coerceDefault(defaultValue, GMapDatum.class, psi, errors);

        return parseMapModelProjection(
            (MapType) type,
            required,
            defaultMapData,
            params,
            annotations,
            (OpInputMapModelProjection) metaProjection,
            mapModelProjectionPsi,
            typesResolver,
            errors
        );

      case LIST:
        @Nullable EdlOpInputListModelProjection listModelProjectionPsi = psi.getOpInputListModelProjection();

        if (listModelProjectionPsi == null)
          return new StepsAndProjection<>(
              0,
              createDefaultModelProjection(
                  type,
                  required,
                  defaultValue,
                  params,
                  annotations,
                  psi,
                  typesResolver,
                  errors
              )
          );

        ensureModelKind(psi, TypeKind.LIST, errors);
        GListDatum defaultListData = coerceDefault(defaultValue, GListDatum.class, psi, errors);

        return parseListModelProjection(
            (ListType) type,
            required,
            defaultListData,
            params,
            annotations,
            (OpInputListModelProjection) metaProjection,
            listModelProjectionPsi,
            typesResolver,
            errors
        );

      case ENUM:
        throw new PsiProcessingException("Unsupported type kind: " + type.kind(), psi, errors);

      case PRIMITIVE:
        GPrimitiveDatum defaultPrimitiveData = coerceDefault(defaultValue, GPrimitiveDatum.class, psi, errors);

        return parsePrimitiveModelProjection(
            (PrimitiveType<?>) type,
            required,
            defaultPrimitiveData,
            params,
            annotations,
            (OpInputPrimitiveModelProjection) metaProjection,
            psi,
            typesResolver,
            errors
        );

      case UNION:
        throw new PsiProcessingException("Unsupported type kind: " + type.kind(), psi, errors);

      default:
        throw new PsiProcessingException("Unknown type kind: " + type.kind(), psi, errors);
    }
  }

  private static void ensureModelKind(
      @NotNull EdlOpInputModelProjection psi,
      @NotNull TypeKind expectedKind,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    @Nullable TypeKind actualKind = findProjectionKind(psi);
    if (expectedKind != actualKind)
      throw new PsiProcessingException(MessageFormat.format(
          "Unexpected projection kind ''{0}'', expected ''{1}''",
          actualKind,
          expectedKind
      ), psi, errors);
  }

  private static @Nullable TypeKind findProjectionKind(@NotNull EdlOpInputModelProjection psi) {
    if (psi.getOpInputRecordModelProjection() != null) return TypeKind.RECORD;
    if (psi.getOpInputMapModelProjection() != null) return TypeKind.MAP;
    if (psi.getOpInputListModelProjection() != null) return TypeKind.LIST;
    return null;
  }

  public static @NotNull OpInputModelProjection<?, ?, ?> createDefaultModelProjection(
      @NotNull DatumType type,
      boolean required,
      @Nullable GDatum defaultValue,
      @NotNull OpParams params,
      @NotNull Annotations annotations,
      @NotNull PsiElement locationPsi,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    @NotNull TextLocation location = EpigraphPsiUtil.getLocation(locationPsi);

    @Nullable Datum defaultDatum = null;
    if (defaultValue != null)
      try {
        defaultDatum = GDataToData.transformDatum(type, defaultValue, resolver);
      } catch (GDataToData.ProcessingException e) {
        throw new PsiProcessingException(e, locationPsi, errors);
      }

    switch (type.kind()) {
      case RECORD:
        return new OpInputRecordModelProjection(
            (RecordType) type,
            required,
            (RecordDatum) defaultDatum,
            params,
            annotations,
            null,
            Collections.emptyMap(),
            location
        );
      case MAP:
        MapType mapType = (MapType) type;

        @NotNull DataType valueType = mapType.valueType();
        Type.@Nullable Tag defaultValuesTag = valueType.defaultTag;

        if (defaultValuesTag == null)
          throw new PsiProcessingException(String.format(
              "Can't create default projection for map type '%s, as it's value type '%s' doesn't have a default tag",
              type.name(),
              valueType.name
          ), locationPsi, errors);

        final OpInputVarProjection valueVarProjection = createDefaultVarProjection(
            valueType.type,
            defaultValuesTag,
            required,
            locationPsi,
            resolver,
            errors
        );

        return new OpInputMapModelProjection(
            mapType,
            required,
            (MapDatum) defaultDatum,
            params,
            annotations,
            null,
            new OpInputKeyProjection(
                OpKeyPresence.OPTIONAL,
                OpParams.EMPTY,
                Annotations.EMPTY,
                location
            ),
            valueVarProjection,
            location
        );
      case LIST:
        ListType listType = (ListType) type;
        @NotNull DataType elementType = listType.elementType();
        Type.@Nullable Tag defaultElementsTag = elementType.defaultTag;

        if (defaultElementsTag == null)
          throw new PsiProcessingException(String.format(
              "Can't create default projection for list type '%s, as it's element type '%s' doesn't have a default tag",
              type.name(),
              elementType.name
          ), locationPsi, errors);

        final OpInputVarProjection itemVarProjection = createDefaultVarProjection(
            elementType.type,
            defaultElementsTag,
            required,
            locationPsi,
            resolver,
            errors
        );

        return new OpInputListModelProjection(
            listType,
            required,
            (ListDatum) defaultDatum,
            params,
            annotations,
            null,
            itemVarProjection,
            location
        );
      case UNION:
        throw new PsiProcessingException(
            "Was expecting to get datum model kind, got: " + type.kind(),
            locationPsi,
            errors
        );
      case ENUM:
        // todo
        throw new PsiProcessingException("Unsupported type kind: " + type.kind(), locationPsi, errors);
      case PRIMITIVE:
        return new OpInputPrimitiveModelProjection(
            (PrimitiveType<?>) type,
            required,
            (PrimitiveDatum<?>) defaultDatum,
            params,
            annotations,
            null,
            location
        );
      default:
        throw new PsiProcessingException("Unknown type kind: " + type.kind(), locationPsi, errors);
    }
  }

  public static @NotNull StepsAndProjection<OpInputRecordModelProjection> parseRecordModelProjection(
      @NotNull RecordType type,
      boolean required,
      @Nullable GRecordDatum defaultValue,
      @NotNull OpParams params,
      @NotNull Annotations annotations,
      @Nullable OpInputRecordModelProjection metaProjection,
      @NotNull EdlOpInputRecordModelProjection psi,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    RecordDatum defaultDatum = null;
    if (defaultValue != null) {
      try {
        defaultDatum = GDataToData.transform(type, defaultValue, resolver);
      } catch (GDataToData.ProcessingException e) {
        throw new PsiProcessingException(e, psi, errors);
      }
    }

    LinkedHashMap<String, OpInputFieldProjectionEntry> fieldProjections = new LinkedHashMap<>();
    @NotNull List<EdlOpInputFieldProjectionEntry> psiFieldProjections = psi.getOpInputFieldProjectionEntryList();

    for (EdlOpInputFieldProjectionEntry fieldProjectionPsi : psiFieldProjections) {
      try {
        final String fieldName = fieldProjectionPsi.getQid().getCanonicalName();
        RecordType.Field field = type.fieldsMap().get(fieldName);
        if (field == null) {
          errors.add(new PsiProcessingError(
                  String.format("Can't build field projection for '%s', field '%s' not found", type.name(), fieldName),
                  fieldProjectionPsi
              )
          );
          continue;
        }

        final @NotNull DataType fieldType = field.dataType();
        final boolean fieldRequired = fieldProjectionPsi.getPlus() != null;

        final OpInputFieldProjection fieldProjection =
            parseFieldProjection(
                fieldType,
                fieldRequired,
                fieldProjectionPsi.getOpInputFieldProjection(),
                resolver,
                errors
            );

        fieldProjections.put(
            fieldName,
            new OpInputFieldProjectionEntry(
                field,
                fieldProjection
                ,
                EpigraphPsiUtil.getLocation(fieldProjectionPsi)
            )
        );
      } catch (PsiProcessingException e) {
        errors.add(e.toError());
      }
    }

    return new StepsAndProjection<>(
        0,
        new OpInputRecordModelProjection(
            type,
            required,
            defaultDatum,
            params,
            annotations,
            metaProjection,
            fieldProjections,
            EpigraphPsiUtil.getLocation(psi)
        )
    );
  }

  public static @NotNull OpInputFieldProjection parseFieldProjection(
      final DataType fieldType,
      final boolean required,
      final @NotNull EdlOpInputFieldProjection psi,
      final @NotNull TypesResolver resolver,
      final @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    @NotNull OpParams fieldParams = parseParams(
        psi.getOpInputFieldProjectionBodyPartList()
            .stream()
            .map(EdlOpInputFieldProjectionBodyPart::getOpParam),
        resolver,
        errors
    );

    @NotNull Annotations fieldAnnotations = parseAnnotations(
        psi.getOpInputFieldProjectionBodyPartList()
            .stream()
            .map(EdlOpInputFieldProjectionBodyPart::getAnnotation),
        errors
    );

    @NotNull EdlOpInputVarProjection psiVarProjection = psi.getOpInputVarProjection();
    OpInputVarProjection varProjection =
        parseVarProjection(fieldType, psiVarProjection, resolver, errors).projection();

    return new OpInputFieldProjection(
        fieldParams,
        fieldAnnotations,
        varProjection,
        required,
        EpigraphPsiUtil.getLocation(psi)
    );
  }

  public static @NotNull StepsAndProjection<OpInputMapModelProjection> parseMapModelProjection(
      @NotNull MapType type,
      boolean required,
      @Nullable GMapDatum defaultValue,
      @NotNull OpParams params,
      @NotNull Annotations annotations,
      @Nullable OpInputMapModelProjection metaProjection,
      @NotNull EdlOpInputMapModelProjection psi,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    MapDatum defaultDatum = null;
    if (defaultValue != null) {
      try {
        defaultDatum = GDataToData.transform(type, defaultValue, resolver);
      } catch (GDataToData.ProcessingException e) {
        throw new PsiProcessingException(e, psi, errors);
      }
    }

    @NotNull OpInputKeyProjection keyProjection = parseKeyProjection(psi.getOpInputKeyProjection(), resolver, errors);

    @Nullable EdlOpInputVarProjection valueProjectionPsi = psi.getOpInputVarProjection();
    @NotNull OpInputVarProjection valueProjection =
        valueProjectionPsi == null ?
        createDefaultVarProjection(type.valueType(), false, psi, resolver, errors) :
        parseVarProjection(type.valueType(), valueProjectionPsi, resolver, errors).projection();

    return new StepsAndProjection<>(
        0,
        new OpInputMapModelProjection(
            type,
            required,
            defaultDatum,
            params,
            annotations,
            metaProjection,
            keyProjection,
            valueProjection,
            EpigraphPsiUtil.getLocation(psi)
        )
    );
  }
  
  private static @NotNull OpInputKeyProjection parseKeyProjection(
      @NotNull EdlOpInputKeyProjection keyProjectionPsi,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    final OpKeyPresence presence;

    if (keyProjectionPsi.getForbidden() != null)
      presence = OpKeyPresence.FORBIDDEN;
    else if (keyProjectionPsi.getRequired() != null)
      presence = OpKeyPresence.REQUIRED;
    else
      presence = OpKeyPresence.OPTIONAL;

    final @NotNull List<EdlOpInputKeyProjectionPart> keyPartsPsi =
        keyProjectionPsi.getOpInputKeyProjectionPartList();

    final @NotNull OpParams keyParams =
        parseParams(keyPartsPsi.stream().map(EdlOpInputKeyProjectionPart::getOpParam), resolver, errors);
    final @NotNull Annotations keyAnnotations =
        parseAnnotations(keyPartsPsi.stream().map(EdlOpInputKeyProjectionPart::getAnnotation), errors);

    return new OpInputKeyProjection(
        presence,
        keyParams,
        keyAnnotations,
        EpigraphPsiUtil.getLocation(keyProjectionPsi)
    );
  }

  public static @NotNull StepsAndProjection<OpInputListModelProjection> parseListModelProjection(
      @NotNull ListType type,
      boolean required,
      @Nullable GListDatum defaultValue,
      @NotNull OpParams params,
      @NotNull Annotations annotations,
      @Nullable OpInputListModelProjection metaProjection,
      @NotNull EdlOpInputListModelProjection psi,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    ListDatum defaultDatum = null;
    if (defaultValue != null) {
      try {
        defaultDatum = GDataToData.transform(type, defaultValue, resolver);
      } catch (GDataToData.ProcessingException e) {
        throw new PsiProcessingException(e, psi, errors);
      }
    }

    OpInputVarProjection itemsProjection;
    @Nullable EdlOpInputVarProjection opInputVarProjectionPsi = psi.getOpInputVarProjection();
    if (opInputVarProjectionPsi == null)
      itemsProjection = createDefaultVarProjection(type, true, psi, resolver, errors);
    else
      itemsProjection = parseVarProjection(type.elementType(), opInputVarProjectionPsi, resolver, errors).projection();

    return new StepsAndProjection<>(
        0,
        new OpInputListModelProjection(
            type,
            required,
            defaultDatum,
            params,
            annotations,
            metaProjection,
            itemsProjection,
            EpigraphPsiUtil.getLocation(psi)
        )
    );
  }

  public static @NotNull StepsAndProjection<OpInputPrimitiveModelProjection> parsePrimitiveModelProjection(
      @NotNull PrimitiveType<?> type,
      boolean required,
      @Nullable GPrimitiveDatum defaultValue,
      @NotNull OpParams params,
      @NotNull Annotations annotations,
      @Nullable OpInputPrimitiveModelProjection metaProjection,
      @NotNull PsiElement locationPsi,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    PrimitiveDatum<?> defaultDatum = null;
    if (defaultValue != null) {
      try {
        defaultDatum = GDataToData.transform(type, defaultValue, resolver);
      } catch (GDataToData.ProcessingException e) {
        throw new PsiProcessingException(e, locationPsi, errors);
      }
    }

    return new StepsAndProjection<>(
        0,
        new OpInputPrimitiveModelProjection(
            type,
            required,
            defaultDatum,
            params,
            annotations,
            metaProjection,
            EpigraphPsiUtil.getLocation(locationPsi)
        )
    );
  }

  @SuppressWarnings("unchecked")
  private static @Nullable <D extends GDatum> D coerceDefault(
      @Nullable GDatum defaultValue,
      Class<D> cls,
      @NotNull PsiElement location,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    if (defaultValue == null) return null;
    if (defaultValue instanceof GNullDatum) return null;
    if (defaultValue.getClass().equals(cls))
      return (D) defaultValue;
    throw new PsiProcessingException(
        String.format("Invalid default value '%s', expected to get '%s'", defaultValue, cls.getName()),
        location,
        errors
    );
  }
}
