package org.bsc.rmi.sample;

import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.websocket.RMIWebsocketServerProxy;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

@Slf4j
public class TemperatureDispatchServer
{


    public static void rebindByUrl(Remote obj ) throws Exception {
        final String url = "rmi://" + InetAddress.getLocalHost().getHostAddress() + ":52369/Hello";
        Naming.rebind(url, obj);

    }

    private static void startWebSocketServer() throws Exception {

        final RMIWebsocketServerProxy s = new RMIWebsocketServerProxy(8887, 52369);
        s.start();
    }

    public static void main(String[] args)
    {
        try
        {
            startWebSocketServer();

            final TemperatureDispatcherImpl lServer = new TemperatureDispatcherImpl();
            // Binding the remote object (stub) in the registry
            final Registry reg = LocateRegistry.createRegistry(52369);

            rebindByUrl(lServer);

            //Create the thread and change the temperature
            final Thread lThread = new Thread(lServer);
            lThread.start();

        }
        catch (Exception aInE)
        {
            log.error("Server error", aInE);
        }
    }
}
