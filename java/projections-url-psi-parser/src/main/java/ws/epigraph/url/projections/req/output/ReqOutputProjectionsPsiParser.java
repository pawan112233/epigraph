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

package ws.epigraph.url.projections.req.output;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ws.epigraph.data.Datum;
import ws.epigraph.lang.TextLocation;
import ws.epigraph.projections.Annotations;
import ws.epigraph.projections.ProjectionUtils;
import ws.epigraph.projections.StepsAndProjection;
import ws.epigraph.projections.op.OpKeyPresence;
import ws.epigraph.projections.op.OpParams;
import ws.epigraph.projections.op.output.*;
import ws.epigraph.projections.req.ReqParams;
import ws.epigraph.projections.req.output.*;
import ws.epigraph.psi.EpigraphPsiUtil;
import ws.epigraph.psi.PsiProcessingError;
import ws.epigraph.psi.PsiProcessingException;
import ws.epigraph.refs.TypeRef;
import ws.epigraph.refs.TypesResolver;
import ws.epigraph.types.*;
import ws.epigraph.url.TypeRefs;
import ws.epigraph.url.parser.psi.*;
import ws.epigraph.url.projections.UrlProjectionsPsiParserUtil;

import java.util.*;

import static ws.epigraph.projections.ProjectionsParsingUtil.*;
import static ws.epigraph.url.projections.UrlProjectionsPsiParserUtil.*;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public final class ReqOutputProjectionsPsiParser {
  private ReqOutputProjectionsPsiParser() {}
  // todo error messages listing supported fields/tags should take parents into account:
  // op : ..bestFriend(id,firstName)~User(profileUrl)
  // req: ..bestFriend(id,firstName)~User(x) -- supported fields are (id, firstName, profileUrl)


  public static @NotNull StepsAndProjection<ReqOutputVarProjection> parseTrunkVarProjection(
      @NotNull DataType dataType,
      @NotNull OpOutputVarProjection op,
      @NotNull UrlReqOutputTrunkVarProjection psi,
      @NotNull TypesResolver typesResolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    final Type type = dataType.type;
    final LinkedHashMap<String, ReqOutputTagProjectionEntry> tagProjections;
    final int steps;
    final boolean parenthesized;

    @Nullable UrlReqOutputTrunkSingleTagProjection singleTagProjectionPsi = psi.getReqOutputTrunkSingleTagProjection();
    final @NotNull TypesResolver subResolver = addTypeNamespace(dataType.type, typesResolver);

    if (psi.getReqOutputStarTagProjection() != null) {
      steps = 0;
      parenthesized = true;

      tagProjections = new LinkedHashMap<>();
      addStarTags(op, tagProjections, errors, psi);
    } else if (singleTagProjectionPsi != null) {
      // try to improve error reporting: singleTagProjectionPsi may be empty
      PsiElement tagLocation = getSingleTagLocation(singleTagProjectionPsi);

      tagProjections = new LinkedHashMap<>();

      final ReqOutputModelProjection<?, ?> parsedModelProjection;
      final @Nullable UrlTagName tagNamePsi = singleTagProjectionPsi.getTagName();

      final @NotNull Type.Tag tag = getTagOrDefaultTag(type, tagNamePsi, op, tagLocation, errors);
      @NotNull OpOutputTagProjectionEntry opTagProjection =
          getTagProjection(tag.name(), op, tagLocation, errors);

      @NotNull OpOutputModelProjection<?, ?> opModelProjection = opTagProjection.projection();
      @NotNull UrlReqOutputTrunkModelProjection modelProjectionPsi =
          singleTagProjectionPsi.getReqOutputTrunkModelProjection();

      StepsAndProjection<? extends ReqOutputModelProjection<?, ?>> stepsAndProjection = parseTrunkModelProjection(
          opModelProjection,
          singleTagProjectionPsi.getPlus() != null,
          parseReqParams(singleTagProjectionPsi.getReqParamList(), opModelProjection.params(), subResolver, errors),
          parseAnnotations(singleTagProjectionPsi.getReqAnnotationList(), errors),
          parseModelMetaProjection(
              opModelProjection,
              singleTagProjectionPsi.getReqOutputModelMeta(),
              subResolver,
              errors
          ),
          modelProjectionPsi,
          subResolver,
          errors
      );

      parsedModelProjection = stepsAndProjection.projection();
      steps = stepsAndProjection.pathSteps() + 1;

      tagProjections.put(
          tag.name(),
          new ReqOutputTagProjectionEntry(
              tag,
              parsedModelProjection,
              EpigraphPsiUtil.getLocation(tagLocation)
          )
      );
      parenthesized = false;

    } else {
      @Nullable UrlReqOutputComaMultiTagProjection multiTagProjection = psi.getReqOutputComaMultiTagProjection();
      assert multiTagProjection != null;
      tagProjections = parseComaMultiTagProjection(dataType, op, multiTagProjection, subResolver, errors);
      steps = 0;
      parenthesized = true;
    }

    final List<ReqOutputVarProjection> tails =
        parseTails(dataType, op, psi.getReqOutputVarPolymorphicTail(), subResolver, errors);

    try {
      return new StepsAndProjection<>(
          steps,
          new ReqOutputVarProjection(type, tagProjections, tails, parenthesized, EpigraphPsiUtil.getLocation(psi))
      );
    } catch (Exception e) {
      throw new PsiProcessingException(e, psi, errors);
    }
  }

  private static @NotNull PsiElement
  getSingleTagLocation(final @NotNull UrlReqOutputTrunkSingleTagProjection singleTagProjectionPsi) {
    PsiElement tagLocation = singleTagProjectionPsi;
    if (tagLocation.getText().isEmpty()) {
      final @Nullable UrlReqOutputComaFieldProjection fieldProjectionPsi =
          PsiTreeUtil.getParentOfType(tagLocation, UrlReqOutputComaFieldProjection.class);
      if (fieldProjectionPsi == null) {
        final @Nullable UrlReqOutputTrunkRecordModelProjection recordProjectionPsi =
            PsiTreeUtil.getParentOfType(tagLocation, UrlReqOutputTrunkRecordModelProjection.class);
        if (recordProjectionPsi != null)
          tagLocation = recordProjectionPsi.getQid();
      } else {
        tagLocation = fieldProjectionPsi.getQid();
      }
    }
    return tagLocation;
  }

  private static void addStarTags(
      final @NotNull OpOutputVarProjection op,
      final @NotNull LinkedHashMap<String, ReqOutputTagProjectionEntry> tagProjections,
      final @NotNull List<PsiProcessingError> errors,
      final @NotNull PsiElement locationPsi) throws PsiProcessingException {

    TextLocation location = EpigraphPsiUtil.getLocation(locationPsi);

    for (final Map.Entry<String, OpOutputTagProjectionEntry> entry : op.tagProjections().entrySet()) {
      final Type.Tag tag = entry.getValue().tag();
      final OpOutputModelProjection<?, ?> opModelProjection = entry.getValue().projection();

      tagProjections.put(
          entry.getKey(),
          new ReqOutputTagProjectionEntry(
              tag,
              createDefaultModelProjection(
                  tag.type,
                  false,
                  opModelProjection,
                  ReqParams.EMPTY,
                  Annotations.EMPTY,
                  locationPsi,
                  errors
              ),
              location
          )
      );
    }
  }


  public static StepsAndProjection<ReqOutputVarProjection> parseComaVarProjection(
      @NotNull DataType dataType,
      @NotNull OpOutputVarProjection op,
      @NotNull UrlReqOutputComaVarProjection psi,
      @NotNull TypesResolver typesResolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    final Type type = dataType.type;
    final LinkedHashMap<String, ReqOutputTagProjectionEntry> tagProjections;
    final boolean parenthesized;

    final @NotNull TypesResolver subResolver = addTypeNamespace(dataType.type, typesResolver);
    @Nullable UrlReqOutputComaSingleTagProjection singleTagProjectionPsi = psi.getReqOutputComaSingleTagProjection();

    if (psi.getReqOutputStarTagProjection() != null) {
      parenthesized = true;

      tagProjections = new LinkedHashMap<>();
      addStarTags(op, tagProjections, errors, psi);
    } else if (singleTagProjectionPsi != null) {
      tagProjections = new LinkedHashMap<>();

      Type.Tag tag = findTagOrDefaultTag(type, singleTagProjectionPsi.getTagName(), op, singleTagProjectionPsi, errors);
      if (tag != null || !singleTagProjectionPsi.getText().isEmpty()) {
        if (tag == null) tag =
            getTagOrDefaultTag(type, null, op, singleTagProjectionPsi, errors); // will throw proper error
        @NotNull OpOutputTagProjectionEntry opTagProjection =
            getTagProjection(tag.name(), op, singleTagProjectionPsi, errors);

        @NotNull OpOutputModelProjection<?, ?> opModelProjection = opTagProjection.projection();

        @NotNull UrlReqOutputComaModelProjection modelProjectionPsi =
            singleTagProjectionPsi.getReqOutputComaModelProjection();

        final ReqOutputModelProjection<?, ?> parsedModelProjection = parseComaModelProjection(
            opModelProjection,
            singleTagProjectionPsi.getPlus() != null,
            parseReqParams(singleTagProjectionPsi.getReqParamList(), opModelProjection.params(), subResolver, errors),
            parseAnnotations(singleTagProjectionPsi.getReqAnnotationList(), errors),
            parseModelMetaProjection(
                opModelProjection,
                singleTagProjectionPsi.getReqOutputModelMeta(),
                subResolver,
                errors
            ),
            modelProjectionPsi, subResolver, errors
        );

        tagProjections.put(
            tag.name(),
            new ReqOutputTagProjectionEntry(
                tag,
                parsedModelProjection,
                EpigraphPsiUtil.getLocation(singleTagProjectionPsi)
            )
        );
      }

      parenthesized = false;

    } else {
      @Nullable UrlReqOutputComaMultiTagProjection multiTagProjection = psi.getReqOutputComaMultiTagProjection();
      assert multiTagProjection != null;
      tagProjections = parseComaMultiTagProjection(dataType, op, multiTagProjection, subResolver, errors);
      parenthesized = true;
    }

    final List<ReqOutputVarProjection> tails =
        parseTails(dataType, op, psi.getReqOutputVarPolymorphicTail(), subResolver, errors);

    try {
      return new StepsAndProjection<>(
          0,
          new ReqOutputVarProjection(type, tagProjections, tails, parenthesized, EpigraphPsiUtil.getLocation(psi))
      );
    } catch (Exception e) {
      throw new PsiProcessingException(e, psi, errors);
    }
  }

  private static @NotNull LinkedHashMap<String, ReqOutputTagProjectionEntry> parseComaMultiTagProjection(
      @NotNull DataType dataType,
      @NotNull OpOutputVarProjection op,
      @NotNull UrlReqOutputComaMultiTagProjection psi,
      @NotNull TypesResolver typesResolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    if (!(dataType.type.equals(op.type())))
      throw new PsiProcessingException(
          String.format("Inconsistent arguments. data type: '%s', op type: '%s'", dataType.name, op.type().name()),
          psi,
          errors
      );

    final @NotNull TypesResolver subResolver = addTypeNamespace(dataType.type, typesResolver);

    final LinkedHashMap<String, ReqOutputTagProjectionEntry> tagProjections = new LinkedHashMap<>();

    // parse list of tags
    @NotNull List<UrlReqOutputComaMultiTagProjectionItem> tagProjectionPsiList =
        psi.getReqOutputComaMultiTagProjectionItemList();

    for (UrlReqOutputComaMultiTagProjectionItem tagProjectionPsi : tagProjectionPsiList) {
      try {
        @NotNull Type.Tag tag =
            UrlProjectionsPsiParserUtil.getTag(tagProjectionPsi.getTagName(), op, tagProjectionPsi, errors);
        @NotNull OpOutputTagProjectionEntry opTag = getTagProjection(tag.name(), op, tagProjectionPsi, errors);

        OpOutputModelProjection<?, ?> opTagProjection = opTag.projection();

        @NotNull UrlReqOutputComaModelProjection modelProjection = tagProjectionPsi.getReqOutputComaModelProjection();

        final ReqOutputModelProjection<?, ?> parsedModelProjection = parseComaModelProjection(
            opTagProjection,
            tagProjectionPsi.getPlus() != null,
            parseReqParams(tagProjectionPsi.getReqParamList(), opTagProjection.params(), subResolver, errors),
            parseAnnotations(tagProjectionPsi.getReqAnnotationList(), errors),
            parseModelMetaProjection(opTagProjection, tagProjectionPsi.getReqOutputModelMeta(), subResolver, errors),
            modelProjection, subResolver, errors
        );

        tagProjections.put(
            tag.name(),
            new ReqOutputTagProjectionEntry(
                tag,
                parsedModelProjection,
                EpigraphPsiUtil.getLocation(tagProjectionPsi)
            )
        );
      } catch (PsiProcessingException e) {
        errors.add(e.toError());
      }
    }

    return tagProjections;
  }

  private static @Nullable List<ReqOutputVarProjection> parseTails(
      @NotNull DataType dataType,
      @NotNull OpOutputVarProjection op,
      @Nullable UrlReqOutputVarPolymorphicTail psi,
      @NotNull TypesResolver typesResolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    final List<ReqOutputVarProjection> tails;

    @NotNull TypesResolver subResolver = addTypeNamespace(dataType.type, typesResolver);

    if (psi == null) tails = null;
    else {
      tails = new ArrayList<>();

      @Nullable UrlReqOutputVarSingleTail singleTail = psi.getReqOutputVarSingleTail();
      if (singleTail == null) {
        @Nullable UrlReqOutputVarMultiTail multiTail = psi.getReqOutputVarMultiTail();
        assert multiTail != null;
        Type prevTailType = null;

        for (UrlReqOutputVarMultiTailItem tailItem : multiTail.getReqOutputVarMultiTailItemList()) {
          try {
            if (prevTailType != null)
              subResolver = addTypeNamespace(prevTailType, typesResolver);

            @NotNull UrlTypeRef tailTypeRef = tailItem.getTypeRef();
            @NotNull UrlReqOutputComaVarProjection psiTailProjection = tailItem.getReqOutputComaVarProjection();
            @NotNull ReqOutputVarProjection tailProjection =
                buildTailProjection(dataType, op, tailTypeRef, psiTailProjection, subResolver, errors);
            tails.add(tailProjection);

            prevTailType = tailProjection.type();
          } catch (PsiProcessingException e) {
            errors.add(e.toError());
          }
        }
      } else {
        @NotNull UrlTypeRef tailTypeRef = singleTail.getTypeRef();
        @NotNull UrlReqOutputComaVarProjection psiTailProjection = singleTail.getReqOutputComaVarProjection();
        @NotNull ReqOutputVarProjection tailProjection =
            buildTailProjection(dataType, op, tailTypeRef, psiTailProjection, subResolver, errors);
        tails.add(tailProjection);
      }

    }

    return tails;
  }


  @Contract("_, null, _, _ -> null")
  private static @Nullable ReqOutputModelProjection<?, ?> parseModelMetaProjection(
      @NotNull OpOutputModelProjection<?, ?> op,
      @Nullable UrlReqOutputModelMeta modelMetaPsi,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    if (modelMetaPsi == null) return null;

    OpOutputModelProjection<?, ?> metaOp = op.metaProjection();

    if (metaOp == null) {
      errors.add(
          new PsiProcessingError(
              String.format("Meta projection not supported on type '%s'", op.model().name()),
              modelMetaPsi
          )
      );
      return null;
    }

    // no params/annotations/meta on meta for now

    return parseComaModelProjection(
        metaOp,
        modelMetaPsi.getPlus() != null,
        ReqParams.EMPTY,
        Annotations.EMPTY,
        null,
        modelMetaPsi.getReqOutputComaModelProjection(),
        addTypeNamespace(metaOp.model(), resolver),
        errors
    );
  }

  private static @NotNull ReqOutputVarProjection buildTailProjection(
      @NotNull DataType dataType,
      @NotNull OpOutputVarProjection op,
      @NotNull UrlTypeRef tailTypeRefPsi,
      @NotNull UrlReqOutputComaVarProjection tailProjectionPsi,
      @NotNull TypesResolver typesResolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    @NotNull TypeRef tailTypeRef = TypeRefs.fromPsi(tailTypeRefPsi, errors);
    @NotNull Type tailType = getType(tailTypeRef, typesResolver, tailTypeRefPsi, errors);

    @NotNull OpOutputVarProjection opTail = op.normalizedForType(tailType);

    return parseComaVarProjection(
        new DataType(tailType, dataType.defaultTag),
        opTail,
        tailProjectionPsi,
        typesResolver,
        errors
    ).projection();
  }

  /**
   * Creates default var projection with explicitly specified list of tags
   */
  private static ReqOutputVarProjection createDefaultVarProjection(
      @NotNull Type type,
      @NotNull Iterable<Type.Tag> tags,
      @NotNull OpOutputVarProjection op,
      boolean required,
      @NotNull PsiElement locationPsi,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    LinkedHashMap<String, ReqOutputTagProjectionEntry> tagProjections = new LinkedHashMap<>();

    for (Type.Tag tag : tags) {
      final OpOutputTagProjectionEntry opOutputTagProjection = op.tagProjections().get(tag.name());
      if (opOutputTagProjection != null) {
        tagProjections.put(
            tag.name(),
            new ReqOutputTagProjectionEntry(
                tag,
                createDefaultModelProjection(
                    tag.type,
                    required,
                    opOutputTagProjection.projection(),
                    ReqParams.EMPTY,
                    Annotations.EMPTY,
                    locationPsi,
                    errors
                ),
                EpigraphPsiUtil.getLocation(locationPsi)
            )
        );
      }
    }

    return new ReqOutputVarProjection(
        type,
        tagProjections,
        null,
        op.parenthesized() || tagProjections.size() != 1,
        EpigraphPsiUtil.getLocation(locationPsi)
    );
  }

  private static @NotNull ReqOutputVarProjection createDefaultVarProjection(
      @NotNull Type type,
      @NotNull OpOutputVarProjection op,
      boolean required,
      @NotNull PsiElement locationPsi,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    Type.@Nullable Tag defaultTag = findDefaultTag(type, op, locationPsi, errors);
    Iterable<Type.Tag> tags = defaultTag == null ?
                              Collections.emptyList() :
                              Collections.singletonList(defaultTag);
    return createDefaultVarProjection(type, tags, op, required, locationPsi, errors);
  }


  public static @NotNull ReqOutputVarProjection createDefaultVarProjection(
      @NotNull DataType type,
      @NotNull OpOutputVarProjection op,
      boolean required,
      @NotNull PsiElement locationPsi,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    return createDefaultVarProjection(type.type, op, required, locationPsi, errors);
  }

  public static @NotNull StepsAndProjection<? extends ReqOutputModelProjection<?, ?>> parseTrunkModelProjection(
      @NotNull OpOutputModelProjection<?, ?> op,
      boolean required,
      @NotNull ReqParams params,
      @NotNull Annotations annotations,
      @Nullable ReqOutputModelProjection<?, ?> metaProjection,
      @NotNull UrlReqOutputTrunkModelProjection psi,
      @NotNull TypesResolver typesResolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    final @NotNull TypesResolver subResolver = addTypeNamespace(op.model(), typesResolver);

    //noinspection SwitchStatementWithoutDefaultBranch,EnumSwitchStatementWhichMissesCases
    switch (op.model().kind()) {
      case RECORD:
        @Nullable
        UrlReqOutputTrunkRecordModelProjection trunkRecordProjectionPsi = psi.getReqOutputTrunkRecordModelProjection();

        if (trunkRecordProjectionPsi == null) break;
        else {
          return parseTrunkRecordModelProjection(
              (OpOutputRecordModelProjection) op,
              required,
              params,
              annotations,
              (ReqOutputRecordModelProjection) metaProjection,
              trunkRecordProjectionPsi,
              subResolver,
              errors
          );
        }

      case MAP:
        @Nullable
        UrlReqOutputTrunkMapModelProjection trunkMapProjectionPsi = psi.getReqOutputTrunkMapModelProjection();

        if (trunkMapProjectionPsi == null) break;
        else {
          return parseTrunkMapModelProjection(
              (OpOutputMapModelProjection) op,
              required,
              params,
              annotations,
              (ReqOutputMapModelProjection) metaProjection,
              trunkMapProjectionPsi,
              subResolver,
              errors
          );
        }
    }

    // end of path
    return
        new StepsAndProjection<>(
            0,
            parseComaModelProjection(op, required, params, annotations, metaProjection, psi, subResolver, errors)
        );

  }

  public static @NotNull ReqOutputModelProjection<?, ?> parseComaModelProjection(
      @NotNull OpOutputModelProjection<?, ?> op,
      boolean required,
      @NotNull ReqParams params,
      @NotNull Annotations annotations,
      @Nullable ReqOutputModelProjection<?, ?> metaProjection,
      @NotNull UrlReqOutputComaModelProjection psi,
      @NotNull TypesResolver typesResolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    DatumType model = op.model();
    final @NotNull TypesResolver subResolver = addTypeNamespace(model, typesResolver);

    switch (model.kind()) {
      case RECORD:
        final OpOutputRecordModelProjection opRecord = (OpOutputRecordModelProjection) op;

        @Nullable UrlReqOutputComaRecordModelProjection recordModelProjectionPsi =
            psi.getReqOutputComaRecordModelProjection();

        if (recordModelProjectionPsi == null)
          return createDefaultModelProjection(model, required, opRecord, params, annotations, psi, errors);

        ensureModelKind(findProjectionKind(psi), TypeKind.RECORD, psi, errors);

        return parseComaRecordModelProjection(
            opRecord,
            required,
            params,
            annotations,
            (ReqOutputRecordModelProjection) metaProjection,
            recordModelProjectionPsi,
            subResolver,
            errors
        );

      case MAP:
        final OpOutputMapModelProjection opMap = (OpOutputMapModelProjection) op;
        @Nullable UrlReqOutputComaMapModelProjection mapModelProjectionPsi = psi.getReqOutputComaMapModelProjection();

        if (mapModelProjectionPsi == null)
          return createDefaultModelProjection(model, required, opMap, params, annotations, psi, errors);

        ensureModelKind(findProjectionKind(psi), TypeKind.MAP, psi, errors);

        return parseComaMapModelProjection(
            opMap,
            required,
            params,
            annotations,
            (ReqOutputMapModelProjection) metaProjection,
            mapModelProjectionPsi,
            subResolver,
            errors
        );

      case LIST:
        final OpOutputListModelProjection opList = (OpOutputListModelProjection) op;
        @Nullable UrlReqOutputComaListModelProjection listModelProjectionPsi =
            psi.getReqOutputComaListModelProjection();

        if (listModelProjectionPsi == null)
          return createDefaultModelProjection(model, required, opList, params, annotations, psi, errors);

        ensureModelKind(findProjectionKind(psi), TypeKind.LIST, psi, errors);

        return parseListModelProjection(
            opList,
            required,
            params,
            annotations,
            (ReqOutputListModelProjection) metaProjection,
            listModelProjectionPsi,
            subResolver,
            errors
        );

      case ENUM:
        throw new PsiProcessingException("Unsupported type kind: " + model.kind(), psi, errors);

      case PRIMITIVE:
        return parsePrimitiveModelProjection(
            (PrimitiveType<?>) model,
            required,
            params,
            annotations,
            (ReqOutputPrimitiveModelProjection) metaProjection,
            psi
        );

      case UNION:
        throw new PsiProcessingException("Unsupported type kind: " + model.kind(), psi, errors);

      default:
        throw new PsiProcessingException("Unknown type kind: " + model.kind(), psi, errors);
    }
  }


  private static @Nullable TypeKind findProjectionKind(@NotNull UrlReqOutputComaModelProjection psi) {
    if (psi.getReqOutputComaRecordModelProjection() != null) return TypeKind.RECORD;
    if (psi.getReqOutputComaMapModelProjection() != null) return TypeKind.MAP;
    if (psi.getReqOutputComaListModelProjection() != null) return TypeKind.LIST;
    return null;
  }

  public static @NotNull ReqOutputModelProjection<?, ?> createDefaultModelProjection(
      @NotNull DatumType type,
      boolean required,
      @NotNull OpOutputModelProjection<?, ?> op,
      @NotNull ReqParams params,
      @NotNull Annotations annotations,
      @NotNull PsiElement locationPsi,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    @NotNull TextLocation location = EpigraphPsiUtil.getLocation(locationPsi);

    switch (type.kind()) {
      case RECORD:
        OpOutputRecordModelProjection opRecord = (OpOutputRecordModelProjection) op;
        final Map<String, OpOutputFieldProjectionEntry> opFields = opRecord.fieldProjections();

        final @NotNull Map<String, ReqOutputFieldProjectionEntry> fields;

        if (opFields.isEmpty()) {
          fields = Collections.emptyMap();
        } else {
          fields = new LinkedHashMap<>();

//          for (Map.Entry<String, OpOutputFieldProjectionEntry> entry : opFields.entrySet()) {
//            final OpOutputFieldProjectionEntry opFieldProjectionEntry = entry.getValue();
//            @NotNull final OpOutputFieldProjection opFieldProjection = opFieldProjectionEntry.projection();

//            if (opFieldProjection.includeInDefault()) {
//              final String fieldName = entry.getKey();
//              final RecordType.Field field = opFieldProjectionEntry.field();
//
//              fields.put(
//                  fieldName,
//                  new ReqOutputFieldProjectionEntry(
//                      field,
//                      new ReqOutputFieldProjection(
//                          ReqParams.EMPTY,
//                          Annotations.EMPTY,
//                          createDefaultVarProjection(
//                              field.dataType().type,
//                              opFieldProjection.projection(),
//                              false,
//                              locationPsi,
//                              errors
//                          ),
//                          false,
//                          TextLocation.UNKNOWN
//                      ),
//                      TextLocation.UNKNOWN
//                  )
//              );
//            }
//          }
        }

        return new ReqOutputRecordModelProjection(
            (RecordType) type,
            required,
            params,
            annotations,
            null,
            fields,
            location
        );
      case MAP:
        OpOutputMapModelProjection opMap = (OpOutputMapModelProjection) op;

        if (opMap.keyProjection().presence() == OpKeyPresence.REQUIRED)
          throw new PsiProcessingException(
              String.format("Can't build default projection for '%s': keys are required", type.name()),
              locationPsi,
              errors
          );

        MapType mapType = (MapType) type;
        final ReqOutputVarProjection valueVarProjection = createDefaultVarProjection(
            mapType.valueType(),
            opMap.itemsProjection(),
            required,
            locationPsi,
            errors
        );

        return new ReqOutputMapModelProjection(
            mapType,
            required,
            params,
            annotations,
            null,
            null,
            valueVarProjection,
            location
        );
      case LIST:
        OpOutputListModelProjection opList = (OpOutputListModelProjection) op;
        ListType listType = (ListType) type;

        final ReqOutputVarProjection itemVarProjection = createDefaultVarProjection(
            listType.elementType(),
            opList.itemsProjection(),
            required,
            locationPsi,
            errors
        );

        return new ReqOutputListModelProjection(
            listType,
            required,
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
        return new ReqOutputPrimitiveModelProjection(
            (PrimitiveType<?>) type,
            required,
            params,
            annotations,
            null,
            location
        );
      default:
        throw new PsiProcessingException("Unknown type kind: " + type.kind(), locationPsi, errors);
    }
  }

  public static @NotNull StepsAndProjection<ReqOutputRecordModelProjection> parseTrunkRecordModelProjection(
      @NotNull OpOutputRecordModelProjection op,
      boolean required,
      @NotNull ReqParams params,
      @NotNull Annotations annotations,
      @Nullable ReqOutputRecordModelProjection metaProjection,
      @NotNull UrlReqOutputTrunkRecordModelProjection psi,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    Map<String, OpOutputFieldProjectionEntry> opFields = op.fieldProjections();
    final String fieldName = psi.getQid().getCanonicalName();

    if (opFields.isEmpty())
      throw new PsiProcessingException("No fields are supported by the operation", psi.getQid(), errors);

    OpOutputFieldProjectionEntry opFieldProjectionEntry = opFields.get(fieldName);
    if (opFieldProjectionEntry == null) {
      throw new PsiProcessingException(
          String.format(
              "Unsupported field '%s', supported fields: (%s)",
              fieldName,
              ProjectionUtils.listFields(opFields.keySet())
          ),
          psi.getQid(),
          errors
      );
    }

    final @NotNull RecordType.Field field = opFieldProjectionEntry.field();
    final @NotNull OpOutputFieldProjection opFieldProjection = opFieldProjectionEntry.fieldProjection();

    @NotNull DataType fieldType = field.dataType();

    @Nullable UrlReqOutputTrunkFieldProjection fieldProjectionPsi = psi.getReqOutputTrunkFieldProjection();
    boolean fieldRequired = psi.getPlus() != null;

    @Nullable LinkedHashMap<String, ReqOutputFieldProjectionEntry> fieldProjections = new LinkedHashMap<>();
    final int steps;

    final @NotNull TextLocation fieldLocation = EpigraphPsiUtil.getLocation(psi);

    if (fieldProjectionPsi == null) {
      @Nullable Type.Tag defaultFieldTag = fieldType.defaultTag;
      if (defaultFieldTag == null)
        throw new PsiProcessingException(String.format(
            "Can't construct default projection for field '%s', as it's type '%s' has no default tag",
            fieldName,
            fieldType.name
        ), psi, errors);

      @NotNull ReqOutputVarProjection varProjection = createDefaultVarProjection(
          fieldType.type,
          opFieldProjection.varProjection(),
          required,
          psi,
          errors
      );

      fieldProjections.put(
          fieldName,
          new ReqOutputFieldProjectionEntry(
              field,
              new ReqOutputFieldProjection(
                  ReqParams.EMPTY,
                  Annotations.EMPTY,
                  varProjection,
                  fieldRequired,
                  fieldLocation
              ),
              fieldLocation
          )
      );

      // first step = our field, second step = default var. default var projection is a trunk projection,
      // default model projection is a coma projection
      steps = 2;
    } else {
      @NotNull StepsAndProjection<ReqOutputFieldProjection> fieldStepsAndProjection =
          parseTrunkFieldProjection(
              fieldRequired,
              fieldType,
              opFieldProjection,
              fieldProjectionPsi,
              resolver,
              errors
          );

      fieldProjections.put(
          fieldName,
          new ReqOutputFieldProjectionEntry(
              field,
              fieldStepsAndProjection.projection(),
              EpigraphPsiUtil.getLocation(fieldProjectionPsi)
          )
      );

      steps = fieldStepsAndProjection.pathSteps();
    }

    return new StepsAndProjection<>(
        steps,
        new ReqOutputRecordModelProjection(
            op.model(),
            required,
            params,
            annotations,
            metaProjection,
            fieldProjections,
            fieldLocation
        )
    );
  }

  public static @NotNull StepsAndProjection<ReqOutputFieldProjection> parseTrunkFieldProjection(
      boolean required,
      @NotNull DataType fieldType,
      @NotNull OpOutputFieldProjection op,
      @NotNull UrlReqOutputTrunkFieldProjection psi,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors
  ) throws PsiProcessingException {

    return parseTrunkFieldProjection(required, fieldType, op.params(), op.varProjection(), psi, resolver, errors);
  }

  public static @NotNull StepsAndProjection<ReqOutputFieldProjection> parseTrunkFieldProjection(
      boolean required,
      @NotNull DataType fieldType,
      @Nullable OpParams opParams,
      @NotNull OpOutputVarProjection opVarProjection,
      @NotNull UrlReqOutputTrunkFieldProjection psi,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors
  ) throws PsiProcessingException {

    final int steps;
    final ReqOutputVarProjection varProjection;

    @Nullable UrlReqOutputTrunkVarProjection psiVarProjection = psi.getReqOutputTrunkVarProjection();
    StepsAndProjection<ReqOutputVarProjection> stepsAndProjection =
        parseTrunkVarProjection(fieldType, opVarProjection, psiVarProjection, resolver, errors);

    varProjection = stepsAndProjection.projection();
    steps = stepsAndProjection.pathSteps() + 1;

    return new StepsAndProjection<>(
        steps,
        new ReqOutputFieldProjection(
            parseReqParams(psi.getReqParamList(), opParams, resolver, errors),
            parseAnnotations(psi.getReqAnnotationList(), errors),
            varProjection,
            required,
            EpigraphPsiUtil.getLocation(psi)
        )
    );
  }

  public static @NotNull ReqOutputRecordModelProjection parseComaRecordModelProjection(
      @NotNull OpOutputRecordModelProjection op,
      boolean required,
      @NotNull ReqParams params,
      @NotNull Annotations annotations,
      @Nullable ReqOutputRecordModelProjection metaProjection,
      @NotNull UrlReqOutputComaRecordModelProjection psi,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    LinkedHashMap<String, ReqOutputFieldProjectionEntry> fieldProjections = new LinkedHashMap<>();
    @NotNull List<UrlReqOutputComaFieldProjection> psiFieldProjections = psi.getReqOutputComaFieldProjectionList();

    Map<String, OpOutputFieldProjectionEntry> opFields = op.fieldProjections();

    if (psi.getStar() == null) {
      for (UrlReqOutputComaFieldProjection fieldProjectionPsi : psiFieldProjections) {
        final String fieldName = fieldProjectionPsi.getQid().getCanonicalName();

        @Nullable OpOutputFieldProjectionEntry opFieldProjectionEntry = opFields.get(fieldName);

        if (opFieldProjectionEntry == null)
          errors.add(new PsiProcessingError(
              String.format(
                  "Unsupported field '%s', supported fields: (%s)",
                  fieldName,
                  ProjectionUtils.listFields(opFields.keySet())
              ),
              fieldProjectionPsi
          ));
        else {
          try {
            final @NotNull RecordType.Field field = opFieldProjectionEntry.field();
            final @NotNull OpOutputFieldProjection opFieldProjection = opFieldProjectionEntry.fieldProjection();
            final boolean fieldRequired = fieldProjectionPsi.getPlus() != null;

            ReqParams fieldParams =
                parseReqParams(fieldProjectionPsi.getReqParamList(), opFieldProjection.params(), resolver, errors);

            Annotations fieldAnnotations = parseAnnotations(fieldProjectionPsi.getReqAnnotationList(), errors);

            @Nullable UrlReqOutputComaVarProjection psiVarProjection =
                fieldProjectionPsi.getReqOutputComaVarProjection();
            @NotNull ReqOutputVarProjection varProjection =
                parseComaVarProjection(
                    field.dataType(),
                    opFieldProjection.varProjection(),
                    psiVarProjection,
                    resolver,
                    errors
                ).projection();

            final @NotNull TextLocation fieldLocation = EpigraphPsiUtil.getLocation(fieldProjectionPsi);

            fieldProjections.put(
                fieldName,
                new ReqOutputFieldProjectionEntry(
                    field,
                    new ReqOutputFieldProjection(
                        fieldParams,
                        fieldAnnotations,
                        varProjection,
                        fieldRequired,
                        fieldLocation
                    ),
                    fieldLocation
                )
            );
          } catch (PsiProcessingException e) {
            errors.add(e.toError());
          }
        }
      }
    } else {
      TextLocation location = EpigraphPsiUtil.getLocation(psi.getStar());

      for (final Map.Entry<String, OpOutputFieldProjectionEntry> entry : opFields.entrySet()) {
        final RecordType.Field field = entry.getValue().field();
        final @NotNull OpOutputFieldProjection opFieldProjection = entry.getValue().fieldProjection();

        fieldProjections.put(
            entry.getKey(),
            new ReqOutputFieldProjectionEntry(
                field,
                new ReqOutputFieldProjection(
                    ReqParams.EMPTY,
                    Annotations.EMPTY,
                    createDefaultVarProjection(
                        field.dataType(),
                        opFieldProjection.varProjection(),
                        false,
                        psi.getStar(),
                        errors
                    ),
                    false,
                    location
                ),
                location
            )
        );
      }

    }

    return new ReqOutputRecordModelProjection(
        op.model(),
        required,
        params,
        annotations,
        metaProjection,
        fieldProjections,
        EpigraphPsiUtil.getLocation(psi)
    );
  }

  public static @NotNull StepsAndProjection<ReqOutputMapModelProjection> parseTrunkMapModelProjection(
      @NotNull OpOutputMapModelProjection op,
      boolean required,
      @NotNull ReqParams params,
      @NotNull Annotations annotations,
      @Nullable ReqOutputMapModelProjection metaProjection,
      @NotNull UrlReqOutputTrunkMapModelProjection psi,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    if (op.keyProjection().presence() == OpKeyPresence.FORBIDDEN)
      throw new PsiProcessingException("Map keys are forbidden", psi.getDatum(), errors);

    @NotNull UrlDatum valuePsi = psi.getDatum();
    @Nullable Datum keyValue = getDatum(valuePsi, op.model().keyType(), resolver, "Error processing map key: ", errors);
    if (keyValue == null) throw new PsiProcessingException("Null keys are not allowed", valuePsi, errors);

    ReqOutputKeyProjection keyProjection = new ReqOutputKeyProjection(
        keyValue,
        parseReqParams(psi.getReqParamList(), op.keyProjection().params(), resolver, errors),
        parseAnnotations(psi.getReqAnnotationList(), errors),
        EpigraphPsiUtil.getLocation(psi)
    );

    final int steps;
    final ReqOutputVarProjection valueProjection;

    @Nullable UrlReqOutputTrunkVarProjection valueProjectionPsi = psi.getReqOutputTrunkVarProjection();
    StepsAndProjection<ReqOutputVarProjection> stepsAndProjection =
        parseTrunkVarProjection(op.model().valueType(), op.itemsProjection(), valueProjectionPsi, resolver, errors);

    valueProjection = stepsAndProjection.projection();
    steps = stepsAndProjection.pathSteps() + 1;

    return new StepsAndProjection<>(
        steps,
        new ReqOutputMapModelProjection(
            op.model(),
            required,
            params,
            annotations,
            metaProjection,
            Collections.singletonList(keyProjection),
            valueProjection,
            EpigraphPsiUtil.getLocation(psi)
        )
    );
  }

  public static @NotNull ReqOutputMapModelProjection parseComaMapModelProjection(
      @NotNull OpOutputMapModelProjection op,
      boolean required,
      @NotNull ReqParams params,
      @NotNull Annotations annotations,
      @Nullable ReqOutputMapModelProjection metaProjection,
      @NotNull UrlReqOutputComaMapModelProjection psi,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    @NotNull UrlReqOutputComaKeysProjection keysProjectionPsi = psi.getReqOutputComaKeysProjection();

    final @NotNull OpOutputKeyProjection opKeyProjection = op.keyProjection();
    final List<ReqOutputKeyProjection> keyProjections;

    if (keysProjectionPsi.getStar() == null) {
      if (opKeyProjection.presence() == OpKeyPresence.FORBIDDEN)
        throw new PsiProcessingException("Map keys are forbidden", keysProjectionPsi, errors);

      final int keysSize = keysProjectionPsi.getReqOutputComaKeyProjectionList().size();
      keyProjections = new ArrayList<>(keysSize);
      for (UrlReqOutputComaKeyProjection keyProjectionPsi : keysProjectionPsi.getReqOutputComaKeyProjectionList()) {

        try {
          @NotNull UrlDatum valuePsi = keyProjectionPsi.getDatum();
          @Nullable Datum keyValue =
              getDatum(valuePsi, op.model().keyType(), resolver, "Error processing map key: ", errors);

          if (keyValue == null) errors.add(new PsiProcessingError("Null keys are not allowed", valuePsi));
          else {
            keyProjections.add(
                new ReqOutputKeyProjection(
                    keyValue,
                    parseReqParams(keyProjectionPsi.getReqParamList(), opKeyProjection.params(), resolver, errors),
                    parseAnnotations(keyProjectionPsi.getReqAnnotationList(), errors),
                    EpigraphPsiUtil.getLocation(keyProjectionPsi)
                )
            );
          }

        } catch (PsiProcessingException e) {
          errors.add(e.toError());
        }
      }
    } else {
      keyProjections = null;
      if (opKeyProjection.presence() == OpKeyPresence.REQUIRED)
        throw new PsiProcessingException("Map keys are required", keysProjectionPsi.getStar(), errors);
    }

    @Nullable UrlReqOutputComaVarProjection valueProjectionPsi = psi.getReqOutputComaVarProjection();
    final @NotNull ReqOutputVarProjection valueProjection;
    if (valueProjectionPsi == null) {
      valueProjection = createDefaultVarProjection(
          op.model().valueType().type,
          op.itemsProjection(),
          false,
          psi,
          errors
      );
    } else {
      valueProjection = parseComaVarProjection(
          op.model().valueType(),
          op.itemsProjection(),
          valueProjectionPsi,
          resolver,
          errors
      ).projection();
    }


    return new ReqOutputMapModelProjection(
        op.model(),
        required,
        params,
        annotations,
        metaProjection,
        keyProjections,
        valueProjection,
        EpigraphPsiUtil.getLocation(psi)
    );
  }

  public static @NotNull ReqOutputListModelProjection parseListModelProjection(
      @NotNull OpOutputListModelProjection op,
      boolean required,
      @NotNull ReqParams params,
      @NotNull Annotations annotations,
      @Nullable ReqOutputListModelProjection metaProjection,
      @NotNull UrlReqOutputComaListModelProjection psi,
      @NotNull TypesResolver resolver,
      @NotNull List<PsiProcessingError> errors) throws PsiProcessingException {

    ReqOutputVarProjection itemsProjection;
    @Nullable UrlReqOutputComaVarProjection reqOutputVarProjectionPsi = psi.getReqOutputComaVarProjection();
    if (reqOutputVarProjectionPsi == null)
      itemsProjection = createDefaultVarProjection(op.model().elementType(), op.itemsProjection(), true, psi, errors);
    else
      itemsProjection =
          parseComaVarProjection(
              op.model().elementType(),
              op.itemsProjection(),
              reqOutputVarProjectionPsi,
              resolver,
              errors
          ).projection();


    return new ReqOutputListModelProjection(
        op.model(),
        required,
        params,
        annotations,
        metaProjection,
        itemsProjection,
        EpigraphPsiUtil.getLocation(psi)
    );
  }

  public static @NotNull ReqOutputPrimitiveModelProjection parsePrimitiveModelProjection(
      @NotNull PrimitiveType<?> type,
      boolean required,
      @NotNull ReqParams params,
      @NotNull Annotations annotations,
      @Nullable ReqOutputPrimitiveModelProjection metaProjection,
      @NotNull PsiElement locationPsi) {

    return new ReqOutputPrimitiveModelProjection(
        type,
        required,
        params,
        annotations,
        metaProjection,
        EpigraphPsiUtil.getLocation(locationPsi)
    );
  }

}
