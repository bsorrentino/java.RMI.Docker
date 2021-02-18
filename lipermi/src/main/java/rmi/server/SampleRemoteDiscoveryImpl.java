package rmi.server;

import lombok.extern.slf4j.Slf4j;
import rmi.SampleRemote;
import rmi.SampleRemoteDiscovery;

@Slf4j
public class SampleRemoteDiscoveryImpl implements SampleRemoteDiscovery  {

  @Override
  public SampleRemote lookup(String name) throws Exception {
    log.trace( "SampleRemoteDiscoveryImpl.lookup({})", name);

    return new SampleRemoteImpl();
  }
}
