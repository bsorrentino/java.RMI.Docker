package org.bsc.rmi.jetty_websocket.client;

import lombok.NonNull;
import org.bsc.rmi.proxy.socket.debug.RMIDebugServerSocketFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

public class RMIEventHandlerWebsocketFactory2 implements RMIServerSocketFactory {

    final RMIWebsocketEventHandlerProxy eventHandlerProxy;

    final RMIServerSocketFactory delegate = new RMIDebugServerSocketFactory();

    public RMIEventHandlerWebsocketFactory2( @NonNull  RMIWebsocketEventHandlerProxy eventHandlerProxy) {

        this.eventHandlerProxy = eventHandlerProxy;
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        final ServerSocket ss =  delegate.createServerSocket(port);
        try {
            eventHandlerProxy.start();
        } catch (Exception e) {
            throw new IOException(e);
        }
        return ss;
    }
}
