package org.bsc.rmi.jetty_websocket.client;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.jetty_websocket.WebSocket2SocketProxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIServerSocketFactory;

/**
 * RMIServerSocketFactory invoked on the client to handle event from the server
 *
 */
@Deprecated
@Slf4j
public class RMIEventHandlerWebsocketFactory implements RMIServerSocketFactory {


    @EqualsAndHashCode
    class EventHandlerServerSocket extends ServerSocket {

        final WebSocket2SocketProxy websocketClientProxy;

        public EventHandlerServerSocket(@NonNull String host, int websocket_port, int rmi_port) throws IOException {
            super(rmi_port);

            try {
                final String websocket_path = "rmi/push";

                log.debug( "create rmi Event Handler Server Socket - host:{} rmi port: {} websocket port:{} websocket path:{}",
                        host,
                        rmi_port,
                        websocket_port, websocket_path);

                websocketClientProxy =
                        new WebSocket2SocketProxy(host,
                                websocket_port,
                                websocket_path,
                                rmi_port);
            } catch (IOException e) {
                throw e;
            } catch (Throwable e) {
                throw new IOException(e);
            }

        }

        @Override
        public Socket accept() throws IOException {

            log.debug( "accept  Event Handler Server Socket");

            return websocketClientProxy;
        }

    }

    final EventHandlerServerSocket serverSocket;

    public RMIEventHandlerWebsocketFactory(@NonNull String host, int websocket_port, int rmi_event_port) throws IOException {
        serverSocket = new EventHandlerServerSocket(host, websocket_port, rmi_event_port);
    }

    @Override
    public ServerSocket createServerSocket(int rmi_port) throws IOException {
        return serverSocket;
    }
}
