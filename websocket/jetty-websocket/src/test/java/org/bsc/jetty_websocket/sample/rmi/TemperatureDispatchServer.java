package org.bsc.jetty_websocket.sample.rmi;


import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.jetty_websocket.server.RMIEventDispatcherWebSocketFactory;
import org.bsc.rmi.jetty_websocket.server.RMIWebSocketFactoryServer;
import org.bsc.rmi.jetty_websocket.server.RMIWebSocketServerProxy;
import org.bsc.rmi.sample.TemperatureDispatcherImpl;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;

@Slf4j
public class TemperatureDispatchServer implements Constants
{

    public static void main(String[] args)
    {
        try
        {
            final RMIWebSocketServerProxy wsServer = new RMIWebSocketServerProxy(WEBSOCKET_PORT);

            final RMIEventDispatcherWebSocketFactory wsEventDispatcher =
                    new RMIEventDispatcherWebSocketFactory(wsServer.eventDispatcherlistener);

            wsServer.start();

            final String host = InetAddress.getLocalHost().getHostAddress();

            log.info( "Server started on host:{}", host);

            final RMISocketFactory factory =
                RMIWebSocketFactoryServer.builder()
                        .clientSocketFactory(wsEventDispatcher)
                        .debug(true)
                        .build();

            RMISocketFactory.setSocketFactory( factory );

            final TemperatureDispatcherImpl lServer = new TemperatureDispatcherImpl(RMI_PORT);
            // Binding the remote object (stub) in the registry
            final Registry reg = LocateRegistry.createRegistry(RMI_PORT);

            reg.rebind("Hello",lServer);

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
