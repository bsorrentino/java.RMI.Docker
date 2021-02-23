package rmi.server;

import lombok.extern.slf4j.Slf4j;
import net.sf.lipermi.rmi.UnicastRemoteObject;
import rmi.SampleRemote;

import java.rmi.RemoteException;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.empty;

/**
 * Remote object to receive calls forwarded from the ServletHandler.
 */
@Slf4j
public class SampleRemoteImpl extends UnicastRemoteObject implements SampleRemote {

    Optional<String> notSerializableAttribute = empty();

    public SampleRemoteImpl() throws RemoteException {
        super();
    }

    public String justPass(String passed) throws RemoteException {
        log.info( "justPass( '{}' )", passed );
        return format( "string passed to remote server is [%s]", passed) ;
    }

    @Override
    public String getInfo() throws RemoteException {
        log.info( "getInfo()" );
        return "I'm a RMI server";
    }

}
