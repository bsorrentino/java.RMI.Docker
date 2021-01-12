package org.bsc.rmi.jetty_websocket.client;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.jetty_websocket.WebSocketAdapterEx;
import org.bsc.rmi.jetty_websocket.server.RMIWebsocketServerProxy;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.common.WebSocketSession;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.Future;

import static java.lang.String.format;

@Slf4j
public class RMIWebsocketEventHandlerProxy {

    public static class RMISession extends Thread implements Closeable {
        final private WebSocketSession session;
        final private Socket rmi_socket;
        final private java.io.OutputStream outToClient;


        public RMISession(@NonNull WebSocketSession session, int rmi_port) throws Exception {
            super("RMISession");
            this.session = session;
            final String host = session.getLocalAddress().getHostString();
            log.debug( "creating rmi proxy socket host:{} rmi_port:{}", host, rmi_port);
            rmi_socket = new Socket(host, rmi_port);

            outToClient = rmi_socket.getOutputStream();

        }

        /**
         * @param bb
         * @return
         */
        public int setMessage(@NonNull ByteBuffer bb) {
            if (outToClient == null)
                throw new IllegalStateException("output stream id null");
            if (log.isDebugEnabled())
                log.debug("write bytes {}", bb.remaining());

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
            try (final java.io.InputStream inFromClient = rmi_socket.getInputStream()) {

                byte[] buf = new byte[4096];
                int bytes_read;

                while ((bytes_read = inFromClient.read(buf)) != -1) {

                    log.debug("send {} bytes to websocket", bytes_read);
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
            rmi_socket.close();

        }
    }

    class Listener extends WebSocketAdapterEx {

        private Optional<RMISession> getRMIConnProxy(@NonNull Session sess ) {
            return getBean( sess, RMISession.class);
        }

        @Override
        public void onWebSocketConnect(Session sess)
        {
            super.onWebSocketConnect(sess);

            final UpgradeRequest req = sess.getUpgradeRequest();

            final int rmi_port = req.getHeaderInt("rmi_port");

            log.debug("connected session\nuri={}\nrmi_port={}",
                    req.getRequestURI(),
                    rmi_port);


            asWebSocketSession( sess ).ifPresent( wsess -> {
                try {

                    //  Attach RMISession to WS Session
                    final RMISession connProxy = new RMISession(wsess, rmi_port);
                    wsess.addBean( connProxy, false);
                    connProxy.start();

                } catch (Exception e) {
                    log.error("create RMI session proxy error", e);
                }
            });
        }

        @Override
        public void onWebSocketClose(int statusCode, String reason)
        {
            log.info("closed conn {}, {}", statusCode, reason);

            asWebSocketSession( getSession() ).ifPresent( wsess -> {
                try {

                    //  Detach RMISession to WS Session
                    final RMIWebsocketServerProxy.RMISession proxy = wsess.getBean( RMIWebsocketServerProxy.RMISession.class);
                    wsess.removeBean(proxy);
                    proxy.close();


                } catch (Exception e) {
                    log.warn( "error closing RMI Session Proxy", e);
                }
            });
            super.onWebSocketClose(statusCode, reason);

        }

        @Override
        public void onWebSocketText(String message) {
            getRMIConnProxy(getSession()).ifPresent( proxy ->
                    proxy.setMessage( ByteBuffer.wrap( message.getBytes(StandardCharsets.UTF_8)))
            );

        }

        @Override
        public void onWebSocketBinary(byte[] payload, int offset, int len) {
            getRMIConnProxy(getSession()).ifPresent( proxy ->
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

    final Listener listener = new Listener();
    final WebSocketClient client = new WebSocketClient();

    final java.net.URI wsURI;
    final int rmi_port;

    public RMIWebsocketEventHandlerProxy(@NonNull String host, int websocket_port, int rmi_port) {
        this.rmi_port = rmi_port;

        final String websocket_path = "rmi/push";

        log.debug("create rmi client socket - host:{} rmi port:{} websocket port:{} websocket path:{}",
                host,
                rmi_port,
                websocket_port, websocket_path);

        wsURI = java.net.URI.create(format("ws://%s:%d/%s", host, websocket_port, websocket_path));

    }

    public void start() throws Exception {
        client.start();

        final ClientUpgradeRequest request = new ClientUpgradeRequest();
        request.setHeader("rmi_port", String.valueOf(rmi_port));

        final Future<Session> sessionFuture = client.connect(listener,wsURI, request);

        sessionFuture.get();


    }
}
