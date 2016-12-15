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

package ws.epigraph.projections.op.path;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import ws.epigraph.psi.EpigraphPsiUtil;
import ws.epigraph.psi.PsiProcessingException;
import ws.epigraph.refs.SimpleTypesResolver;
import ws.epigraph.refs.TypesResolver;
import ws.epigraph.edl.parser.EdlSubParserDefinitions;
import ws.epigraph.edl.parser.psi.EdlOpVarPath;
import ws.epigraph.test.TestUtil;
import ws.epigraph.tests.*;
import ws.epigraph.types.DataType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ws.epigraph.test.TestUtil.*;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class OpOutputPathTest {
  @Test
  public void testParseEmpty() throws PsiProcessingException {
    testVarPathParsing("");
  }

  @Test
  public void testParseTag() throws PsiProcessingException {
    testVarPathParsingErr(":id");
  }

  @Test
  public void testParseParam() throws PsiProcessingException {
    testVarPathParsing(
        ":`record` { ;foo: epigraph.String } / id { ;+param: map[epigraph.String,ws.epigraph.tests.Person] []( :id ) = ( \"foo\": < id: 123 > ) { deprecated = true } }"
        ,
        lines(
            ":`record` { ;foo: epigraph.String }",
            "  /",
            "    id {",
            "      ;+param: map[epigraph.String,ws.epigraph.tests.Person] []( :id ) = ( \"foo\": < id: 123 > ) { deprecated = true }",
            "    }"
        )
    );
  }


  @Test
  public void testParseRecordFieldsWithStructure() throws PsiProcessingException {
    testVarPathParsing(":`record` / bestFriend :`record` / id");
  }

  @Test
  public void testParseRecordFieldsWithCustomParams() throws PsiProcessingException {
    testVarPathParsing(":`record` / bestFriend { deprecated = true } :`record` / id");
  }

  @Test
  public void testParseMap() throws PsiProcessingException {
    testVarPathParsing(":`record` / friendsMap { ;param: epigraph.String } / . :`record` / id");
  }

  private void testVarPathParsingErr(String str) {
    try {
      testVarPathParsing(
          new DataType(Person.type, Person.id),
          str
          ,
          str
      );
      fail();
    } catch (PsiProcessingException ignored) {
    }
  }

  private void testVarPathParsing(String str) throws PsiProcessingException {
    testVarPathParsing(
        str
        ,
        str
    );
  }

  private void testVarPathParsing(String str, String expected) throws PsiProcessingException {
    testVarPathParsing(
        new DataType(Person.type, Person.id),
        str
        ,
        expected
    );
  }

  private void testVarPathParsing(DataType varDataType, String projectionString, String expected)
      throws PsiProcessingException {

    OpVarPath varPath = parseOpVarPath(varDataType, projectionString);

    String actual = TestUtil.printOpVarPath(varPath);

    assertEquals("\n" + actual, expected, actual);
//    assertEquals(expected.trim(), actual.trim());
  }

  private OpVarPath parseOpVarPath(DataType varDataType, String projectionString) throws PsiProcessingException {

    TypesResolver
        resolver = new SimpleTypesResolver(
        PersonId.type,
        Person.type,
        User.type,
        UserId.type,
        UserRecord.type,
        String_Person_Map.type,
        epigraph.String.type
    );

    return parseOpVarPath(varDataType, projectionString, false, resolver);
  }

  public static @NotNull OpVarPath parseOpVarPath(
      @NotNull DataType varDataType,
      @NotNull String projectionString,
      boolean catchPsiErrors,
      @NotNull TypesResolver resolver) throws PsiProcessingException {

    EpigraphPsiUtil.ErrorsAccumulator errorsAccumulator = new EpigraphPsiUtil.ErrorsAccumulator();

    EdlOpVarPath psiVarProjection = EpigraphPsiUtil.parseText(
        projectionString,
        EdlSubParserDefinitions.OP_VAR_PATH,
        errorsAccumulator
    );

    failIfHasErrors(psiVarProjection, errorsAccumulator);

    final TestUtil.PsiParserClosure<OpVarPath> closure =
        errors -> OpPathPsiParser.parseVarPath(varDataType, psiVarProjection, resolver, errors);

    return catchPsiErrors ? runPsiParser(closure) : runPsiParserNotCatchingErrors(closure);
  }

}
