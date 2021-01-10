package org.bsc.rmi.jetty_websocket;

import org.bsc.rmi.jetty_websocket.client.RMIClientWebsocketFactory;
import org.bsc.rmi.jetty_websocket.client.RMIEventHandlerWebsocketFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;

public class RMIWebsocketFactoryClient extends RMISocketFactory {

    final RMIClientWebsocketFactory client;
    final RMIServerSocketFactory server;

    public RMIWebsocketFactoryClient(int websocket_port) {
        client = new RMIClientWebsocketFactory(websocket_port);
        server = new RMIEventHandlerWebsocketFactory(websocket_port);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return client.createSocket(host, port);
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return server.createServerSocket(port);
    }
}
