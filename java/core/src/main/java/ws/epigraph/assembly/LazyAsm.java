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

package ws.epigraph.assembly;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class LazyAsm<D, P, R> implements Asm<D, P, R> {
  private final Supplier<Asm<D, P, R>> asmSupplier;

  public LazyAsm(final @NotNull Supplier<Asm<D, P, R>> supplier) {asmSupplier = supplier;}

  @Override
  public @NotNull R assemble(
      final @NotNull D dto, final @NotNull P projection, final @NotNull AsmContext ctx) {

    return asmSupplier.get().assemble(dto, projection, ctx);
  }
}
