package org.bsc.jetty_websocket.sample.rmi;


import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.jetty_websocket.RMIWebsocketServerProxy;
import org.bsc.rmi.sample.SampleRemote;

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
public class SampleRemoteServer extends java.rmi.server.UnicastRemoteObject implements SampleRemote {

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

    /**
     * You should not need to run this server from the command line.
     * The ServletHandler class creates its own instance of the
     * rmiregistry and (optionally) an instance of this class as well.
     * This main method will not be executed from the ServletHandler.
     */

    static int RMI_PORT  = 1099;
    static int WEBSOCKET_PORT  = 8887;


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

    private static CompletableFuture<Void> startWebSocketServer( Void param ) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        try {
            final RMIWebsocketServerProxy s = new RMIWebsocketServerProxy(WEBSOCKET_PORT, RMI_PORT);
            s.start();

            result.complete( null );
        }
        catch( Exception e ) {
            result.completeExceptionally(e);
        }

        return result;
    }

    /**
     *
     * @param args
     */
    public static void main(String args[]) {

        log.debug( "java.security.policy={}", System.getProperty("java.security.policy"));

        createRMIRegistry()
            .thenCompose( SampleRemoteServer::bind )
            .thenCompose( SampleRemoteServer::startWebSocketServer )
            .exceptionally( ex -> {
                log.error("main", ex);
                return null;
            })
            .join();
    }
}
