package org.bsc.rmi.jetty_websocket.server;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.jetty_websocket.WebSocketProxyListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

@Slf4j
public class RMIWebsocketFactoryServer extends RMISocketFactory {

    public final RMIEventDispatcherWebsocketFactory client;

    public RMIWebsocketFactoryServer( @NonNull  WebSocketProxyListener eventDispatcherlistener) {
        this.client = new RMIEventDispatcherWebsocketFactory(eventDispatcherlistener);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {

        log.debug("create rmi client socket - host:{} port:{}",
                host,
                port);

        int event_dispatcher_port = 5000;

        return ( port != event_dispatcher_port ) ?
            getDefaultSocketFactory().createSocket(host,port) :
            client.createSocket(host,port);
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        log.debug("create rmi server socket - port:{}",
                port);

        return getDefaultSocketFactory().createServerSocket(port);
    }
}
