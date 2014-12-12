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
package spark;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import spark.route.HttpMethod;
import spark.route.RouteMatcher;
import spark.route.SimpleRouteMatcher;
import spark.webserver.SparkServerFactory;

import java.util.Arrays;

/**
 * The main building block of a Spark application is a set of routes. A route is
 * made up of three simple pieces:
 * <p/>
 * <ul>
 * <li>A verb (get, post, put, delete, head, trace, connect, options)</li>
 * <li>A path (/hello, /users/:name)</li>
 * <li>A callback ( handle(Request request, Response response) )</li>
 * </ul>
 * <p/>
 * Example:
 * <p/>
 * <pre>
 * {@code
 * Spark.get(new Route("/hello") {
 *    public Object handle(Request request, Response response) {
 *       return "Hello World!";
 *    }
 * });
 * </pre>
 * <p/>
 * <code>
 * <p/>
 * </code>
 *
 * @author Per Wendel
 */
public final class Spark {

    private boolean initialized = false;

    private Server server;
    private RouteMatcher routeMatcher;
    private final int port;

    // Hide constructor
    public Spark(int port) {
        this.port = port;
    }

    /**
     * Set the connection to be secure, using the specified keystore and
     * truststore. This has to be called before any route mapping is done. You
     * have to supply a keystore file, truststore file is optional (keystore
     * will be reused).
     * <p/>
     * This method is only relevant when using embedded Jetty servers. It should
     * not be used if you are using Servlets, where you will need to secure the
     * connection in the servlet container
     *
     * @param keystoreFile       The keystore file location as string
     * @param keystorePassword   the password for the keystore
     * @param truststoreFile     the truststore file location as string, leave null to reuse
     *                           keystore
     * @param truststorePassword the trust store password
     */
    public synchronized void setSecure(String keystoreFile,
                                              String keystorePassword, String truststoreFile,
                                              String truststorePassword) {
        if (initialized) {
            throwBeforeRouteMappingException();
        }

        if (keystoreFile == null) {
            throw new IllegalArgumentException(
                    "Must provide a keystore file to run secured");
        }

    }

    /**
     * Maps a filter to be executed before any matching routes
     *
     * @param filter The filter
     */
    public synchronized void before(Filter filter) {
        addFilter(HttpMethod.before.name(), filter);
    }

    /**
     * Maps a filter to be executed after any matching routes
     *
     * @param filter The filter
     */
    public synchronized void after(Filter filter) {
        addFilter(HttpMethod.after.name(), filter);
    }

    synchronized void runFromServlet() {
        if (!initialized) {
            routeMatcher = new SimpleRouteMatcher(); // RouteMatcherFactory.get();
            initialized = true;
        }
    }

    // WARNING, used for jUnit testing only!!! (not anymore!!!!)
    public synchronized void clearRoutes() {
        routeMatcher.clearRoutes();
    }

    // Used for jUnit testing!
    public synchronized void stop() {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                e.printStackTrace(); // TODO sort that out
            }
        }
        initialized = false;
    }

    public synchronized void addRoute(String httpMethod, Route route) {
        init();
        routeMatcher.parseValidateAddRoute(httpMethod + " '" + route.getPath()
                + "'", route.getAcceptType(), route);
    }

    private void addFilter(String httpMethod, Filter filter) {
        init();
        routeMatcher.parseValidateAddRoute(httpMethod + " '" + filter.getPath()
                + "'", filter.getAcceptType(), filter);
    }
    
    private synchronized void init() {
        if (!initialized) {
            routeMatcher = new SimpleRouteMatcher(); // RouteMatcherFactory.get();
            server = SparkServerFactory.create(port, routeMatcher);
            initialized = true;
        }
    }

    private void throwBeforeRouteMappingException() {
        throw new IllegalStateException(
                "This must be done before route mapping has begun");
    }

    public int getPort() {
        init();
        Connector[] connectors = server.getConnectors();
        if (connectors.length != 1) {
            throw new IllegalStateException("Can't pick a connector: " + Arrays.toString(connectors));
        }

        Connector connector = connectors[0];
        return connector.getLocalPort();
    }
    
    /*
     * TODO: discover new TODOs.
     * 
     * 
     * TODO: Make available as maven dependency, upload on repo etc... 
     * TODO: Add *, splat possibility 
     * TODO: Add validation of routes, invalid characters and stuff, also validate parameters, check static, ONGOING
     * 
     * TODO: Javadoc
     * 
     * TODO: Create maven archetype, "ONGOING" 
     * TODO: Add cache-control helpers
     * 
     * advanced TODO list: 
     * TODO: Add regexp URIs
     * 
     * Ongoing
     * 
     * Done 
     * TODO: Routes are matched in the order they are defined. The rirst route that matches the request is invoked. ??? 
     * TODO: Before method for filters...check sinatra page 
     * TODO: Setting Headers 
     * TODO: Do we want get-prefixes for all *getters* or do we want a more ruby like approach???
     * (Maybe have two choices?) 
     * TODO: Setting Body, Status Code 
     * TODO: Add possibility to set content type on return, DONE 
     * TODO: Add possibility to access HttpServletContext in method impl, DONE 
     * TODO: Redirect func in web context, DONE 
     * TODO: Refactor, extract interfaces, DONE 
     * TODO: Figure out a nice name, DONE - SPARK 
     * TODO: Add /uri/{param} possibility, DONE 
     * TODO: Tweak log4j config, DONE 
     * TODO: Query string in web context, DONE 
     * TODO: Add URI-param fetching from webcontext ie. ?param=value&param2=...etc, AND headers, DONE
     * TODO: sessions? (use session servlet context?) DONE
     */
}
