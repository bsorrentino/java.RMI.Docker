package org.bsc.jetty_websocket.sample.rmi;


import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.jetty_websocket.server.RMIWebsocketFactoryServer;
import org.bsc.rmi.jetty_websocket.server.RMIWebsocketServerProxy;
import org.bsc.rmi.proxy.socket.debug.RMIDebugSocketFactory;
import org.bsc.rmi.sample.TemperatureDispatcherImpl;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;

import static java.lang.String.format;

@Slf4j
public class TemperatureDispatchServer implements Constants
{
    public static void rebindByUrl(Remote obj ) throws Exception {
        final String url = format( "rmi://%s:%d/Hello",InetAddress.getLocalHost().getHostAddress(), RMI_PORT);
        Naming.rebind(url, obj);
    }

    public static void main(String[] args)
    {
        try
        {
            final RMIWebsocketServerProxy wsserver = new RMIWebsocketServerProxy(WEBSOCKET_PORT);
            wsserver.start();

            RMISocketFactory.setSocketFactory( new RMIWebsocketFactoryServer(wsserver.eventDispatcherlistener) );

            final TemperatureDispatcherImpl lServer = new TemperatureDispatcherImpl();
            // Binding the remote object (stub) in the registry
            final Registry reg = LocateRegistry.createRegistry(RMI_PORT);

            rebindByUrl(lServer);

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
