package org.bsc.rmi.jetty_websocket;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Slf4j
public class WebSocketProxyListener extends WebSocketAdapter {

    final BlockingByteArrayInputStream istream = new BlockingByteArrayInputStream();
    final java.io.OutputStream          ostream = new WebsocketOutputStream<>(this);

    public WebSocketProxyListener() {}

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
