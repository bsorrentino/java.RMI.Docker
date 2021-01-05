package org.bsc.samples;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
@Slf4j
public class ExampleServer extends WebSocketServer {

    public ExampleServer(int port) {
        super(new InetSocketAddress(port));
    }

    public ExampleServer(InetSocketAddress address) {
        super(address);
    }

    public ExampleServer(int port, Draft_6455 draft) {
        super(new InetSocketAddress(port), singletonList(draft));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("Welcome to the server!"); //This method sends a message to the new client

        broadcast(format( "new connection: %s", handshake.getResourceDescriptor())); //This method sends a message to all clients connected

        log.info( "{} entered the room!", conn.getRemoteSocketAddress().getAddress().getHostAddress() );
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        broadcast(format( "%s has left the room!", conn));
        log.info("{} has left the room!", conn );
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        broadcast(message);
        log.info( "{} : {}", conn.getResourceDescriptor(), message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        broadcast(message.array());
        log.info(conn + ": " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        log.error( "socket error", ex );
        if (conn != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    @Override
    public void onStart() {
        log.info("Server started!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        int port = 8887; // 843 flash policy port

        final ExampleServer s = new ExampleServer(port);
        s.start();
        log.info("ChatServer started on port: " + s.getPort());

        try (BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in)) ) {

            while (true) {
                final String in = sysin.readLine();
                s.broadcast(in);
                if (in.equals("exit")) {
                    s.stop(1000);
                    break;
                }
            }
        }
    }


}
