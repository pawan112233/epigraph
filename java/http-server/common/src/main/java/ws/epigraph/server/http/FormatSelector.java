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

package ws.epigraph.server.http;

import org.jetbrains.annotations.NotNull;
import ws.epigraph.wire.FormatException;
import ws.epigraph.wire.FormatFactories;

/**
 * Selects format factories to be used based on given invocation context
 *
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public interface FormatSelector<C extends HttpInvocationContext> {
  /**
   * Gets format factories based on passed context
   *
   * @param context invocation context
   *
   * @return format factories
   * @throws FormatException in case if format can't be determined based on context
   */
  @NotNull FormatFactories getFactories(@NotNull C context) throws FormatException;
}
