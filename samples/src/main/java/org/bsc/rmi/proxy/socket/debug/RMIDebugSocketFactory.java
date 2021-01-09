package org.bsc.rmi.proxy.socket.debug;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

public class RMIDebugSocketFactory extends RMISocketFactory {

    final RMIDebugClientSocketFactory client = new RMIDebugClientSocketFactory();
    final RMIDebugServerSocketFactory server = new RMIDebugServerSocketFactory();

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return client.createSocket(host,port);
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return server.createServerSocket(port);
    }
}
