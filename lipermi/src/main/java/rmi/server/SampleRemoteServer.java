package rmi.server;


import lombok.extern.slf4j.Slf4j;
import net.sf.lipermi.rmi.Registry;
import net.sf.lipermi.rmi.UnicastRemoteObject;
import rmi.Constants;
import rmi.SampleRemote;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;

/**
 * Remote object to receive calls forwarded from the ServletHandler.
 */
@Slf4j
public class SampleRemoteServer extends UnicastRemoteObject implements SampleRemote, Constants {

    public SampleRemoteServer(int rmi_port) throws RemoteException {
        super(rmi_port);
    }

    public String justPass(String passed) throws RemoteException {
        log.info( "justPass( '{}' )", passed );
        return format( "string passed to remote server is [%s]", passed) ;
    }

    @Override
    public String getInfo() throws RemoteException {
        return "I'm a RMI server";
    }


    private static CompletableFuture<Void> bind( Registry reg ) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        try {
            reg.bind( SampleRemote.class, new SampleRemoteServer(RMI_PORT) );
            result.complete( null );
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    private static CompletableFuture<Registry> createRMIRegistry() {

        final CompletableFuture<Registry> result = new CompletableFuture<>();

        try {
            final Registry reg = net.sf.lipermi.rmi.LocateRegistry.createRegistry(RMI_PORT);

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

        createRMIRegistry()
            .thenCompose( SampleRemoteServer::bind )
            .exceptionally( ex -> {
                log.error("main", ex);
                return null;
            })
            .join();
    }
}
