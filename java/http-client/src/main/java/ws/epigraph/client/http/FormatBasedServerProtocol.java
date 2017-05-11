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

package ws.epigraph.client.http;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ws.epigraph.data.Data;
import ws.epigraph.errors.ErrorValue;
import ws.epigraph.http.ContentType;
import ws.epigraph.http.MimeTypes;
import ws.epigraph.invocation.OperationInvocationContext;
import ws.epigraph.invocation.OperationInvocationResult;
import ws.epigraph.projections.req.output.ReqOutputTagProjectionEntry;
import ws.epigraph.projections.req.output.ReqOutputVarProjection;
import ws.epigraph.service.operations.ReadOperationResponse;
import ws.epigraph.types.Type;
import ws.epigraph.util.HttpStatusCode;
import ws.epigraph.util.IOUtil;
import ws.epigraph.wire.FormatException;
import ws.epigraph.wire.FormatFactories;
import ws.epigraph.wire.FormatReader;
import ws.epigraph.wire.ReqOutputFormatReader;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class FormatBasedServerProtocol implements ServerProtocol {
  private static final Logger LOG = LoggerFactory.getLogger(FormatBasedServerProtocol.class);

  private final @NotNull FormatReader.Factory<? extends ReqOutputFormatReader> reqOutputReaderFactory;
  private final @NotNull Charset requestCharset;

  public FormatBasedServerProtocol(
      final @NotNull FormatFactories formatFactories,
      final @NotNull Charset requestCharset) {
    reqOutputReaderFactory = formatFactories.reqOutputReaderFactory();
    this.requestCharset = requestCharset;
  }

  @Override
  public @NotNull String mimeType() {
    // here we assume that all formatFactories use the same format, i.e. we don't mix json/xml
    return reqOutputReaderFactory.format().mimeType();
  }

  @Override
  public @Nullable OperationInvocationResult<ReadOperationResponse<?>> readResponse(
      final @NotNull ReqOutputVarProjection projection,
      final @NotNull OperationInvocationContext operationInvocationContext,
      final @NotNull HttpResponse httpResponse) {

    int statusCode = httpResponse.getStatusLine().getStatusCode();
    try {
      if (statusCode == HttpStatusCode.OK) {
        try (InputStream inputStream = httpResponse.getEntity().getContent()) {
          ReqOutputFormatReader formatReader = reqOutputReaderFactory.newFormatReader(inputStream);
          Data data = formatReader.readData(projection);
          return OperationInvocationResult.success(new ReadOperationResponse<>(data));
        } catch (FormatException e) {
          return OperationInvocationResult.failure(new MalformedOutputInvocationError(e));
        }
      } else {
        @NotNull Charset responseCharset = Util.defaultCharset;

        Header contentTypeHeader = httpResponse.getEntity().getContentType();
        if (contentTypeHeader == null) // broken server?
          return readPlainTextError(httpResponse, statusCode, responseCharset);

        ContentType contentType = Util.parseContentType(contentTypeHeader.getValue());
        responseCharset = Optional.ofNullable(contentType.charset()).orElse(responseCharset);

        if (contentType.mimeType().equals(MimeTypes.JSON)) {
          ErrorValue error = readError(httpResponse, responseCharset);
          Data data = createErrorData(projection, error);
          return OperationInvocationResult.success(new ReadOperationResponse<>(data));
        } else
          return readPlainTextError(httpResponse, statusCode, responseCharset);

      }
    } catch (IOException e) {
      if (operationInvocationContext.isDebug())
        LOG.error("Error reading operation response", e);

      return OperationInvocationResult.failure(new IOExceptionInvocationError(e));
    }
  }

  private @NotNull OperationInvocationResult<ReadOperationResponse<?>> readPlainTextError(
      @NotNull HttpResponse httpResponse,
      int statusCode, @NotNull Charset responseCharset) throws IOException {

    try (InputStream is = httpResponse.getEntity().getContent()) {
      return OperationInvocationResult.failure(
          new ServerSideInvocationError(statusCode, IOUtil.readInputStream(is, responseCharset))
      );
    }
  }

  private @NotNull ErrorValue readError(@NotNull HttpResponse response, @NotNull Charset charset) throws IOException {
    // read response text fully first as we might need to use it twice

    StringBuilder textBuilder = new StringBuilder();

    try (
        InputStream inputStream = response.getEntity().getContent();
        Reader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName(charset.name())))
    ) {
      for (int c = reader.read(); c != -1; c = reader.read()) { textBuilder.append((char) c); }
    }

    String string = textBuilder.toString();
    try {
      ReqOutputFormatReader formatReader = reqOutputReaderFactory.newFormatReader(
          new ByteArrayInputStream(string.getBytes(charset))
      );
      return formatReader.readError();
    } catch (FormatException ignored) { // log it? not all messages are guaranteed to be in proper format
      return new ErrorValue(response.getStatusLine().getStatusCode(), string);
    }
  }

  private @NotNull Data createErrorData(@NotNull ReqOutputVarProjection projection, @NotNull ErrorValue errorValue) {
    // create data instance with all requested tags set to error

    Type type = (Type) projection.type();
    Data.Builder builder = type.createDataBuilder();

    for (final ReqOutputTagProjectionEntry tpe : projection.tagProjections().values()) {
      builder._raw().setError((Type.Tag) tpe.tag(), errorValue);
    }

    return builder;
  }
}