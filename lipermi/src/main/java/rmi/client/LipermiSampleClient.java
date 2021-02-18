package rmi.client;


import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.sf.lipermi.rmi.LocateRegistry;
import net.sf.lipermi.rmi.Registry;
import net.sf.lipermi.rmi.UnicastRemoteObject;
import rmi.*;

import java.net.InetAddress;


@Slf4j
public class LipermiSampleClient implements Constants
{
    private static final int RMI_EVENT_PORT = 0;

    final  TemperatureDispatcher temperatureDispatcher;
    final SampleRemoteDiscovery remoteDiscovery;


    public LipermiSampleClient( @NonNull  Registry reg ) throws Exception {
        temperatureDispatcher = reg.lookup(TemperatureDispatcher.class);
        remoteDiscovery = reg.lookup( SampleRemoteDiscovery.class );
    }

    public void lookupTemperatureDispatcher() throws Exception {

        // Display the current temperature
        log.info("Origin Temperature {}", temperatureDispatcher.getTemperature());

        final TemperatureMonitor lTemperatureMonitor = new TemperatureMonitor(RMI_EVENT_PORT);
        UnicastRemoteObject.exportObject(TemperatureListener.class,lTemperatureMonitor);
        temperatureDispatcher.addTemperatureListener( lTemperatureMonitor );
    }

    public void lookupDiscovery() throws Exception {

        final SampleRemote remote = remoteDiscovery.lookup( "test");

        final String info = remote.getInfo();

        log.info( "get info: {}", info );


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

            final Registry reg = LocateRegistry.getRegistry(host, RMI_PORT /*, s.factory*/);

            final LipermiSampleClient client = new LipermiSampleClient(reg);

            client.lookupDiscovery();

            client.lookupTemperatureDispatcher();


        }
        catch (Exception aInE) {
            log.error( "Client invocation error", aInE);
        }
    }

}
