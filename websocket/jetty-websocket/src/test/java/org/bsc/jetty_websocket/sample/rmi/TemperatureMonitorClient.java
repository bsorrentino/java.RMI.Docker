package org.bsc.jetty_websocket.sample.rmi;


import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.jetty_websocket.client.RMIClientWebsocketFactory;
import org.bsc.rmi.jetty_websocket.client.RMIWebsocketFactoryClient;
import org.bsc.rmi.jetty_websocket.server.RMIWebsocketFactoryServer;
import org.bsc.rmi.jetty_websocket.server.RMIWebsocketServerProxy;
import org.bsc.rmi.proxy.socket.debug.RMIDebugClientSocketFactory;
import org.bsc.rmi.proxy.socket.debug.RMIDebugSocketFactory;
import org.bsc.rmi.sample.TemperatureDispatcher;
import org.bsc.rmi.sample.TemperatureMonitor;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;


@Slf4j
public class TemperatureMonitorClient implements Constants
{

    enum RMIClientSocketFactoryEnum {

        DEFAULT( RMISocketFactory.getDefaultSocketFactory() ),
        DEBUG( new RMIDebugClientSocketFactory() ),
        WEBSOCKET( new RMIClientWebsocketFactory(WEBSOCKET_PORT))
        ;

        RMIClientSocketFactory factory;

        RMIClientSocketFactoryEnum( RMIClientSocketFactory factory ) {
            this.factory = factory;
        }
    }

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
     * @throws Exception
     */
    public static RMIClientSocketFactoryEnum setupWebsocket() throws Exception {

        final RMISocketFactory factory =
            RMIWebsocketFactoryClient.builder()
                .clientSocketFactory( new RMIClientWebsocketFactory(WEBSOCKET_PORT) )
                .build();

        RMISocketFactory.setSocketFactory( factory );

        return RMIClientSocketFactoryEnum.WEBSOCKET;
    }

    /**
     *
     * @throws Exception
     */
    public static RMIClientSocketFactoryEnum setupDebuggingRMI() throws Exception {
        final RMISocketFactory factory =
            RMIWebsocketFactoryClient.builder()
                .debug( true )
                .build();

        RMISocketFactory.setSocketFactory( factory );

        return RMIClientSocketFactoryEnum.DEBUG;

    }

    /**
     *
     * @param args
     */
    public static void main(String[] args)
    {
        try {

            final String host = InetAddress.getLocalHost().getHostAddress();

            final RMIClientWebsocketFactory websocket_client =
                new RMIClientWebsocketFactory(WEBSOCKET_PORT);

            
            final RMISocketFactory factory =
                RMIWebsocketFactoryClient.builder()
                    .clientSocketFactory( websocket_client )
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
