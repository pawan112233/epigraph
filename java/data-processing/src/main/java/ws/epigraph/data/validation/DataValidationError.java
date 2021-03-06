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

package ws.epigraph.data.validation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class DataValidationError {
  private final @NotNull List<DataValidationContext.@NotNull StackItem> location;
  private final @NotNull String message;
  private final boolean isImplementationError;

  DataValidationError(
      final @NotNull Collection<DataValidationContext.StackItem> location,
      final @NotNull String message) {

    this(location, message, false);
  }

  DataValidationError(
      final @NotNull Collection<DataValidationContext.StackItem> location,
      final @NotNull String message,
      final boolean isImplementationError) {

    final ArrayList<DataValidationContext.StackItem> locationTmp = new ArrayList<>(location);
    Collections.reverse(locationTmp);
    this.location = Collections.unmodifiableList(locationTmp);
    this.message = message;
    this.isImplementationError = isImplementationError;
  }

  public @NotNull String message() { return message; }

  public @NotNull List<DataValidationContext.StackItem> location() { return location; }

  public boolean isImplementationError() { return isImplementationError; }

  @Override
  public @NotNull String toString() {
    String location = location().stream().map(DataValidationContext.StackItem::toString).collect(Collectors.joining());
    return location.trim().isEmpty() ?
           message() :
           location + " : " +
           message();
  }
}
