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

import org.eclipse.jetty.server.Server;
import spark.route.RouteMatcher;

/**
 * 
 *
 * @author Per Wendel
 */
public final class SparkServerFactory {

    private SparkServerFactory() {}
    
    public static Server create(int port, RouteMatcher routeMatcher) {
        MatcherFilter matcherFilter = new MatcherFilter(routeMatcher, false, false);
        matcherFilter.init(null);
        JettyHandler handler = new JettyHandler(matcherFilter);

        Server server = new Server(port);
        server.setHandler(handler);

        try {
            server.start();
            //server.join();
        } catch (Exception e) {
            return null; // TODO sort this out
        }

        return server;
    }
    
}
