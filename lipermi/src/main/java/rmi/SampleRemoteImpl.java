package rmi;

import lombok.extern.slf4j.Slf4j;
import net.sf.lipermi.rmi.UnicastRemoteObject;

import java.rmi.RemoteException;

import static java.lang.String.format;

/**
 * Remote object to receive calls forwarded from the ServletHandler.
 */
@Slf4j
public class SampleRemoteImpl extends UnicastRemoteObject implements SampleRemote {

    public SampleRemoteImpl() throws RemoteException {
        super();
    }

    public String justPass(String passed) throws RemoteException {
        log.info( "justPass( '{}' )", passed );
        return format( "string passed to remote server is [%s]", passed) ;
    }

    @Override
    public String getInfo() throws RemoteException {
        return "I'm a RMI server";
    }

}
