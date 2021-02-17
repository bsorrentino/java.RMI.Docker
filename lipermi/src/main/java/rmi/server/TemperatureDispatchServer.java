package rmi.server;


import lombok.extern.slf4j.Slf4j;
import net.sf.lipermi.rmi.LocateRegistry;
import net.sf.lipermi.rmi.Registry;
import rmi.Constants;
import rmi.TemperatureDispatcher;

import java.net.InetAddress;

@Slf4j
public class TemperatureDispatchServer implements Constants
{

    public static void main(String[] args)
    {
        try
        {
            final String host = InetAddress.getLocalHost().getHostAddress();

            log.info( "Server started on host:{}", host);

            final TemperatureDispatcherImpl lServer = new TemperatureDispatcherImpl(RMI_PORT);
            // Binding the remote object (stub) in the registry
            final Registry reg = LocateRegistry.createRegistry(RMI_PORT);

            reg.rebind(TemperatureDispatcher.class,lServer);

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
