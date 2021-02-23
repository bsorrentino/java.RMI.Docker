package rmi.client;

import lombok.extern.slf4j.Slf4j;
import rmi.SampleRemote;
import rmi.TemperatureListener;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Optional;

import static java.util.Optional.ofNullable;


@Slf4j
public class TemperatureMonitor extends UnicastRemoteObject implements TemperatureListener
{
    private transient SampleRemote remote;

    public Optional<SampleRemote> getRemote() {
        return ofNullable(remote);
    }

    public void setRemote(SampleRemote remote) {
        this.remote = remote;
    }

    public TemperatureMonitor(int port ) throws RemoteException {
        super( port );
    }

    @Override
    public void temperatureChanged(double temperature) throws RemoteException {
        log.info("Temperature change event {}", temperature);

        getRemote().ifPresent( v -> {
            try {
                v.justPass(String.valueOf(temperature));
            } catch (RemoteException e) {
                log.warn( "error invoking justPass()", e);
            }
        });
    }
}
