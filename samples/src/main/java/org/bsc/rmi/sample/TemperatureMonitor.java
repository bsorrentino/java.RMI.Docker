package org.bsc.rmi.sample;

import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


@Slf4j
public class TemperatureMonitor extends UnicastRemoteObject implements TemperatureListener
{

    public TemperatureMonitor() throws RemoteException {}

    @Override
    public void temperatureChanged(double temperature) throws RemoteException {
        log.info("Temperature change event {}", temperature);
    }
}
