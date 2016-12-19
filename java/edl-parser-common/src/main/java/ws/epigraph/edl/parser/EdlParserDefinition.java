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

package ws.epigraph.edl.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import ws.epigraph.edl.lexer.EdlElementTypes;
import ws.epigraph.edl.lexer.EdlFlexAdapter;
import ws.epigraph.edl.parser.psi.EdlFile;
import ws.epigraph.edl.parser.psi.stubs.EdlStubElementTypes;
import org.jetbrains.annotations.NotNull;

import static ws.epigraph.edl.lexer.EdlElementTypes.*;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class EdlParserDefinition implements ParserDefinition {
  public static final EdlParserDefinition INSTANCE = new EdlParserDefinition();

  public static final TokenSet WHITESPACES = TokenSet.create(TokenType.WHITE_SPACE);
  public static final TokenSet IDENTIFIERS = TokenSet.create(S_ID);
  public static final TokenSet COMMENTS = TokenSet.create(S_COMMENT, S_BLOCK_COMMENT);
  public static final TokenSet CURLY_BRACES = TokenSet.create(S_CURLY_LEFT, S_CURLY_RIGHT);
  public static final TokenSet KEYWORDS = TokenSet.create(
      S_NAMESPACE,
      S_IMPORT,
      S_MAP,
      S_DEFAULT,
      S_NODEFAULT,
      S_LIST,
      S_RECORD,
      S_EXTENDS,
      S_VARTYPE,
      S_ENUM,
      S_META,
      S_SUPPLEMENT,
      S_SUPPLEMENTS,
      S_WITH,
      S_ABSTRACT,
      S_OVERRIDE,
      S_INTEGER_T,
      S_LONG_T,
      S_DOUBLE_T,
      S_BOOLEAN_T,
      S_STRING_T,
      S_NULL, // or is it a LITERAL?
      S_FORBIDDEN,
      S_REQUIRED,
      S_RESOURCE,
      S_GET,
      S_POST,
      S_PUT,
      S_DELETE,
      S_OP_READ,
      S_OP_CREATE,
      S_OP_UPDATE,
      S_OP_DELETE,
      S_OP_CUSTOM,
      S_METHOD,
      S_PATH,
      S_INPUT_TYPE,
      S_INPUT_PROJECTION,
      S_OUTPUT_TYPE,
      S_OUTPUT_PROJECTION,
      S_DELETE_PROJECTION
  );
  public static final TokenSet STRING_LITERALS = TokenSet.create(S_STRING);
  public static final TokenSet LITERALS = TokenSet.andSet(STRING_LITERALS, TokenSet.create(S_NUMBER, S_BOOLEAN));
  public static final TokenSet TYPE_KINDS = TokenSet.create(S_VARTYPE, S_RECORD, S_MAP, S_LIST, S_ENUM,
      S_STRING_T, S_INTEGER_T, S_LONG_T, S_DOUBLE_T, S_BOOLEAN_T);

  @NotNull
  @Override
  public Lexer createLexer(Project project) {
    return EdlFlexAdapter.newInstance();
  }

  @Override
  public PsiParser createParser(Project project) {
    return new EdlParser();
  }

  @Override
  public IFileElementType getFileNodeType() {
    return EdlStubElementTypes.EDL_FILE;
  }

  @NotNull
  @Override
  public TokenSet getWhitespaceTokens() {
    return WHITESPACES;
  }

  @NotNull
  @Override
  public TokenSet getCommentTokens() {
    return COMMENTS;
  }

  @NotNull
  @Override
  public TokenSet getStringLiteralElements() {
    return STRING_LITERALS;
  }

  @NotNull
  @Override
  public PsiElement createElement(ASTNode node) {
    return EdlElementTypes.Factory.createElement(node);
  }

  @Override
  public PsiFile createFile(FileViewProvider viewProvider) {
    return new EdlFile(viewProvider);
  }

  @Override
  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY; // TODO refine
  }

  //

  public static boolean isKeyword(@NotNull String name) {
    EdlFlexAdapter lexer = EdlFlexAdapter.newInstance();
    lexer.start(name);
    return EdlParserDefinition.KEYWORDS.contains(lexer.getTokenType()) && lexer.getTokenEnd() == name.length();
  }

  public static boolean isIdentifier(@NotNull String name) {
    EdlFlexAdapter lexer = EdlFlexAdapter.newInstance();
    lexer.start(name);
    return EdlParserDefinition.IDENTIFIERS.contains(lexer.getTokenType()) && lexer.getTokenEnd() == name.length();
  }
}
