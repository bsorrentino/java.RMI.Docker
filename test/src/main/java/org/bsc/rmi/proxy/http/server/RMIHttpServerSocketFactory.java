package org.bsc.rmi.proxy.http.server;

import lombok.extern.java.Log;
import org.bsc.rmi.transport.proxy.http.server.HttpAwareServerSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

import static java.lang.String.format;

/**
 *
 */
@Log
public class RMIHttpServerSocketFactory implements RMIServerSocketFactory {

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        log.info( format("%s.createServerSocket(%d)", getClass().getSimpleName(), port) );
        return new org.bsc.rmi.transport.proxy.http.server.HttpAwareServerSocket(port);
    }
}
