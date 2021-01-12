package org.bsc.rmi.jetty_websocket.server;

import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.jetty_websocket.RMIWebSocketServerAdapter;
import org.bsc.rmi.jetty_websocket.WebSocketProxyListener;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.server.NativeWebSocketServletContainerInitializer;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;

@Slf4j
public class RMIWebSocketServerProxy {


    final Server server = new Server();
    public final WebSocketProxyListener eventDispatcherlistener = new WebSocketProxyListener();

    public RMIWebSocketServerProxy(int websocket_port) throws Exception {

        final ServerConnector connector = new ServerConnector(server);
        connector.setPort(websocket_port);
        server.addConnector(connector);

        // Setup the basic application "context" for this application at "/"
        // This is also known as the handler tree (in jetty speak)
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Configure specific websocket behavior
        NativeWebSocketServletContainerInitializer.configure(context,
                (servletContext, nativeWebSocketConfiguration) ->
                {
                    // Configure default max size
                    nativeWebSocketConfiguration.getPolicy().setMaxTextMessageBufferSize(65535);

                    final WebSocketListener listener = new RMIWebSocketServerAdapter();

                    // Add websockets
                    nativeWebSocketConfiguration.addMapping("/rmi/pull", (req,res) -> listener);
                    nativeWebSocketConfiguration.addMapping("/rmi/push", (req,res) -> eventDispatcherlistener);
                });

        // Add generic filter that will accept WebSocket upgrade.
        WebSocketUpgradeFilter.configure(context);

    }

    public void start() throws Exception {
        server.start();
    }




}
