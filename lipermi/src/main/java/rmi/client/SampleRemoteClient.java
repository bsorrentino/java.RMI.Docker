package rmi.client;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.sf.lipermi.rmi.Registry;
import rmi.Constants;
import rmi.SampleRemote;

import java.net.InetAddress;
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

            final Registry reg = net.sf.lipermi.rmi.LocateRegistry.getRegistry(host, port);

            result.complete(reg);

        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    private static CompletableFuture<SampleRemote> lookup(Registry reg) {
        CompletableFuture<SampleRemote> result = new CompletableFuture<>();
        try {
            final SampleRemote robject = reg.lookup(SampleRemote.class);
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

        getRMIRegistry(host, RMI_PORT)
                .thenCompose(SampleRemoteClient::lookup)
                .thenCompose(SampleRemoteClient::call)
                .exceptionally(e -> {
                    log.error("error", e);
                    return null;
                })
                .join();

        System.exit(0);
    }
}
