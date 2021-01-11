package org.bsc.rmi.jetty_websocket;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@Slf4j
public class Socket2WebSocketProxy extends Socket {
    public final WebSocketProxyListener listener;

    public Socket2WebSocketProxy(@NonNull  String host, int port, @NonNull WebSocketProxyListener listener) throws IOException {
        super(host, port);
        this.listener = listener;

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
