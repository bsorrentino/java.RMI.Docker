package rmi.server;


import lombok.extern.slf4j.Slf4j;
import net.sf.lipermi.rmi.LocateRegistry;
import net.sf.lipermi.rmi.Registry;
import rmi.Constants;
import rmi.SampleRemote;
import rmi.SampleRemoteDiscovery;
import rmi.TemperatureDispatcher;

import java.net.InetAddress;

@Slf4j
public class LipermiSampleServer implements Constants
{
    final TemperatureDispatcherImpl lServer;
    final SampleRemoteImpl sample;
    final SampleRemoteDiscoveryImpl discovery;

    public LipermiSampleServer() throws Exception  {
        this.lServer =  new TemperatureDispatcherImpl();
        this.sample = new SampleRemoteImpl();
        this.discovery = new SampleRemoteDiscoveryImpl();
    }

    void registerDispatcher( Registry reg ) throws Exception {

        log.trace( "registerDispatcher");

        reg.rebind(TemperatureDispatcher.class,lServer);

        //Create the thread and change the temperature
        final Thread lThread = new Thread(lServer);
        lThread.start();

    }

    void registerSampleRemote( Registry reg ) throws Exception {
        log.trace( "registerSampleRemote");
        reg.bind( SampleRemote.class, sample );
    }

    void registerSampleDiscovery( Registry reg ) throws Exception {
        log.trace( "registerSampleDiscovery");
        reg.bind( SampleRemoteDiscovery.class, discovery );
    }

    public static void main(String[] args)
    {
        try
        {
            final LipermiSampleServer app = new LipermiSampleServer();

            final String host = InetAddress.getLocalHost().getHostAddress();

            log.info( "Server started on host:{}", host);

            // Binding the remote object (stub) in the registry
            final Registry reg = LocateRegistry.createRegistry(RMI_PORT);

            app.registerSampleDiscovery(reg);

            app.registerSampleRemote(reg);

            //app.registerDispatcher(reg);

        }
        catch (Exception aInE)
        {
            log.error("Server error", aInE);
        }
    }
}
