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

package ws.epigraph.projections;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ws.epigraph.projections.gen.GenModelProjection;
import ws.epigraph.projections.gen.GenTagProjectionEntry;
import ws.epigraph.projections.gen.GenVarProjection;
import ws.epigraph.projections.gen.ProjectionReferenceName;
import ws.epigraph.psi.PsiProcessingContext;
import ws.epigraph.psi.PsiProcessingException;
import ws.epigraph.refs.TypeRef;
import ws.epigraph.refs.TypesResolver;
import ws.epigraph.types.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public final class ProjectionsParsingUtil {

  private ProjectionsParsingUtil() {}

  public static @NotNull TagApi getTag(
      @NotNull TypeApi type,
      @Nullable String tagName,
      @Nullable TagApi defaultTag,
      @NotNull PsiElement location,
      @NotNull PsiProcessingContext context) throws PsiProcessingException {

    final TagApi tag = findTag(type, tagName, defaultTag, location, context);

    if (tag == null)
      throw new PsiProcessingException(
          String.format("Can't parse default tag projection for '%s', default tag not specified", type.name()),
          location,
          context
      );
    return tag;
  }

  public static @Nullable TagApi findTag(
      @NotNull TypeApi type,
      @Nullable String tagName,
      @Nullable TagApi defaultTag,
      @NotNull PsiElement location,
      @NotNull PsiProcessingContext context) throws PsiProcessingException {

    final TagApi tag;

    if (tagName == null) {
      // get self tag
      if (defaultTag == null) {
        TagApi selfTag = findSelfTag(type, null, location, context);

        if (selfTag == null)
          return null;

        tag = selfTag;
      } else
        tag = defaultTag;

      verifyTag(type, tag, location, context);
    } else tag = getTag(type, tagName, location, context);
    return tag;
  }

  public static @NotNull TagApi getTag(
      @NotNull TypeApi type,
      @NotNull String tagName,
      @NotNull PsiElement location,
      @NotNull PsiProcessingContext context) throws PsiProcessingException {

    TagApi tag = type.tagsMap().get(tagName);
    if (tag == null)
      throw new PsiProcessingException(
          String.format("Unknown tag '%s' in type '%s', known tags: (%s)", tagName, type.name(), listTags(type)),
          location, context
      );
    return tag;
  }

  public static void verifyTag(
      @NotNull TypeApi type,
      @NotNull TagApi tag,
      @NotNull PsiElement location,
      @NotNull PsiProcessingContext context) throws PsiProcessingException {

    if (!type.tags().contains(tag))
      throw new PsiProcessingException(
          String.format(
              "Tag '%s' doesn't belong to type '%s', known tags: (%s)",
              tag.name(),
              type.name(),
              listTags(type)
          ), location, context);
  }

  public static <VP extends GenVarProjection<VP, ?, ?>> void verifyData(
      @NotNull DataTypeApi dataType,
      @NotNull VP varProjection,
      @NotNull PsiElement location,
      @NotNull PsiProcessingContext context) throws PsiProcessingException {

    if (!varProjection.type().isAssignableFrom(dataType.type())) {
      final ProjectionReferenceName projectionName = varProjection.referenceName();
      final String message;

      if (projectionName == null)
        message = String.format(
            "Projection type '%s' is not compatible with type '%s'",
            varProjection.type().name(), dataType.type().name()
        );
      else
        message = String.format(
            "Projection '%s' type '%s' is not compatible with type '%s'",
            projectionName, varProjection.type().name(), dataType.type().name()
        );

      //context.addError(message, location);
      throw new PsiProcessingException(message, location, context);
    }
  }

  private static String listTags(@NotNull TypeApi type) {
    return type.tags().stream().map(TagApi::name).collect(Collectors.joining(","));
  }

  public static <VP extends GenVarProjection<?, ?, ?>> String listTags(@NotNull VP op) {
    return String.join(", ", op.tagProjections().keySet());
  }

  public static @NotNull TypeApi getType(
      @NotNull TypeRef typeRef,
      @NotNull TypesResolver resolver,
      @NotNull PsiElement location,
      @NotNull PsiProcessingContext context) throws PsiProcessingException {

    @Nullable TypeApi type = typeRef.resolve(resolver);
    if (type == null)
      throw new PsiProcessingException(String.format("Can't find type '%s'", typeRef.toString()), location, context);
    return type;
  }


  public static @NotNull UnionTypeApi getUnionType(
      @NotNull TypeRef typeRef,
      @NotNull TypesResolver resolver,
      @NotNull PsiElement location,
      @NotNull PsiProcessingContext context) throws PsiProcessingException {

    TypeApi type = getType(typeRef, resolver, location, context);
    if (type instanceof UnionTypeApi)
      return (UnionTypeApi) type;

    throw new PsiProcessingException(
        String.format("Expected '%s' to be a var type, but actual kind is '%s'",
            type.name().toString(), type.kind().toString()
        ), location, context);
  }

  public static @NotNull DatumTypeApi getDatumType(
      @NotNull TypeRef typeRef,
      @NotNull TypesResolver resolver,
      @NotNull PsiElement location,
      @NotNull PsiProcessingContext context) throws PsiProcessingException {

    TypeApi type = getType(typeRef, resolver, location, context);
    if (type instanceof DatumTypeApi)
      return (DatumTypeApi) type;

    throw new PsiProcessingException(
        String.format("Expected '%s' to be a non-var type, but actual kind is '%s'",
            type.name().toString(), type.kind().toString()
        ), location, context);
  }

  /**
   * Finds tag projection by tag name
   */
  public static @NotNull <
      MP extends GenModelProjection<?, ?, ?, ?>,
      TP extends GenTagProjectionEntry<TP, MP>,
      VP extends GenVarProjection<VP, TP, MP>
      >
  TP getTagProjection(
      @NotNull String tagName,
      @NotNull VP op,
      @NotNull PsiElement location,
      @NotNull PsiProcessingContext context) throws PsiProcessingException {
    final TP tagProjection = op.tagProjections().get(tagName);
    if (tagProjection == null) {
      throw new PsiProcessingException(
          String.format("Tag '%s' is not supported, supported tags: {%s}", tagName, listTags(op)), location, context);
    }
    return tagProjection;
  }

  /**
   * Finds default tags for a given {@code type}
   * <p>
   * If it's a {@code DatumType}, then default tag is {@code self}, provided that {@code op} contains it.
   */
  public static @Nullable <
      MP extends GenModelProjection<?, ?, ?, ?>,
      TP extends GenTagProjectionEntry<TP, MP>,
      VP extends GenVarProjection<VP, TP, MP>
      >
  TagApi findSelfTag(
      @NotNull TypeApi type,
      @Nullable VP op,
      @NotNull PsiElement locationPsi,
      @NotNull PsiProcessingContext context) throws PsiProcessingException {

    if (type.kind() != TypeKind.UNION) {
      DatumTypeApi datumType = (DatumTypeApi) type;
      final @NotNull TagApi self = datumType.self();
      if (op != null) getTagProjection(self.name(), op, locationPsi, context); // check that op contains it
      return self;
    }

    return null;
  }

  public static <VP extends GenVarProjection<VP, ?, ?>> @NotNull VP getTail(
      @NotNull VP vp,
      @NotNull UnionTypeApi targetType,
      @NotNull PsiElement location,
      @NotNull PsiProcessingContext ctx) {

    if (targetType.equals(vp.type())) return vp;

    if (!hasTail(vp, targetType)) {
      ctx.addError(
          String.format(
              "Polymorphic tail for type '%s' is not supported. Supported tail types: {%s}",
              targetType.name(),
              String.join(", ", supportedVarTailTypes(vp))
          ),
          location
      );
    }

    return vp.normalizedForType(targetType, true);
  }

  @SuppressWarnings("unchecked")
  public static <VP extends GenVarProjection<VP, ?, ?>> boolean hasTail(
      @NotNull VP vp,
      @NotNull UnionTypeApi tailType) {

    if (vp.tailByType(tailType) != null) return true;
    final List<?> tails = vp.polymorphicTails();
    return tails != null && tails.stream().anyMatch(t -> hasTail((VP) t, tailType));
  }

  public static @NotNull List<String> supportedVarTailTypes(@NotNull GenVarProjection<?, ?, ?> vp) {
    if (vp.polymorphicTails() == null) return Collections.emptyList();
    Set<String> acc = new HashSet<>();
    supportedVarTailTypes(vp, acc);
    List<String> res = new ArrayList<>(acc);
    Collections.sort(res);
    return res;
  }

  @SuppressWarnings("unchecked")
  private static void supportedVarTailTypes(@NotNull GenVarProjection<?, ?, ?> vp, Set<String> acc) {
    final List<GenVarProjection<?, ?, ?>> tails = (List<GenVarProjection<?, ?, ?>>) vp.polymorphicTails();
    if (tails != null)
      tails.stream().map(t -> t.type().name().toString()).forEach(acc::add);
  }

  @SuppressWarnings("unchecked")
  public static <MP extends GenModelProjection<?, ?, ?, ?>> @NotNull MP getTail(
      @NotNull MP mp,
      @NotNull DatumTypeApi targetType,
      @NotNull PsiElement location,
      @NotNull PsiProcessingContext ctx) {

    if (targetType.equals(mp.type())) return mp;

    if (!hasTail(mp, targetType)) {
      ctx.addError(
          String.format(
              "Polymorphic tail for type '%s' is not supported. Supported tail types: {%s}",
              targetType.name(),
              String.join(", ", supportedModelTailTypes(mp))
          ),
          location
      );
    }

    return (MP) mp.normalizedForType(targetType, true);
  }

  @SuppressWarnings("unchecked")
  public static <MP extends GenModelProjection<?, ?, ?, ?>> boolean hasTail(
      @NotNull MP mp,
      @NotNull DatumTypeApi tailType) {

    if (mp.tailByType(tailType) != null) return true;
    final List<?> tails = mp.polymorphicTails();
    return tails != null && tails.stream().anyMatch(t -> hasTail((MP) t, tailType));
  }

  public static @NotNull List<String> supportedModelTailTypes(@NotNull GenModelProjection<?, ?, ?, ?> vp) {
    if (vp.polymorphicTails() == null) return Collections.emptyList();
    Set<String> acc = new HashSet<>();
    supportedModelTailTypes(vp, acc);
    List<String> res = new ArrayList<>(acc);
    Collections.sort(res);
    return res;
  }

  @SuppressWarnings("unchecked")
  private static void supportedModelTailTypes(@NotNull GenModelProjection<?, ?, ?, ?> vp, Set<String> acc) {
    final List<GenModelProjection<?, ?, ?, ?>> tails = (List<GenModelProjection<?, ?, ?, ?>>) vp.polymorphicTails();
    if (tails != null)
      tails.stream().map(t -> t.type().name().toString()).forEach(acc::add);
  }

  public static void checkTailType(
      @NotNull UnionTypeApi tailType,
      @NotNull DataTypeApi dataType,
      @NotNull PsiElement tailTypeRefPsi,
      @NotNull PsiProcessingContext context) throws PsiProcessingException {

    if (!dataType.type().isAssignableFrom(tailType))
      throw new PsiProcessingException(
          String.format(
              "Tail type '%s' is not compatible with type '%s'",
              tailType.name(), dataType.type().name()
          ),
          tailTypeRefPsi,
          context
      );
  }
}
