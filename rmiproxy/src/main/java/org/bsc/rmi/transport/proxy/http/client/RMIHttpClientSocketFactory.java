package org.bsc.rmi.transport.proxy.http.client;

import lombok.extern.java.Log;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.rmi.server.RMIClientSocketFactory;

import static java.lang.String.format;

/**
 *
 */
@Log
public class RMIHttpClientSocketFactory implements RMIClientSocketFactory {

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        log.info( format("%s.createSocket( %s, %d)", getClass().getSimpleName(), host, port) );

        return new HttpSendSocket(host, port, new URL("http", host, port, "/rmi?forward=1099"));
    }
}
