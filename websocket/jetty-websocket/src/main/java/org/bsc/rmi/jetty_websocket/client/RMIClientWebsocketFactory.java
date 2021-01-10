package org.bsc.rmi.jetty_websocket.client;

import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.jetty_websocket.WebSocket2SocketProxy;

import java.io.IOException;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

@Slf4j
public class RMIClientWebsocketFactory implements RMIClientSocketFactory {

    final int websocket_port;

    public RMIClientWebsocketFactory(int websocket_port) {
        this.websocket_port = websocket_port;
    }

    @Override
    public Socket createSocket(String host, int rmi_port) throws IOException {
        try {
            return new WebSocket2SocketProxy(host, websocket_port, "rmi/pull", rmi_port);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }
}
