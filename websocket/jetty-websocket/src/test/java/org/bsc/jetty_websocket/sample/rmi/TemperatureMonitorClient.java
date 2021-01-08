package org.bsc.jetty_websocket.sample.rmi;


import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.jetty_websocket.RMIClientWebsocketFactory;
import org.bsc.rmi.sample.TemperatureDispatcher;
import org.bsc.rmi.sample.TemperatureMonitor;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMISocketFactory;


@Slf4j
public class TemperatureMonitorClient
{
    enum RMIClientSocketFactoryEnum {

        DEFAULT( RMISocketFactory.getDefaultSocketFactory() ),
        WEBSOCKET( new RMIClientWebsocketFactory());

        RMIClientSocketFactory factory;

        RMIClientSocketFactoryEnum( RMIClientSocketFactory factory ) {
            this.factory = factory;
        }
    }

    public static TemperatureDispatcher lookupByUrl(int port, RMIClientSocketFactoryEnum s ) throws Exception {
        final String host = InetAddress.getLocalHost().getHostAddress();

        //final String url = format("rmi:/%s:%s/Hello", host, port );
        //final Remote lRemote = Naming.lookup(url);

        final Registry reg = java.rmi.registry.LocateRegistry.getRegistry(host, port, s.factory);

        final Remote lRemote = reg.lookup("Hello");

        return (TemperatureDispatcher) lRemote;
    }

    public static void main(String[] args)
    {
        try {
            // Lookup for the service
            //final TemperatureServer lRemoteDispatcher = lookupByUrl(52369, RMIClientSocketFactoryEnum.DEFAULT);
            final TemperatureDispatcher lRemoteDispatcher =
                    lookupByUrl(8887, RMIClientSocketFactoryEnum.WEBSOCKET);

            // Display the current temperature
            log.info("Origin Temperature {}", lRemoteDispatcher.getTemperature());

            // Create a temperature monitor and register it as a Listener
            final TemperatureMonitor lTemperatureMonitor = new TemperatureMonitor();
            lRemoteDispatcher.addTemperatureListener(lTemperatureMonitor);

        }
        catch (Exception aInE) {
            log.error( "Client invocation error", aInE);
        }
    }

}
