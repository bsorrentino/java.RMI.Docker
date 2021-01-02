package org.bsc.simplejsonrpc.server;

import com.github.arteam.simplejsonrpc.server.JsonRpcServer;
import com.github.arteam.simplejsonrpc.server.JsonRpcServerRegistry;
import com.google.common.io.CharStreams;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;

public class JsonRpcHttpServerRegistry extends JsonRpcServerRegistry  {

    public interface HttpServer {
        void start() throws Exception;

        void join() throws InterruptedException;
    }

    private static final Logger log = LoggerFactory.getLogger(JsonRpcHttpServerRegistry.class);

    public JsonRpcHttpServerRegistry(@NotNull JsonRpcServer server) {
        super(server);
    }

    public JsonRpcHttpServerRegistry(@NotNull JsonRpcServer server, @NotNull Object defaultService) {
        super(server, defaultService);

     }

    public HttpServer server( int port ) throws Exception {

        int maxThreads = 10;
        int minThreads = 1;
        int idleTimeout = 120;

        final QueuedThreadPool threadPool =
                new QueuedThreadPool(maxThreads, minThreads, idleTimeout);

        final Server httpServer = new Server(threadPool);

        final ServerConnector connector = new ServerConnector(httpServer);

        connector.setPort(port);

        httpServer.setConnectors(new Connector[]{connector});

        httpServer.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

                log.debug( "\n>\ntarget:{}\nrequestURI:{}\n>",
                        target,
                        baseRequest.getRequestURI());

                final String body = CharStreams.toString(baseRequest.getReader());

                log.debug( ">\n%s\n<", body );

                final Optional<Object> optService = JsonRpcHttpServerRegistry.this.lookup(target);

                final String result =
                        optService.map( service ->
                                JsonRpcHttpServerRegistry.this.getServer().handle( body, service))
                                .orElseThrow( () ->
                                        new IllegalStateException(
                                                format("service for target %s not found!", target)));

                res.getWriter().print(result);
                res.getWriter().flush();

                baseRequest.setHandled(true);
            }
        });

        return new HttpServer() {
            @Override
            public void start() throws Exception {
                httpServer.start();
            }

            @Override
            public void join() throws InterruptedException {
                httpServer.join();
            }
        };

    }

}
