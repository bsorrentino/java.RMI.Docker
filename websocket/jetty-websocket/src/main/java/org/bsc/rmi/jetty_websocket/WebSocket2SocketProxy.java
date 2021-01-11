package org.bsc.rmi.jetty_websocket;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;

import static java.lang.String.format;

@Slf4j
@EqualsAndHashCode
public class WebSocket2SocketProxy extends Socket {


    static class RMIWebSocketClientListener extends WebSocketAdapter {

        final BlockingByteArrayInputStream istream = new BlockingByteArrayInputStream();
        final java.io.OutputStream          ostream = new WebsocketOutputStream<>(this);

        public RMIWebSocketClientListener() {
        }

        @Override
        public void onWebSocketConnect(Session sess)
        {
            super.onWebSocketConnect(sess);
            log.debug("onWebSocketConnect( {} )", sess);
        }


        @Override
        public void onWebSocketText(String message)
        {
            log.debug("onMessage string {}", message);
            istream.setMessage( ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)) );

        }

        @Override
        public void onWebSocketBinary(byte[] payload, int offset, int len)
        {
            log.debug("onMessage bytes {}", len);
            istream.setMessage( ByteBuffer.wrap(payload,offset,len) );
        }

        @Override
        public void onWebSocketClose(int statusCode, String reason)
        {
            log.debug("onWebSocketClose( {}, {} )", statusCode, reason);
            try {
                istream.close();
            } catch (IOException e) {
                log.error( "error closing the input stream", e);
            }
            try {
                ostream.close();
            } catch (IOException e) {
                log.error( "error closing the output stream", e);
            }
            super.onWebSocketClose(statusCode, reason);
        }

        @Override
        public void onWebSocketError(Throwable cause)
        {
            log.error("web socket error", cause);

        }
    }

    final RMIWebSocketClientListener listener;
    final WebSocketClient client = new WebSocketClient();

    public WebSocket2SocketProxy(@NonNull String host, int websocket_port, @NonNull String websocket_path, int rmi_port) throws Exception {
        super(host, websocket_port);

        log.debug("create rmi client socket - host:{} rmi port:{} websocket port:{} websocket path:{}",
                host,
                rmi_port,
                websocket_port, websocket_path);

        listener = new RMIWebSocketClientListener();

        client.start();

        final java.net.URI uri =
                java.net.URI.create(format("ws://%s:%d/%s", host, websocket_port, websocket_path));

        final ClientUpgradeRequest request = new ClientUpgradeRequest();
        request.setHeader("rmi_port", String.valueOf(rmi_port));

        final Future<Session> sessionFuture = client.connect(listener,uri, request);

        sessionFuture.get();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return listener.istream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return listener.ostream;
    }


}
