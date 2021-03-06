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

package ws.epigraph.server.http.servlet;

import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public final class SyncServletExchange extends ServletExchange {
  private final @NotNull HttpServletRequest request;
  private final @NotNull HttpServletResponse response;

  public SyncServletExchange(
      final @NotNull HttpServletRequest request,
      final @NotNull HttpServletResponse response) {
    this.request = request;
    this.response = response;
  }

  @Override
  protected @NotNull HttpServletRequest request() { return request;}

  @Override
  protected @NotNull HttpServletResponse response() { return response;}
}
