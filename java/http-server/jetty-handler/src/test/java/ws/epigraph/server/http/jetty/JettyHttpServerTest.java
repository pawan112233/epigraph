/*
 * Copyright 2018 Sumo Logic
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

package ws.epigraph.server.http.jetty;

import net.jcip.annotations.NotThreadSafe;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import ws.epigraph.server.http.AbstractHttpServerTest;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
@SuppressWarnings("ProhibitedExceptionDeclared")
@NotThreadSafe
public class JettyHttpServerTest extends AbstractHttpServerTest {
  private static Server jettyServer;

  private static final int port = UNIQUE_PORT.incrementAndGet();

  @Override
  protected int port() { return port; }

  public static void main(String[] args) throws Exception {
    start();
    jettyServer.join();
  }

  @BeforeClass
  public static void start() throws Exception {
    jettyServer = new Server(port);
    EpigraphJettyHandler handler = new EpigraphJettyHandler(buildUsersService(), -1);
    jettyServer.setHandler(handler);

    jettyServer.start();
  }

  @AfterClass
  public static void stop() throws Exception {
    jettyServer.stop();
  }
}
