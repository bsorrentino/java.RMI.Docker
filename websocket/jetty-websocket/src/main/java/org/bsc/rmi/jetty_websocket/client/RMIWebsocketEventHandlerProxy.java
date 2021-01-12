package org.bsc.rmi.jetty_websocket.client;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.jetty_websocket.RMIWebSocketServerAdapter;
import org.bsc.rmi.jetty_websocket.server.RMIWebsocketServerProxy;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.common.WebSocketSession;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.Future;

import static java.lang.String.format;

@Slf4j
public class RMIWebsocketEventHandlerProxy {

    final WebSocketListener listener = new RMIWebSocketServerAdapter();
    final WebSocketClient client = new WebSocketClient();

    final java.net.URI wsURI;
    final int rmi_port;

    public RMIWebsocketEventHandlerProxy(@NonNull String host, int websocket_port, int rmi_port) {
        this.rmi_port = rmi_port;

        final String websocket_path = "rmi/push";

        log.debug("create rmi client socket - host:{} rmi port:{} websocket port:{} websocket path:{}",
                host,
                rmi_port,
                websocket_port, websocket_path);

        wsURI = java.net.URI.create(format("ws://%s:%d/%s", host, websocket_port, websocket_path));

    }

    public void start() throws Exception {
        client.start();

        final ClientUpgradeRequest request = new ClientUpgradeRequest();
        request.setHeader("rmi_port", String.valueOf(rmi_port));

        final Future<Session> sessionFuture = client.connect(listener,wsURI, request);

        sessionFuture.get();


    }
}
