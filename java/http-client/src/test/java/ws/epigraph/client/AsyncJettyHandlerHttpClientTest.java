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

package ws.epigraph.client;

import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOReactorException;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import ws.epigraph.client.http.AsyncHttpRequestDispatcher;
import ws.epigraph.server.http.jetty.EpigraphJettyHandler;

import java.io.IOException;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class AsyncJettyHandlerHttpClientTest extends AbstractHttpClientTest {
  private static Server jettyServer;

  @BeforeClass
  public static void start() throws Exception {
    jettyServer = new Server(PORT);
    EpigraphJettyHandler handler = new EpigraphJettyHandler(buildUsersService(), -1);
    jettyServer.setHandler(handler);

    jettyServer.start();
  }

  @AfterClass
  public static void stop() throws Exception {
    jettyServer.stop();
  }

  @BeforeClass
  public static void startDispatcher() throws IOReactorException {
    dispatcher = new AsyncHttpRequestDispatcher(
        ConnectionConfig.DEFAULT,
        IOReactorConfig.DEFAULT,
        2,
        TIMEOUT
    );
  }

  @AfterClass
  public static void stopDispatcher() throws IOException {
    dispatcher.shutdown();
  }
}
