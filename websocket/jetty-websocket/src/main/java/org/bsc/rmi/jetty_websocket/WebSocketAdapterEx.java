package org.bsc.rmi.jetty_websocket;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.common.WebSocketSession;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Slf4j
public class WebSocketAdapterEx extends WebSocketAdapter {

    protected Optional<WebSocketSession> asWebSocketSession(@NonNull Session session ){
        if( session instanceof WebSocketSession) {
            return Optional.of((WebSocketSession) session);
        }
        log.warn( "session {} is not WebSocketSession compliant. IGNORED! ");
        return empty();
    }

    protected <T> Optional<T> getBean(@NonNull Session sess, @NonNull Class<T> clazz ) {

        return asWebSocketSession(sess).flatMap( wsess ->
                ofNullable(wsess.getBean(clazz))
        );
    }

}