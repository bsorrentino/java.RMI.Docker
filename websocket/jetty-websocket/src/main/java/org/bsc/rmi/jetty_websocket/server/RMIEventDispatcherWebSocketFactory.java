package org.bsc.rmi.jetty_websocket.server;

import lombok.NonNull;
import org.bsc.rmi.jetty_websocket.Socket2WebSocketProxy;
import org.bsc.rmi.jetty_websocket.WebSocketProxyListener;

import java.io.IOException;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

public class RMIEventDispatcherWebSocketFactory implements RMIClientSocketFactory {

    public final WebSocketProxyListener eventDispatcherlistener;

    public RMIEventDispatcherWebSocketFactory(@NonNull WebSocketProxyListener eventDispatcherlistener) {
        this.eventDispatcherlistener = eventDispatcherlistener;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {

        return new Socket2WebSocketProxy(host,port,eventDispatcherlistener);
    }
}
