package org.bsc.rmi.jetty_websocket;

import org.bsc.rmi.proxy.socket.debug.RMIDebugServerSocketFactory;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

public class RMIWebsocketFactory extends RMISocketFactory {

    final int websocket_port;

    final RMIClientWebsocketFactory client;
    final RMIDebugServerSocketFactory server = new RMIDebugServerSocketFactory();

    public RMIWebsocketFactory(int websocket_port) {
        this.websocket_port = websocket_port;
        client = new RMIClientWebsocketFactory(websocket_port);
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
