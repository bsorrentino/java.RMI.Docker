package rmi;

import java.rmi.Remote;

public interface SampleRemoteDiscovery extends Remote  {

  SampleRemote lookup( String name ) throws Exception;

}
