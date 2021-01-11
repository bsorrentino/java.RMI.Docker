package org.bsc.rmi.sample;

import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class TemperatureDispatcherImpl extends UnicastRemoteObject implements TemperatureDispatcher, Runnable
{

    private List<TemperatureListener> listeners = new ArrayList<>();
    private volatile double temp = 88.00;

    public TemperatureDispatcherImpl() throws RemoteException { }
    public TemperatureDispatcherImpl( int port ) throws RemoteException  {
        super( port );
    }

    @Override
    public void run()
    {
        final Random lRandom = new Random();
        while (true)
        {
            int duration = lRandom.nextInt() % 10000 + 2000;
            if (duration < 0)
            {
                duration = duration * -1;
            }
            try
            {
                Thread.sleep(duration);
            }
            catch (InterruptedException aInE)
            {
                log.error(aInE.getMessage());
            }
            //Take a number to see if up or down
            int num = lRandom.nextInt();

            if (num < 0)
            {
                temp -= 0.5;
            }
            else
            {
                temp += 0.5;
            }
            notifyTemperatureListeners(temp);
        }
    }

    private void notifyTemperatureListeners(double aInTemp)
    {
        for (TemperatureListener lListener : listeners)
        {
            try
            {
                lListener.temperatureChanged(aInTemp);
            }
            catch (Exception aInE)
            {
                listeners.remove(lListener);
            }
        }
    }

    @Override
    public void addTemperatureListener(TemperatureListener temperatureListener) throws RemoteException
    {
        listeners.add(temperatureListener);
    }

    @Override
    public void removeTemperatureListener(TemperatureListener temperatureListener) throws RemoteException
    {
        listeners.remove(temperatureListener);
    }

    @Override
    public Double getTemperature() throws RemoteException
    {
        return temp;
    }

}
