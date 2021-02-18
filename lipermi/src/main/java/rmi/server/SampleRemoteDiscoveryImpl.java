package rmi.server;

import lombok.extern.slf4j.Slf4j;
import net.sf.lipermi.rmi.UnicastRemoteObject;
import rmi.SampleRemote;
import rmi.SampleRemoteDiscovery;

@Slf4j
public class SampleRemoteDiscoveryImpl implements SampleRemoteDiscovery  {

  @Override
  public SampleRemote lookup(String name) throws Exception {
    log.trace( "SampleRemoteDiscoveryImpl.lookup({})", name);

    return UnicastRemoteObject.getExportedObject(SampleRemote.class)
            .orElseThrow( () -> new IllegalStateException("SampleRemote is not an exported object"));

  }
}
