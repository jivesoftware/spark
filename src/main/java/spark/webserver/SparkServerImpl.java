/*
 * Copyright 2011- Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark.webserver;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

/**
 * Spark server implementation
 * 
 * @author Per Wendel
 */
class SparkServerImpl implements SparkServer {

    private static final String NAME = "Spark";
    private Handler handler;
    private Server server;

    public SparkServerImpl(Handler handler) {
        this.handler = handler;
        System.setProperty("org.mortbay.log.class", "spark.JettyLogger");
    }

    @Override
    public void ignite(int port, String keystoreFile,
                       String keystorePassword) {

        server = new Server(port);
        server.setHandler(handler);

        try {
            System.out.println("== " + NAME + " has ignited ..."); // NOSONAR
            System.out.println(">> Listening on port :" + port); // NOSONAR

            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace(); // NOSONAR
            System.exit(100); // NOSONAR
        }
    }

    @Override
    public void stop() {
        System.out.print(">>> " + NAME + " shutting down..."); // NOSONAR
        try {
            if (server != null) {
                server.stop();
            }
        } catch (Exception e) {
            e.printStackTrace(); // NOSONAR
            System.exit(100); // NOSONAR
        }
        System.out.println("done"); // NOSONAR
    }
}
