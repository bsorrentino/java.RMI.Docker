package org.bsc.websocket.sample;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

/**
 * This example demonstrates how to create a websocket connection to a server. Only the most
 * important callbacks are overloaded.
 */
@Slf4j
public class ExampleClient extends WebSocketClient {

    public ExampleClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    public ExampleClient(URI serverURI) {
        super(serverURI);
    }

    public ExampleClient(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        send("Hello, it is me. Mario :)");
        log.info("opened connection");
        // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
    }

    @Override
    public void onMessage(String message) {
        log.info("received: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

        // The codecodes are documented in class org.java_websocket.framing.CloseFrame

        log.info( "Connection closed by {}  Code: {} Reason: {}",
                (remote ? "remote peer" : "us"),
                code,
                reason);
    }

    @Override
    public void onError(Exception ex) {
        log.error( "socket error", ex );
        // if the error is fatal then onClose will be called additionally
    }

    public static void main(String[] args) throws URISyntaxException {
        final ExampleClient c = new ExampleClient(new URI(
                "ws://localhost:8887")); // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
        c.connect();
    }

}
