package io.epigraph.projections.op.output;

import com.intellij.psi.PsiErrorElement;
import io.epigraph.idl.parser.projections.IdlSubParserDefinitions;
import io.epigraph.idl.parser.psi.IdlOpOutputVarProjection;
import io.epigraph.psi.PsiProcessingException;
import io.epigraph.projections.op.input.OpInputPrimitiveModelProjection;
import io.epigraph.psi.EpigraphPsiUtil;
import io.epigraph.tests.*;
import io.epigraph.types.DataType;
import io.epigraph.types.SimpleTypesResolver;
import io.epigraph.types.TypesResolver;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class OpOutputProjectionsTest {
  @Test
  public void testRecursiveFieldProjection() {
    OpOutputRecordModelProjection recursivePersonRecordProjection = new OpOutputRecordModelProjection(
        PersonRecord.type,
        false,
        null,
        OpOutputRecordModelProjection.fields(
            new OpOutputFieldProjection(
                PersonRecord.id,
                null,
                new OpOutputVarProjection(
                    Person.type,
                    new OpOutputTagProjection(
                        Person.id,
                        new OpOutputPrimitiveModelProjection(PersonId.type, false, null)
                    )
                ), true
            )
        )
    );

    OpOutputFieldProjection recursiveBestFriendProjection = new OpOutputFieldProjection(
        PersonRecord.bestFriend,
        null,
        new OpOutputVarProjection(
            Person.type,
            new OpOutputTagProjection(
                Person.record,
                recursivePersonRecordProjection
            )
        ), true
    );
    recursivePersonRecordProjection.addFieldProjection(
        recursiveBestFriendProjection
    );

    OpOutputRecordModelProjection recursivePersonRecordProjection2 = new OpOutputRecordModelProjection(
        PersonRecord.type,
        false,
        null,
        OpOutputRecordModelProjection.fields(
            new OpOutputFieldProjection(
                PersonRecord.id,
                null,
                new OpOutputVarProjection(
                    Person.type,
                    new OpOutputTagProjection(
                        Person.id,
                        new OpOutputPrimitiveModelProjection(PersonId.type, false, null)
                    )
                ), true
            )
        )
    );
    OpOutputFieldProjection recursiveBestFriendProjection2 = new OpOutputFieldProjection(
        PersonRecord.bestFriend,
        null,
        new OpOutputVarProjection(
            Person.type,
            new OpOutputTagProjection(
                Person.record,
                recursivePersonRecordProjection2
            )
        ), true
    );
    recursivePersonRecordProjection2.addFieldProjection(
        recursiveBestFriendProjection2
    );

    assertEquals(recursiveBestFriendProjection.hashCode(), recursiveBestFriendProjection2.hashCode());
    assertEquals(recursiveBestFriendProjection, recursiveBestFriendProjection2);
  }

  @Test
  public void testRecursiveRecordProjection() {
    OpOutputRecordModelProjection recursivePersonRecordProjection = new OpOutputRecordModelProjection(
        PersonRecord.type,
        false,
        null,
        OpOutputRecordModelProjection.fields(
            new OpOutputFieldProjection(
                PersonRecord.id,
                null,
                new OpOutputVarProjection(
                    Person.type,
                    new OpOutputTagProjection(
                        Person.id,
                        new OpOutputPrimitiveModelProjection(PersonId.type, false, null)
                    )
                ), true
            )
        )
    );
    recursivePersonRecordProjection.addFieldProjection(
        new OpOutputFieldProjection(
            PersonRecord.bestFriend,
            null,
            new OpOutputVarProjection(
                Person.type,
                new OpOutputTagProjection(
                    Person.record,
                    recursivePersonRecordProjection
                )
            ), true
        )
    );

    OpOutputRecordModelProjection recursivePersonRecordProjection2 = new OpOutputRecordModelProjection(
        PersonRecord.type,
        false,
        null,
        OpOutputRecordModelProjection.fields(
            new OpOutputFieldProjection(
                PersonRecord.id,
                null,
                new OpOutputVarProjection(
                    Person.type,
                    new OpOutputTagProjection(
                        Person.id,
                        new OpOutputPrimitiveModelProjection(PersonId.type, false, null)
                    )
                ), true
            )
        )
    );
    recursivePersonRecordProjection2.addFieldProjection(
        new OpOutputFieldProjection(
            PersonRecord.bestFriend,
            null,
            new OpOutputVarProjection(
                Person.type,
                new OpOutputTagProjection(
                    Person.record,
                    recursivePersonRecordProjection2
                )
            ), true
        )
    );

    assertEquals(recursivePersonRecordProjection.hashCode(), recursivePersonRecordProjection2.hashCode());
    assertEquals(recursivePersonRecordProjection, recursivePersonRecordProjection2);
  }

  @Test
  public void testParsing() throws PsiProcessingException {
    TypesResolver resolver = new SimpleTypesResolver(
        PersonId.type,
        Person.type,
        User.type,
        UserId.type,
        UserRecord.type,
        epigraph.String.type
    );

    String projectionStr = lines(
        ":(",
        "  +id,",
        "  record (",
        "    +id {",
        "      parameters: {",
//        "        +param1 : epigraph.String { default : \"hello world\" },", // todo enable once there's `toString` on data
        "      }",
        "    },",
        "    +bestFriend :record (",
        "      +id,",
        "      bestFriend: id",
        // todo get default tag from Person.type, once available
        "    ),",
        "    friends {} *( :+id {}) {}",
        "  )",
        ") ~io.epigraph.tests.User :record (profile)"
    );


    EpigraphPsiUtil.ErrorsAccumulator errorsAccumulator = new EpigraphPsiUtil.ErrorsAccumulator();

    IdlOpOutputVarProjection psiVarProjection = EpigraphPsiUtil.parseText(
        projectionStr,
        IdlSubParserDefinitions.OP_OUTPUT_VAR_PROJECTION.rootElementType(),
        IdlOpOutputVarProjection.class,
        IdlSubParserDefinitions.OP_OUTPUT_VAR_PROJECTION,
        errorsAccumulator
    );

    if (errorsAccumulator.hasErrors()) {
      for (PsiErrorElement element : errorsAccumulator.errors()) {
        System.err.println(element.getErrorDescription() + " at " +
                           EpigraphPsiUtil.getLocation(element, projectionStr));
      }
      fail();
    }

//    String psiDump = DebugUtil.psiToString(psiVarProjection, true, false).trim();
//    System.out.println(psiDump);

    OpOutputVarProjection varProjection = null;
    try {
      varProjection = OpOutputProjectionsPsiParser.parseVarProjection(
          new DataType(false, Person.type, Person.id),
          psiVarProjection,
          resolver
      );

    } catch (PsiProcessingException e) {
      e.printStackTrace();
      System.err.println(e.getMessage() + " at " +
                         EpigraphPsiUtil.getLocation(e.psi(), projectionStr));
      fail();
    }

    String expected = lines(
        "var io.epigraph.tests.Person (",
        "  id: +io.epigraph.tests.PersonId",
        "  record:",
        "    io.epigraph.tests.PersonRecord {",
        "      fields: {",
        "        +id:",
        "          var io.epigraph.tests.PersonId (",
        "            self: io.epigraph.tests.PersonId",
        "          )",
        "        +bestFriend:",
        "          var io.epigraph.tests.Person (",
        "            record:",
        "              io.epigraph.tests.PersonRecord {",
        "                fields: {",
        "                  +id:",
        "                    var io.epigraph.tests.PersonId (",
        "                      self: io.epigraph.tests.PersonId",
        "                    )",
        "                  bestFriend:",
        "                    var io.epigraph.tests.Person (",
        "                      id: io.epigraph.tests.PersonId",
        "                    )",
        "                }",
        "              }",
        "          )",
        "        friends:",
        "          var list[polymorphic io.epigraph.tests.Person] (",
        "            self:",
        "              list[polymorphic io.epigraph.tests.Person] {",
        "                items:",
        "                  var io.epigraph.tests.Person (",
        "                    id: +io.epigraph.tests.PersonId",
        "                  )",
        "              }",
        "          )",
        "      }",
        "    }",
        ")",
        "~(",
        "  var io.epigraph.tests.User (",
        "    record:",
        "      io.epigraph.tests.UserRecord {",
        "        fields: {",
        "          profile:",
        "            var io.epigraph.tests.Url (",
        "              self: io.epigraph.tests.Url",
        "            )",
        "        }",
        "      }",
        "  )",
        ")"
    );

    assertEquals(expected, varProjection.toString());
  }

  @Test
  public void testBuildSampleProjection() {
    OpOutputRecordModelProjection recursivePersonRecordProjection = new OpOutputRecordModelProjection(
        PersonRecord.type,
        false,
        null,
        OpOutputRecordModelProjection.fields(
            new OpOutputFieldProjection(
                PersonRecord.id,
                null,
                new OpOutputVarProjection(
                    Person.type,
                    new OpOutputTagProjection(
                        Person.id,
                        new OpOutputPrimitiveModelProjection(PersonId.type, false, null)
                    )
                ), true
            )
        )
    );
    recursivePersonRecordProjection.addFieldProjection(
        new OpOutputFieldProjection(
            PersonRecord.bestFriend,
            null,
            new OpOutputVarProjection(
                Person.type,
                new OpOutputTagProjection(
                    Person.record,
                    recursivePersonRecordProjection
                )
            ), true
        )
    );

    OpOutputVarProjection personVarProjection = new OpOutputVarProjection(
        Person.type,
        new LinkedHashSet<>(
            Arrays.asList(
                new OpOutputTagProjection(
                    Person.id,
                    new OpOutputPrimitiveModelProjection(
                        PersonId.type,
                        true,
                        OpParam.params(
                            // todo string
                            new OpParam("token", new OpInputPrimitiveModelProjection(UserId.type, false, null))
                        )
                    )
                ),
                new OpOutputTagProjection(
                    Person.record,
                    new OpOutputRecordModelProjection(
                        PersonRecord.type,
                        false,
                        null,
                        OpOutputRecordModelProjection.fields(
                            new OpOutputFieldProjection(
                                PersonRecord.bestFriend,
                                null,
                                // todo make it recursive?
                                new OpOutputVarProjection(
                                    Person.type,
                                    new OpOutputTagProjection(
                                        Person.id,
                                        new OpOutputPrimitiveModelProjection(PersonId.type, false, null)
                                    )
                                ),
                                true
                            )
                        )
                    )
                )
            )
        ),
        new LinkedHashSet<>(
            Collections.singletonList(
                new OpOutputVarProjection(
                    UserRecord.type,
                    new OpOutputTagProjection(
                        User.record,
                        new OpOutputRecordModelProjection(
                            UserRecord.type,
                            false, null,
                            OpOutputRecordModelProjection.fields(
                                new OpOutputFieldProjection(
                                    UserRecord.bestFriend,
                                    OpParam.params(new OpParam("maxAge",
                                                               new OpInputPrimitiveModelProjection(
                                                                   PersonId.type,
                                                                   false,
                                                                   null
                                                               )
                                    )),
                                    new OpOutputVarProjection(
                                        Person.type, // todo ??
                                        new OpOutputTagProjection(
                                            Person.record, // todo ??
                                            recursivePersonRecordProjection
                                        )
                                    ),
                                    true
                                )
                            )
                        )
                    )
                )
            )
        )
    );

//    System.out.println(personVarProjection);
  }

  private static String lines(String... lines) {
    return Arrays.stream(lines).collect(Collectors.joining("\n"));
  }
}