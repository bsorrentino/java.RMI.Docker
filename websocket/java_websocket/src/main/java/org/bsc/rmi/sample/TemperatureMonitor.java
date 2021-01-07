package org.bsc.rmi.sample;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

@Slf4j
public class TemperatureMonitor extends UnicastRemoteObject implements TemperatureListener
{

    protected TemperatureMonitor() throws RemoteException {
    }


    public static void main(String[] args)
    {
        try {
            // Lookup for the service
            final String url = "rmi://" + InetAddress.getLocalHost().getHostAddress() + ":52369/Hello";
            final Remote lRemote = Naming.lookup(url);
            final TemperatureServer lRemoteServer = (TemperatureServer) lRemote;

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
