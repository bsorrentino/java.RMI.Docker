package org.bsc.rmi.jetty_websocket.client;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.jetty_websocket.WebSocket2SocketProxy;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Optional;

import static java.util.Optional.empty;

/**
 * RMIServerSocketFactory invoked on the client to handle event from the server
 *
 */
@Slf4j
public class RMIEventHandlerWebsocketFactory implements RMIServerSocketFactory {


/*
    @EqualsAndHashCode
    static class EventHandlerSocket extends Socket {

        public EventHandlerSocket() {}

        @Override
        public InputStream getInputStream() throws IOException {
            return new DebugInputStream(super.getInputStream());
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return new DebugOutputStream(super.getOutputStream());
        }

        @Override
        public synchronized void close() throws IOException {
            super.close();
        }

        @Override
        public boolean isConnected() {
            return super.isConnected();
        }

        @Override
        public boolean isBound() {
            return super.isBound();
        }

        @Override
        public boolean isClosed() {
            return super.isClosed();
        }
    }
*/

    @EqualsAndHashCode
    class EventHandlerServerSocket extends ServerSocket {

        final WebSocket2SocketProxy websocketClientProxy;

        public EventHandlerServerSocket(int websocket_port, int rmi_port) throws IOException {
            super(rmi_port);

            try {
                final String host = super.getInetAddress().getHostName();
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

        public void setRMIPort( int rmi_port ) {
            websocketClientProxy.setRMIPort( rmi_port );
        }

        @Override
        public Socket accept() throws IOException {

            final int rmi_port = websocketClientProxy.getRMIPort();
            final String host = super.getInetAddress().getHostName();

            log.debug( "accept  Event Handler Server Socket - host:{} rmi port: {}", host, rmi_port);

            return websocketClientProxy;
        }

    }

    Optional<EventHandlerServerSocket> serverSocket;

    public RMIEventHandlerWebsocketFactory(int websocket_port) {
        try {
            serverSocket = Optional.of(new EventHandlerServerSocket(websocket_port, 0));
        } catch (IOException e) {
            log.error( "error creating Event Handler Server Socket", e );
            serverSocket = empty();
        }

    }

    @Override
    public ServerSocket createServerSocket(int rmi_port) throws IOException {

        return serverSocket.map( result -> {
            result.setRMIPort(rmi_port);
            return result;
        }).orElseThrow( () -> new IllegalStateException( "Event Handler Server Socket is null"));
    }
}
