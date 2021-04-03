package rmi.client;


import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.sf.lipermi.rmi.LocateRegistry;
import net.sf.lipermi.rmi.RMISocketRegistryImpl;
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

    final Registry reg;

    public LipermiSampleClient( @NonNull  Registry reg ) throws Exception {
        this.reg = reg;
        temperatureDispatcher = reg.lookup(TemperatureDispatcher.class);
        remoteDiscovery = reg.lookup( SampleRemoteDiscovery.class );
    }

    public void lookupTemperatureDispatcher(SampleRemote remote) throws Exception {

        // Display the current temperature
        log.info("Origin Temperature {}", temperatureDispatcher.getTemperature());

        final TemperatureMonitor lTemperatureMonitor = new TemperatureMonitor(RMI_EVENT_PORT);
        reg.bind(TemperatureListener.class,lTemperatureMonitor);

        lTemperatureMonitor.setRemote(remote);

        temperatureDispatcher.addTemperatureListener( lTemperatureMonitor );
    }

    public SampleRemote lookupDiscovery() throws Exception {

        final SampleRemote remote = remoteDiscovery.lookup( "test");

        final String info = remote.getInfo();

        log.info( "get info: {}", info );

        return remote;

    }

    /**
     *
     * @param args
     */
    public static void main(String[] args)
    {
        LocateRegistry.registerRegistryProvider( RMISocketRegistryImpl::new );

        try {
            final String host = (args.length < 1 || args[0].isEmpty()) ? InetAddress.getLocalHost().getHostAddress() : args[0];

            log.info( "remote host: {}", host);

            final Registry reg = LocateRegistry.getRegistry(host, RMI_PORT /*, s.factory*/);

            final LipermiSampleClient client = new LipermiSampleClient(reg);

            client.lookupTemperatureDispatcher( client.lookupDiscovery() );


        }
        catch (Exception aInE) {
            log.error( "Client invocation error", aInE);
        }
    }

}
