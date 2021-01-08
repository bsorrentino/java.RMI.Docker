package org.bsc.rmi.websocket;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bsc.websocket.sample.EventSocketServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.eclipse.jetty.websocket.server.NativeWebSocketServletContainerInitializer;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;

import javax.servlet.ServletException;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Slf4j
public class RMIWebsocketServerProxy  {

    class RMIConnProxy extends Thread implements Closeable, LifeCycle.Listener {

        final private WebSocketSession session;
        final private Socket socket;
        final private java.io.OutputStream outToClient;

        public RMIConnProxy(@NonNull WebSocketSession session) throws Exception {
            super("RMIConnProxy");
            this.session = session;
            socket = new Socket(session.getLocalAddress().getHostName(), rmi_port);

            outToClient = socket.getOutputStream();

            session.addLifeCycleListener( this );
        }


        int setMessage(@NonNull ByteBuffer bb) {
            if( outToClient == null )
                throw new IllegalStateException("output stream id null");

            try {
                byte[] buf = new byte[bb.remaining()];
                bb.get(buf);
                outToClient.write(buf);
                outToClient.flush();
                return buf.length;
            } catch (IOException e) {
                log.error("error writing to the rmi socket", e);
                return 0;
            }

        }

        @Override
        public void run() {
            try (final java.io.InputStream inFromClient = socket.getInputStream()) {

                byte[] buf = new byte[4096];
                int bytes_read;

                while ((bytes_read = inFromClient.read(buf)) != -1) {

                    log.info("send {} bytes to websocket", bytes_read);
                    session.getRemote().sendBytes(ByteBuffer.wrap(buf, 0, bytes_read));
                    Thread.sleep(100);
                }

            } catch (IOException e) {
                log.error("running thread error {}", e.getMessage());
            } catch (InterruptedException e) {
                log.warn("thread interrupted");
            }


        }

        @Override
        public void close() throws IOException {

//            if( this.isAlive() && !this.isInterrupted() ) {
//                log.info("try to interrupt thread!");
//                this.interrupt();
//            }
            outToClient.close();
            socket.close();
            session.removeLifeCycleListener( this );

        }

        @Override
        public void lifeCycleStarting(LifeCycle lifeCycle) {
            log.trace( "session starting" );
        }

        @Override
        public void lifeCycleStarted(LifeCycle lifeCycle) {
            log.trace( "session started" );
            start();
        }

        @Override
        public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {
            log.trace( "session failure", throwable );
        }

        @Override
        public void lifeCycleStopping(LifeCycle lifeCycle) {
            log.trace( "session stopping" );
        }

        @Override
        public void lifeCycleStopped(LifeCycle lifeCycle) {
            log.trace( "session stopped" );
            try {
                close();
            } catch (IOException e) {
                log.warn( "error closing RMI Session Proxy", e);
            }
        }
    }

    class Listener extends WebSocketAdapter {

        public Listener() {
        }

        private Optional<WebSocketSession> asWebSocketSession(@NonNull Session session ){
            if( session instanceof WebSocketSession) {
                return Optional.of((WebSocketSession) session);
            }
            log.warn( "session {} is not WebSocketSession compliant. IGNORED! ");
            return empty();
        }

        private Optional<RMIConnProxy> getRMIConnProxy( @NonNull Session sess ) {

            return asWebSocketSession(sess).flatMap( wsess ->
                    ofNullable(wsess.getBean(RMIConnProxy.class))
            );
        }

        @Override
        public void onWebSocketConnect(Session sess)
        {
            super.onWebSocketConnect(sess);

            log.info("connected session {}",sess);

            asWebSocketSession( sess ).ifPresent( wsess -> {
                try {

                    final RMIConnProxy connProxy = new RMIConnProxy(wsess);
                    wsess.addBean( connProxy, false);

                } catch (Exception e) {
                    log.error("create RMI session proxy error", e);
                }
            });
        }

        @Override
        public void onWebSocketClose(int statusCode, String reason)
        {
            super.onWebSocketClose(statusCode, reason);
            log.info("closed conn {}, {}", statusCode, reason);
        }

        @Override
        public void onWebSocketText(String message)
        {
            final Session sess = getSession();

            getRMIConnProxy(sess).ifPresent( proxy ->
                    proxy.setMessage( ByteBuffer.wrap( message.getBytes(StandardCharsets.UTF_8)))
            );

        }

        @Override
        public void onWebSocketBinary(byte[] payload, int offset, int len) {

            final Session sess = getSession();

            getRMIConnProxy(sess).ifPresent( proxy ->
                    proxy.setMessage( ByteBuffer.wrap( payload, offset, len ) )
            );
        }

        @Override
        public void onWebSocketError(Throwable cause)
        {
            super.onWebSocketError(cause);
            log.error("web socket error", cause);

        }

    }
    final Server server = new Server();

    final int rmi_port;

    public RMIWebsocketServerProxy(int websocket_port, int rmi_port) throws Exception {

        this.rmi_port = rmi_port;

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

                    // Add websockets
                    nativeWebSocketConfiguration.addMapping("/rmi/*", (req,res) -> new Listener());
                });

        // Add generic filter that will accept WebSocket upgrade.
        WebSocketUpgradeFilter.configure(context);

    }

    public void start() throws Exception {
        server.start();
    }




}
