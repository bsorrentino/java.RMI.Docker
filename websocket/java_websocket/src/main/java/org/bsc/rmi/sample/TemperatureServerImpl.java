package org.bsc.rmi.sample;

import lombok.extern.slf4j.Slf4j;
import org.bsc.rmi.websocket.RMIWebsocketServerProxy;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class TemperatureServerImpl extends UnicastRemoteObject implements TemperatureServer, Runnable
{

    private List<TemperatureListener> listeners = new ArrayList<>();
    private volatile double temp;

    protected TemperatureServerImpl() throws RemoteException
    {
        //Default temperature
        temp = 88.00;
    }

    @Override
    public void run()
    {
        Random lRandom = new Random();
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
            catch (RemoteException aInE)
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

    public static void rebindByUrl(Remote obj ) throws Exception {
        final String url = "rmi://" + InetAddress.getLocalHost().getHostAddress() + ":52369/Hello";
        Naming.rebind(url, obj);

    }

    private static void startWebSocketServer() {

        final RMIWebsocketServerProxy s = new RMIWebsocketServerProxy(8887, 52369);
        s.start();
    }

    public static void main(String[] args)
    {
        try
        {
            startWebSocketServer();

            final TemperatureServerImpl lServer = new TemperatureServerImpl();
            // Binding the remote object (stub) in the registry
            final Registry reg = LocateRegistry.createRegistry(52369);

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
