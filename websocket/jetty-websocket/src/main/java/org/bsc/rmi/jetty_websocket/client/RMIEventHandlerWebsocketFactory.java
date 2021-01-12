package org.bsc.rmi.jetty_websocket.client;

import lombok.NonNull;
import org.bsc.rmi.proxy.socket.debug.RMIDebugServerSocketFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;

public class RMIEventHandlerWebsocketFactory implements RMIServerSocketFactory {


    final RMIWebsocketEventHandlerProxy eventHandlerProxy;

    RMIServerSocketFactory delegate = new RMIDebugServerSocketFactory();

    public RMIEventHandlerWebsocketFactory(@NonNull  RMIWebsocketEventHandlerProxy eventHandlerProxy) {
        this.delegate = RMISocketFactory.getDefaultSocketFactory();
        this.eventHandlerProxy = eventHandlerProxy;
    }

    public void setDelegate( @NonNull  RMIServerSocketFactory delegate) {
        this.delegate = delegate;
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
