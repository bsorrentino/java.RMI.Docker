package org.bsc.rmi.sample;

import lombok.NonNull;
import lombok.extern.java.Log;
import org.bsc.rmi.proxy.socket.debug.RMIDebugClientSocketFactory;

import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static java.lang.String.format;

/**
 * RMI client to invoke calls through the ServletHandler
 */
@Log
public class SampleRMIClient {

    /**
     * @param host
     * @param port
     * @return
     */
    private static CompletableFuture<Registry> getRMIRegistry(@NonNull String host, int port) {

        //final RMIClientSocketFactory clientSocketFactory = new RMIHttpClientSocketFactory();
        //final RMIClientSocketFactory clientSocketFactory = RMISocketFactory.getDefaultSocketFactory();
        final RMIClientSocketFactory clientSocketFactory = new RMIDebugClientSocketFactory();

        CompletableFuture<Registry> result = new CompletableFuture<>();

        try {
            final Registry reg = java.rmi.registry.LocateRegistry.getRegistry(host, port, clientSocketFactory);

            result.complete(reg);

        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    private static CompletableFuture<SampleRemote> lookup(Registry reg) {
        CompletableFuture<SampleRemote> result = new CompletableFuture<>();
        try {
            final SampleRemote robject = (SampleRemote) reg.lookup("SampleRMI");
            result.complete(robject);
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    private static CompletableFuture<Void> call(SampleRemote robject) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        try {

            for (int time = 1; time <= 1; ++time) {
                final String justPassResult = robject.justPass("This is a test of the RMI servlet handler");

                log.info(format("#%d - sampleRMI.justPass()=%s", time, justPassResult));

                Thread.sleep(1000);
            }
            result.complete(null);

        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }


    public static void main(String args[]) {

        final String host = (args.length < 1) ? "localhost" : args[0];

        log.info(System.getProperty("java.security.policy"));

        System.setSecurityManager(new SecurityManager());

        int port = 1099;

        getRMIRegistry(host, 1099)
                .thenCompose(SampleRMIClient::lookup)
                .thenCompose(SampleRMIClient::call)
                .exceptionally(e -> {
                    log.log(Level.SEVERE, "error", e);
                    //log.throwing(SampleRMIClient.class.getName(), "main", e);
                    return null;
                })
                .join();

        System.exit(0);
    }
}
