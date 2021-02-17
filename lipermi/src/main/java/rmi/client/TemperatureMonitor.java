package rmi.client;

import lombok.extern.slf4j.Slf4j;
import rmi.TemperatureListener;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


@Slf4j
public class TemperatureMonitor extends UnicastRemoteObject implements TemperatureListener
{

    public TemperatureMonitor() throws RemoteException {}

    public TemperatureMonitor( int port ) throws RemoteException {
        super( port );
    }

    @Override
    public void temperatureChanged(double temperature) throws RemoteException {
        log.info("Temperature change event {}", temperature);
    }
}
