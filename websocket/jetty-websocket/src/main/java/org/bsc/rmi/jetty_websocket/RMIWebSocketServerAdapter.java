package org.bsc.rmi.jetty_websocket;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.common.WebSocketSession;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Slf4j
public class RMIWebSocketServerAdapter extends WebSocketAdapter {

    class RMISession extends Thread implements Closeable {
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

    protected Optional<WebSocketSession> asWebSocketSession(@NonNull Session session ){
        if( session instanceof WebSocketSession) {
            return Optional.of((WebSocketSession) session);
        }
        log.warn( "session {} is not WebSocketSession compliant. IGNORED! ");
        return empty();
    }

    protected <T> Optional<T> getBean(@NonNull Session sess, @NonNull Class<T> clazz ) {

        return asWebSocketSession(sess).flatMap( wsess ->
                ofNullable(wsess.getBean(clazz))
        );
    }

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
                final RMISession proxy = wsess.getBean( RMISession.class);
                wsess.removeBean(proxy);
                proxy.close();


            } catch (Exception e) {
                log.warn( "error closing RMI Session Proxy", e);
            }
        });
        super.onWebSocketClose(statusCode, reason);

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