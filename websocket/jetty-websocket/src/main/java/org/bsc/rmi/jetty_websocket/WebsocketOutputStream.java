package org.bsc.rmi.jetty_websocket;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;
import java.nio.ByteBuffer;

import static java.util.Optional.ofNullable;

@Slf4j
public class WebsocketOutputStream<T extends WebSocketAdapter> extends java.io.OutputStream {
    final T wsListener;

    public WebsocketOutputStream(@NonNull T wsListener) {
        this.wsListener = wsListener;
    }

    @Override
    public void write(int b) throws IOException {
        throw new UnsupportedOperationException("write(int) is not supported!");
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        log.info( "write bytes( off:{}, len:{}, b.length:{} )", off, len, b.length);

        final Session s = wsListener.getSession();
        if( s == null ) {
            log.warn( "session is null!");
            return;
        }

        final RemoteEndpoint r = s.getRemote();
        if( r == null ) {
            log.warn( "remote endpoint is null!");
            return;
        }

       r.sendBytes( ByteBuffer.wrap(b, off, len) );
    }
}