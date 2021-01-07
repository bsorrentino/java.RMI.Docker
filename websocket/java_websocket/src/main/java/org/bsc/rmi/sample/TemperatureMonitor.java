package org.bsc.rmi.sample;

import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.websocket.RMIClientWebsocketFactory;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;


@Slf4j
public class TemperatureMonitor extends UnicastRemoteObject implements TemperatureListener
{

    protected TemperatureMonitor() throws RemoteException {
    }

    enum RMIClientSocketFactoryEnum {

        DEFAULT( RMISocketFactory.getDefaultSocketFactory() ),
        WEBSOCKET( new RMIClientWebsocketFactory());

        RMIClientSocketFactory factory;

        RMIClientSocketFactoryEnum( RMIClientSocketFactory factory ) {
            this.factory = factory;
        }
    }

    public static TemperatureServer lookupByUrl(int port, RMIClientSocketFactoryEnum s ) throws Exception {
        final String host = InetAddress.getLocalHost().getHostAddress();

        //final String url = format("rmi:/%s:%s/Hello", host, port );
        //final Remote lRemote = Naming.lookup(url);

        final Registry reg = java.rmi.registry.LocateRegistry.getRegistry(host, port, s.factory);

        final Remote lRemote = reg.lookup("Hello");

        return (TemperatureServer) lRemote;
    }

    public static void main(String[] args)
    {
        try {
            // Lookup for the service
            //final TemperatureServer lRemoteServer = lookupByUrl(52369, RMIClientSocketFactoryEnum.DEFAULT);
            final TemperatureServer lRemoteServer =
                    lookupByUrl(8887, RMIClientSocketFactoryEnum.WEBSOCKET);

            // Display the current temperature
            log.info("Origin Temperature {}", lRemoteServer.getTemperature());

            // Create a temperature monitor and register it as a Listener
            final TemperatureMonitor lTemperatureMonitor = new TemperatureMonitor();
            lRemoteServer.addTemperatureListener(lTemperatureMonitor);

        }
        catch (Exception aInE) {
            log.error( "Client invocation error", aInE);
        }
    }

    @Override
    public void temperatureChanged(double temperature) throws RemoteException {
        log.info("Temperature change event {}", temperature);
    }
}
