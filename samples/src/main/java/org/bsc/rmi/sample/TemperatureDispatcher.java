package org.bsc.rmi.sample;

import java.rmi.Remote;
import java.rmi.RemoteException;

interface TemperatureDispatcher extends Remote
{

    void addTemperatureListener(TemperatureListener addTemperatureListener) throws RemoteException;

    void removeTemperatureListener(TemperatureListener addTemperatureListener) throws RemoteException;

    Double getTemperature() throws RemoteException;
}
