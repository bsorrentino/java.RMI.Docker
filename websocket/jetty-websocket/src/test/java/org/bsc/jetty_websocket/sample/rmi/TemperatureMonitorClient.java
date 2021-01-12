package org.bsc.jetty_websocket.sample.rmi;


import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.jetty_websocket.client.RMIClientWebSocketFactory;
import org.bsc.rmi.jetty_websocket.client.RMIEventHandlerWebSocketFactory;
import org.bsc.rmi.jetty_websocket.client.RMIWebSocketEventHandlerProxy;
import org.bsc.rmi.jetty_websocket.client.RMIWebSocketFactoryClient;
import org.bsc.rmi.sample.TemperatureDispatcher;
import org.bsc.rmi.sample.TemperatureMonitor;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;


@Slf4j
public class TemperatureMonitorClient implements Constants
{
    /**
     *
     * @param rmi_port
     * @return
     * @throws Exception
     */
    public static TemperatureDispatcher lookupByUrl(@NonNull  String host, int rmi_port /*, RMIClientSocketFactoryEnum s */ ) throws Exception {

        //final String url = format("rmi:/%s:%s/Hello", host, port );
        //final Remote lRemote = Naming.lookup(url);
        final Registry reg = java.rmi.registry.LocateRegistry.getRegistry(host, rmi_port /*, s.factory*/);

        final Remote lRemote = reg.lookup("Hello");

        return (TemperatureDispatcher) lRemote;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args)
    {
        try {
            final String host = (args.length < 1) ? InetAddress.getLocalHost().getHostAddress() : args[0];

            final RMIClientWebSocketFactory wsClient =
                new RMIClientWebSocketFactory(host, WEBSOCKET_PORT);

            final RMIWebSocketEventHandlerProxy eventHandlerProxy =
                    new RMIWebSocketEventHandlerProxy(host, WEBSOCKET_PORT, RMI_EVENT_PORT);

//            final RMIEventHandlerWebsocketFactory wsEventHandler =
//                    new RMIEventHandlerWebsocketFactory( host, WEBSOCKET_PORT, RMI_EVENT_PORT);
            
            final RMISocketFactory factory =
                RMIWebSocketFactoryClient.builder()
                        .clientSocketFactory( wsClient )
                        .serverSocketFactory( new RMIEventHandlerWebSocketFactory(eventHandlerProxy) )
                        .debug( true )
                        .build();

            RMISocketFactory.setSocketFactory( factory );

            // Lookup for the service
            final TemperatureDispatcher lRemoteDispatcher = lookupByUrl(host, RMI_PORT);

            // Display the current temperature
            log.info("Origin Temperature {}", lRemoteDispatcher.getTemperature());

            // Create a temperature monitor and register it as a Listener
            //final TemperatureMonitor lTemperatureMonitor = new TemperatureMonitor();
            final TemperatureMonitor lTemperatureMonitor = new TemperatureMonitor(RMI_EVENT_PORT);
            lRemoteDispatcher.addTemperatureListener(lTemperatureMonitor);

        }
        catch (Exception aInE) {
            log.error( "Client invocation error", aInE);
        }
    }

}
