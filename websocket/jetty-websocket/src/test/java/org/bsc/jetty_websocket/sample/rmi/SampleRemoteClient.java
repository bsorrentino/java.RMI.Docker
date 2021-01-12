package org.bsc.jetty_websocket.sample.rmi;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.jetty_websocket.client.RMIClientWebSocketFactory;
import org.bsc.rmi.jetty_websocket.client.RMIEventHandlerWebSocketFactory;
import org.bsc.rmi.jetty_websocket.client.RMIWebSocketEventHandlerProxy;
import org.bsc.rmi.jetty_websocket.client.RMIWebSocketFactoryClient;
import org.bsc.rmi.sample.SampleRemote;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.util.concurrent.CompletableFuture;

/**
 * RMI client to invoke calls through the ServletHandler
 */
@Slf4j
public class SampleRemoteClient implements Constants {


    /**
     * @param host
     * @param port
     * @return
     */
    private static CompletableFuture<Registry> getRMIRegistry(@NonNull String host, int port) {

        final CompletableFuture<Registry> result = new CompletableFuture<>();

        try {
            final Registry reg = java.rmi.registry.LocateRegistry.getRegistry(host, port);

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

            for (int time = 1; time <= 10; ++time) {

                final String justPassResult = robject.justPass("This is a test of the RMI servlet handler");
                log.info("#{} - sampleRMI.justPass()={}", time, justPassResult);

                final String info = robject.getInfo();
                log.info("#{} - sampleRMI.getInfo()={}", time, info);

                Thread.sleep(1000);
            }
            result.complete(null);

        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }


    public static void main(String args[]) throws Exception {

        final String host = (args.length < 1 || args[0].isEmpty()) ? InetAddress.getLocalHost().getHostAddress() : args[0];

        log.debug(System.getProperty("java.security.policy"));

        System.setSecurityManager(new SecurityManager());

        final RMIClientWebSocketFactory wsClient =
            new RMIClientWebSocketFactory(host, WEBSOCKET_PORT);

        final RMISocketFactory factory =
            RMIWebSocketFactoryClient.builder()
                .clientSocketFactory( wsClient )
                .debug( true )
                .build();

        RMISocketFactory.setSocketFactory( factory );


        getRMIRegistry(host, RMI_PORT)
                .thenCompose(SampleRemoteClient::lookup)
                .thenCompose(SampleRemoteClient::call)
                .exceptionally(e -> {
                    log.error("error", e);
                    //log.throwing(SampleRMIClient.class.getName(), "main", e);
                    return null;
                })
                .join();

        System.exit(0);
    }
}
