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

package ws.epigraph.edl.gdata;

import org.jetbrains.annotations.Contract;
import ws.epigraph.gdata.*;
import ws.epigraph.edl.TypeRefs;
import ws.epigraph.edl.parser.psi.*;
import ws.epigraph.psi.EpigraphPsiUtil;
import ws.epigraph.psi.PsiProcessingError;
import ws.epigraph.psi.PsiProcessingException;
import ws.epigraph.refs.TypeRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public final class EdlGDataPsiParser {
  private EdlGDataPsiParser() {}

  public static @NotNull GDataValue parseValue(@NotNull EdlDataValue psi, @NotNull List<PsiProcessingError> errors)
      throws PsiProcessingException {
    if (psi.getData() != null) return parseData(psi.getData(), errors);
    else if (psi.getDatum() != null) return parseDatum(psi.getDatum(), errors);
    else throw new PsiProcessingException("Neither data nor datum is set", psi, errors);
  }

  public static @NotNull GData parseData(@NotNull EdlData psi, @NotNull List<PsiProcessingError> errors)
      throws PsiProcessingException {
    @Nullable EdlTypeRef typeRef = psi.getTypeRef();

    LinkedHashMap<String, GDatum> tags = new LinkedHashMap<>();
    for (EdlDataEntry entry : psi.getDataEntryList()) {
      @Nullable EdlDatum value = entry.getDatum();
      if (value == null) throw new PsiProcessingException(
          String.format("Got 'null' value for tag '%s'", entry.getQid().getCanonicalName()), psi, errors
      );
      else tags.put(entry.getQid().getCanonicalName(), parseDatum(value, errors));
    }

    return new GData(getTypeRef(typeRef, errors), tags, EpigraphPsiUtil.getLocation(psi));
  }

  public static @NotNull GDatum parseDatum(@NotNull EdlDatum psi, @NotNull List<PsiProcessingError> errors)
      throws PsiProcessingException {

    if (psi instanceof EdlRecordDatum)
      return parseRecord((EdlRecordDatum) psi, errors);
    else if (psi instanceof EdlMapDatum)
      return parseMap((EdlMapDatum) psi, errors);
    else if (psi instanceof EdlListDatum)
      return parseList((EdlListDatum) psi, errors);
    else if (psi instanceof EdlEnumDatum)
      return parseEnum((EdlEnumDatum) psi);
    else if (psi instanceof EdlPrimitiveDatum)
      return parsePrimitive((EdlPrimitiveDatum) psi, errors);
    else if (psi instanceof EdlNullDatum)
      return parseNull((EdlNullDatum) psi, errors);
    else throw new PsiProcessingException("Unknown value element", psi, errors);
  }

  public static @NotNull GRecordDatum parseRecord(@NotNull EdlRecordDatum psi, @NotNull List<PsiProcessingError> errors)
      throws PsiProcessingException {
    @Nullable EdlTypeRef typeRef = psi.getTypeRef();

    LinkedHashMap<String, GDataValue> fields = new LinkedHashMap<>();
    for (EdlRecordDatumEntry entry : psi.getRecordDatumEntryList()) {
      try {
        @Nullable EdlDataValue value = entry.getDataValue();
        if (value == null) errors.add(new PsiProcessingError(
            String.format("Got 'null' value for field '%s'", entry.getQid().getCanonicalName()), psi
        ));
        else fields.put(entry.getQid().getCanonicalName(), parseValue(value, errors));
      } catch (PsiProcessingException e) {
        errors = e.errors();
      }
    }

    return new GRecordDatum(getTypeRef(typeRef, errors), fields, EpigraphPsiUtil.getLocation(psi));
  }

  public static @NotNull GMapDatum parseMap(@NotNull EdlMapDatum psi, @NotNull List<PsiProcessingError> errors)
      throws PsiProcessingException {
    @Nullable EdlTypeRef typeRef = psi.getTypeRef();

    LinkedHashMap<GDatum, GDataValue> map = new LinkedHashMap<>();
    for (EdlMapDatumEntry entry : psi.getMapDatumEntryList()) {
      try {
        @Nullable EdlDataValue dataValue = entry.getDataValue();
        if (dataValue == null) errors.add(new PsiProcessingError(
            String.format("Got 'null' value for key '%s'", entry.getDataValue().getText()), psi
        ));
        else map.put(parseDatum(entry.getDatum(), errors), parseValue(dataValue, errors));
      } catch (PsiProcessingException e) {
        errors = e.errors();
      }
    }

    return new GMapDatum(getTypeRef(typeRef, errors), map, EpigraphPsiUtil.getLocation(psi));
  }

  public static @NotNull GListDatum parseList(@NotNull EdlListDatum psi, @NotNull List<PsiProcessingError> errors)
      throws PsiProcessingException {

    @Nullable EdlTypeRef typeRef = psi.getTypeRef();

    final List<GDataValue> items = new ArrayList<>();

    for (EdlDataValue value : psi.getDataValueList())
      try {
        items.add(parseValue(value, errors));
      } catch (PsiProcessingException e) {
        errors = e.errors();
      }

    return new GListDatum(getTypeRef(typeRef, errors), items, EpigraphPsiUtil.getLocation(psi));
  }

  public static @NotNull GDataEnum parseEnum(@NotNull EdlEnumDatum psi) {
    return new GDataEnum(psi.getQid().getCanonicalName(), EpigraphPsiUtil.getLocation(psi));
  }

  public static @NotNull GPrimitiveDatum parsePrimitive(@NotNull EdlPrimitiveDatum psi, @NotNull List<PsiProcessingError> errors)
      throws PsiProcessingException {

    @Nullable EdlTypeRef typeRef = psi.getTypeRef();

    final Object value;
    if (psi.getString() != null) {
      String text = psi.getString().getText();
      value = text.substring(1, text.length() - 1);
    } else if (psi.getBoolean() != null) {
      value = Boolean.valueOf(psi.getBoolean().getText());
    } else if (psi.getNumber() != null) {
      // todo make it stable and always parse as Double?
      String text = psi.getNumber().getText();
      if (text.contains(".")) value = Double.valueOf(text);
      else value = Long.valueOf(text);
    } else
      throw new PsiProcessingException(
          String.format("Don't know how to handle primitive '%s'", psi.getText()),
          psi,
          errors
      );

    return new GPrimitiveDatum(getTypeRef(typeRef, errors), value, EpigraphPsiUtil.getLocation(psi));
  }

  public static @NotNull GNullDatum parseNull(@NotNull EdlNullDatum psi, @NotNull List<PsiProcessingError> errors)
      throws PsiProcessingException {
    @Nullable EdlTypeRef typeRef = psi.getTypeRef();
    return new GNullDatum(getTypeRef(typeRef, errors), EpigraphPsiUtil.getLocation(psi));
  }

  @Contract("null, _ -> null; !null, _ -> !null")
  private static @Nullable TypeRef getTypeRef(EdlTypeRef typeRef, @NotNull List<PsiProcessingError> errors)
      throws PsiProcessingException {
    return typeRef == null ? null : TypeRefs.fromPsi(typeRef, errors);
  }
}
