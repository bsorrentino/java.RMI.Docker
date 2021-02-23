package rmi.server;

import lombok.extern.slf4j.Slf4j;
import net.sf.lipermi.rmi.LocateRegistry;
import rmi.SampleRemote;
import rmi.SampleRemoteDiscovery;

@Slf4j
public class SampleRemoteDiscoveryImpl implements SampleRemoteDiscovery  {

  @Override
  public SampleRemote lookup(String name) throws Exception {
    log.trace( "SampleRemoteDiscoveryImpl.lookup({})", name);

    return LocateRegistry.getCurrentRegistry()
        .orElseThrow( () -> new IllegalStateException("SampleRemote is not an exported object") )
        .lookup(SampleRemote.class);
  }

}
