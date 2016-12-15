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

package ws.epigraph.edl.parser.psi.stubs;

import com.intellij.psi.stubs.*;
import ws.epigraph.edl.parser.EdlLanguage;
import ws.epigraph.edl.parser.psi.EdlSupplementDef;
import ws.epigraph.edl.parser.psi.impl.EdlSupplementDefImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class EdlSupplementDefStubElementType extends IStubElementType<EdlSupplementDefStub, EdlSupplementDef> {
  public EdlSupplementDefStubElementType(@NotNull @NonNls String debugName) {
    super(debugName, EdlLanguage.INSTANCE);
  }

  @Override
  public EdlSupplementDef createPsi(@NotNull EdlSupplementDefStub stub) {
    return new EdlSupplementDefImpl(stub, this);
  }

  @Override
  public EdlSupplementDefStub createStub(@NotNull EdlSupplementDef supplementDef, StubElement parentStub) {
    return new EdlSupplementDefStubImpl(parentStub);
  }

  @NotNull
  @Override
  public String getExternalId() {
    return "epigraph_edl.supplement";
  }

  @Override
  public void serialize(@NotNull EdlSupplementDefStub stub, @NotNull StubOutputStream dataStream) throws IOException {
  }

  @NotNull
  @Override
  public EdlSupplementDefStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void indexStub(@NotNull EdlSupplementDefStub stub, @NotNull IndexSink sink) {
  }
}
