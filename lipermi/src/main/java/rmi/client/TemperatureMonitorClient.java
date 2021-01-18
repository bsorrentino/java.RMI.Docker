package rmi.client;


import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.sf.lipermi.rmi.LocateRegistry;
import net.sf.lipermi.rmi.Registry;
import net.sf.lipermi.rmi.UnicastRemoteObject;
import rmi.Constants;
import rmi.TemperatureDispatcher;
import rmi.TemperatureListener;
import rmi.TemperatureMonitor;

import java.net.InetAddress;


@Slf4j
public class TemperatureMonitorClient implements Constants
{
    private static final int RMI_EVENT_PORT = 0;

    /**
     *
     * @param rmi_port
     * @return
     * @throws Exception
     */
    public static TemperatureDispatcher lookupByUrl(@NonNull  String host, int rmi_port /*, RMIClientSocketFactoryEnum s */ ) throws Exception {

        final Registry reg = LocateRegistry.getRegistry(host, rmi_port /*, s.factory*/);

        final TemperatureDispatcher lRemote = reg.lookup(TemperatureDispatcher.class);

        return lRemote;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args)
    {
        try {
            final String host = (args.length < 1 || args[0].isEmpty()) ? InetAddress.getLocalHost().getHostAddress() : args[0];

            log.info( "remote host: {}", host);

            // Lookup for the service
            final TemperatureDispatcher lRemoteDispatcher = lookupByUrl(host, RMI_PORT);

            // Display the current temperature
            log.info("Origin Temperature {}", lRemoteDispatcher.getTemperature());

            final TemperatureMonitor lTemperatureMonitor = new TemperatureMonitor(RMI_EVENT_PORT);
            UnicastRemoteObject.exportObject(TemperatureListener.class,lTemperatureMonitor);
            lRemoteDispatcher.addTemperatureListener( lTemperatureMonitor );

        }
        catch (Exception aInE) {
            log.error( "Client invocation error", aInE);
        }
    }

}
