package org.bsc.rmi.websocket;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.common.WebSocketSession;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Slf4j
public class RMIWebsocketServerProxy extends WebSocketAdapter {

    class RMIConnProxy extends Thread implements Closeable {
/*

        final private WebSocket conn;
        final private Socket socket;
        final private java.io.OutputStream outToClient;

        public RMIConnProxy(@NonNull WebSocket conn) throws Exception {
            super("RMIConnProxy");
            //this.conn = conn;
            socket = new Socket(getAddress().getHostName(), rmi_port);

            outToClient = socket.getOutputStream();
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
                    conn.send(ByteBuffer.wrap(buf, 0, bytes_read));
                    Thread.sleep(100);
                }

            } catch (IOException e) {
                log.error("running thread error {}", e.getMessage());
            } catch (InterruptedException e) {
                log.warn("thread interrupted");
            }


        }
*/
        @Override
        public void close() throws IOException {
/*

//            if( this.isAlive() && !this.isInterrupted() ) {
//                log.info("try to interrupt thread!");
//                this.interrupt();
//            }
            outToClient.close();
            socket.close();
 */
        }

    }

    final int rmi_port;

    public RMIWebsocketServerProxy(int rmi_port) {
        this.rmi_port = rmi_port;

    }

    private Optional<RMIConnProxy> getRMIConnProxy( @NonNull Session sess ) {

        if( sess instanceof WebSocketSession) {
            final WebSocketSession wsess = (WebSocketSession) sess;

            final RMIConnProxy proxy = wsess.getBean(RMIConnProxy.class);

            return ofNullable(proxy);
        }

        return empty();
    }
/*

    @Override
    public void onOpen(WebSocket conn, ClientHandshake clientHandshake) {
        log.info("opened conn {}", conn.getRemoteSocketAddress().getAddress().getHostAddress());

        try {
            final RMIConnProxy connProxy = new RMIConnProxy(conn);
            conn.setAttachment(connProxy);
            connProxy.start();

        } catch (Exception e) {
            log.error( "create RMI connection proxy error", e );
        }
    }

    @Override
    public void onClose(WebSocket conn, int i, String s, boolean b) {
        log.info("closed conn {}", conn);

        getRMIConnProxy(conn).ifPresent( proxy ->  {
                try {
                    proxy.close();
                } catch (IOException e) {
                    log.warn( "error closing RMI connection proxy", e);
                }
        });

    }

    @Override
    public void onMessage(WebSocket conn, String s) {

        getRMIConnProxy(conn).ifPresent( proxy ->
                proxy.setMessage( ByteBuffer.wrap( s.getBytes(StandardCharsets.UTF_8)))
        );
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {

        getRMIConnProxy(conn).ifPresent( proxy ->
            proxy.setMessage( message )
        );
    }
*/

    @Override
    public void onWebSocketError(Throwable cause)
    {
        super.onWebSocketError(cause);
        log.error("web socket error", cause);

    }


    @Override
    public void onWebSocketConnect(Session sess)
    {
        super.onWebSocketConnect(sess);
        log.info( "web socket server started on port {} proxy to RMI server started on port {}", 0, rmi_port );
    }


}
