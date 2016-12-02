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

package ws.epigraph.url.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import ws.epigraph.idl.Idl;
import ws.epigraph.idl.ResourceIdl;
import ws.epigraph.idl.operations.CustomOperationIdl;
import ws.epigraph.idl.operations.OperationIdl;
import ws.epigraph.projections.StepsAndProjection;
import ws.epigraph.projections.req.input.ReqInputFieldProjection;
import ws.epigraph.projections.req.output.ReqOutputFieldProjection;
import ws.epigraph.psi.EpigraphPsiUtil;
import ws.epigraph.psi.PsiProcessingError;
import ws.epigraph.psi.PsiProcessingException;
import ws.epigraph.refs.SimpleTypesResolver;
import ws.epigraph.refs.TypesResolver;
import ws.epigraph.tests.*;
import ws.epigraph.types.DataType;
import ws.epigraph.url.CustomRequestUrl;
import ws.epigraph.url.parser.psi.UrlCustomUrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ws.epigraph.test.TestUtil.*;
import static ws.epigraph.url.parser.RequestUrlPsiParserTestUtil.parseIdl;
import static ws.epigraph.url.parser.RequestUrlPsiParserTestUtil.printParameters;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class CustomRequestUrlPsiParserTest {
  private TypesResolver resolver = new SimpleTypesResolver(
      PersonId.type,
      Person.type,
      User.type,
      UserId.type,
      UserRecord.type,
      String_Person_Map.type,
      epigraph.String.type,
      epigraph.Boolean.type
  );

  private String idlText = lines(
      "namespace test",
      "import ws.epigraph.tests.Person",
      "import ws.epigraph.tests.UserRecord",
      "resource users : map[String,Person] {",
      "  myCustomOp CUSTOM {",
      "    method POST",
      "    inputType UserRecord",
      "    inputProjection (id, firstName)",
      "    outputProjection [required]( :record (id, firstName) )",
      "  }",
      "}"
  );

  private CustomOperationIdl customIdl1;
  private DataType resourceType = String_Person_Map.type.dataType();

  {
    try {
      Idl idl = parseIdl(idlText, resolver);
      ResourceIdl resourceIdl = idl.resources().get("users");

      final @NotNull List<OperationIdl> operationIdls = resourceIdl.operations();
      customIdl1 = (CustomOperationIdl) operationIdls.get(0);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testParsing1() throws IOException, PsiProcessingException {
    test(
        customIdl1,
        "/users<(id)>/123:record(id)?format='json'&verbose=true",
        "users",
        3,
        "( id )",
        "+users / \"123\" :record ( id )",
        "{format = \"json\", verbose = true}"
    );
  }

  private void test(
      CustomOperationIdl op,
      String url,
      String expectedResource,
      int expectedSteps,
      String expectedInputProjection,
      String expectedOutputProjection,
      String expectedParams)
      throws IOException, PsiProcessingException {

    List<PsiProcessingError> errors = new ArrayList<>();
    final @NotNull CustomRequestUrl requestUrl = CustomRequestUrlPsiParser.parseCustomRequestUrl(
        resourceType,
        op,
        parseUrlPsi(url),
        resolver,
        errors
    );

    failIfHasErrors(errors);

    assertEquals(expectedResource, requestUrl.fieldName());

    final @Nullable ReqInputFieldProjection inputProjection = requestUrl.inputProjection();
    if (inputProjection == null) assertNull(expectedInputProjection);
    else assertEquals(expectedInputProjection, printReqInputVarProjection(inputProjection.varProjection()));

    final @NotNull StepsAndProjection<ReqOutputFieldProjection> stepsAndProjection = requestUrl.outputProjection();
    assertEquals(expectedSteps, stepsAndProjection.pathSteps());
    assertEquals(
        expectedOutputProjection,
        printReqOutputFieldProjection(expectedResource, stepsAndProjection.projection(), expectedSteps)
    );

    assertEquals(expectedParams, printParameters(requestUrl.parameters()));
  }


  private static UrlCustomUrl parseUrlPsi(@NotNull String text) throws IOException {
    EpigraphPsiUtil.ErrorsAccumulator errorsAccumulator = new EpigraphPsiUtil.ErrorsAccumulator();

    @NotNull UrlCustomUrl urlPsi =
        EpigraphPsiUtil.parseText(text, UrlSubParserDefinitions.CUSTOM_URL, errorsAccumulator);

    failIfHasErrors(urlPsi, errorsAccumulator);

    return urlPsi;
  }

}