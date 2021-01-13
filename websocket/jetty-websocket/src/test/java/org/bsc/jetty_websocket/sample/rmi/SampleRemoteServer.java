package org.bsc.jetty_websocket.sample.rmi;


import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.jetty_websocket.server.RMIEventDispatcherWebSocketFactory;
import org.bsc.rmi.jetty_websocket.server.RMIWebSocketFactoryServer;
import org.bsc.rmi.jetty_websocket.server.RMIWebSocketServerProxy;
import org.bsc.rmi.sample.SampleRemote;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;

/**
 * Remote object to receive calls forwarded from the ServletHandler.
 */
@Slf4j
public class SampleRemoteServer extends java.rmi.server.UnicastRemoteObject implements SampleRemote, Constants{

    public SampleRemoteServer() throws RemoteException {
        super();
    }

    public String justPass(String passed) throws RemoteException {
        log.info( "justPass( '{}' )", passed );
        return format( "string passed to remote server is [%s]", passed) ;
    }

    @Override
    public String getInfo() throws RemoteException {
        return "I'm a RMI server";
    }

    private static CompletableFuture<Void> startWebSocketServer() {

        final CompletableFuture<Void> result = new CompletableFuture<>();
        try {
            final RMIWebSocketServerProxy wsServer = new RMIWebSocketServerProxy(WEBSOCKET_PORT);

//            final RMIEventDispatcherWebSocketFactory wsEventDispatcher =
//                new RMIEventDispatcherWebSocketFactory(wsServer.eventDispatcherlistener);

            wsServer.start();

            final RMISocketFactory factory =
                RMIWebSocketFactoryServer.builder()
                    //.clientSocketFactory(wsEventDispatcher)
                    .debug(true)
                    .build();

            RMISocketFactory.setSocketFactory( factory );

            result.complete(null);
        }
        catch( Exception e ) {
            result.completeExceptionally(e);
        }
        return result;
    }

    private static CompletableFuture<Void> bind( Registry reg ) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        try {
            reg.bind( "SampleRMI", new SampleRemoteServer() );
            result.complete( null );
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    private static CompletableFuture<Registry> createRMIRegistry() {

        final RMIServerSocketFactory serverSocketFactory = RMISocketFactory.getDefaultSocketFactory();
        final RMIClientSocketFactory clientSocketFactory = RMISocketFactory.getDefaultSocketFactory();

        CompletableFuture<Registry> result = new CompletableFuture<>();

        try {
            final Registry reg = java.rmi.registry.LocateRegistry.createRegistry(RMI_PORT, clientSocketFactory, serverSocketFactory );

            result.complete(reg);

        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    /**
     *
     * @param args
     */
    public static void main(String args[]) throws Exception {

        final String host = InetAddress.getLocalHost().getHostAddress();
        log.info( "Server started on host:{}", host);
        
        log.debug( "java.security.policy={}", System.getProperty("java.security.policy"));

        startWebSocketServer()
            .thenCompose( (v) -> createRMIRegistry() )
            .thenCompose( SampleRemoteServer::bind )
            .exceptionally( ex -> {
                log.error("main", ex);
                return null;
            })
            .join();
    }
}
