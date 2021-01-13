package org.bsc.rmi.jetty_websocket.client;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.jetty_websocket.WebSocket2SocketProxy;

import java.io.IOException;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

@Slf4j
public class RMIClientWebSocketFactory implements RMIClientSocketFactory {

    public final int websocket_port;
    public final String websocket_host;

    public RMIClientWebSocketFactory(@NonNull String websocket_host, int websocket_port ) {
        this.websocket_host = websocket_host;
        this.websocket_port = websocket_port;
    }

    @Override
    public Socket createSocket(String host, int rmi_port) throws IOException {
        if( !host.equals(websocket_host) ) {
            log.warn( "RMI provided host:{} is different from given websocket_host:{}", host, websocket_host);
            host = websocket_host;
        }

        try {
            return new WebSocket2SocketProxy(host, websocket_port, "rmi/pull", rmi_port);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }
}
