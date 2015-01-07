package spark;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import spark.route.HttpMethod;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

public class SparkJive {

    private final Spark spark;
    private boolean scriptComplete;

    public SparkJive() {
        this(0);
    }

    public SparkJive(int port) {
        spark = new Spark(port); // Get any available port (check for it with getURI() below)
    }

    /**
     * Specify your test scenario. Only one endpoint will be defined at a time (so you can repeat paths and methods with
     * different return values).
     * @param script
     */
    public void setScript(@Nonnull List<ScriptEntry> script) {
        addScriptEndpoint(Preconditions.checkNotNull(script.iterator()));
    }

    private void addScriptEndpoint(final Iterator<ScriptEntry> iterator) {
        if (iterator.hasNext()) {
            this.scriptComplete = false;
            final ScriptEntry entry = iterator.next();
            spark.addRoute(entry.method.name(), new Route(entry.path) {
                @Override
                public Object handle(Request request, Response response) {
                    spark.clearRoutes();
                    addScriptEndpoint(iterator);
                    if (entry.handler.isPresent()) {
                        return entry.handler.get().handle(request, response);
                    } else {
                        return entry.response.get();
                    }
                }
            });
        } else {
            this.scriptComplete = true;
        }
    }

    public void close() {
        spark.stop();
    }

    public URI getURI() {
        try {
            return new URI("http://localhost:" + spark.getPort() + "/");
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    public boolean isScriptComplete() {
        return scriptComplete;
    }

    public static class ScriptEntry {
        private final HttpMethod method;
        private final String path;
        private final Optional<String> response;
        private final Optional<Handler> handler;

        public ScriptEntry(HttpMethod method, String path, String response) {
            this.method = method;
            this.path = path;
            this.response = Optional.of(response);
            this.handler = Optional.absent();
        }

        public ScriptEntry(HttpMethod method, String path, Handler handler) {
            this.method = method;
            this.path = path;
            this.response = Optional.absent();
            this.handler = Optional.of(handler);
        }
    }

    public interface Handler {
        public Object handle(Request request, Response response);
    }
}
