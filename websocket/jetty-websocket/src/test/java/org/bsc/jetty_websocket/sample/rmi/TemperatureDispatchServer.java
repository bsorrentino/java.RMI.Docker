package org.bsc.jetty_websocket.sample.rmi;


import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.jetty_websocket.WebSocketProxyListener;
import org.bsc.rmi.jetty_websocket.server.RMIEventDispatcherWebsocketFactory;
import org.bsc.rmi.jetty_websocket.server.RMIWebsocketFactoryServer;
import org.bsc.rmi.jetty_websocket.server.RMIWebsocketServerProxy;
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
            final RMIWebsocketServerProxy wsServer = new RMIWebsocketServerProxy(WEBSOCKET_PORT);

            final RMIEventDispatcherWebsocketFactory wsEventDispatcher =
                    new RMIEventDispatcherWebsocketFactory(wsServer.eventDispatcherlistener);

            wsServer.start();

            final String host = InetAddress.getLocalHost().getHostAddress();

            final RMISocketFactory factory =
                RMIWebsocketFactoryServer.builder()
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
