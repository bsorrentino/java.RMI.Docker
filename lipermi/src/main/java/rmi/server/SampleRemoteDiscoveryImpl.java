package rmi.server;

import rmi.SampleRemote;
import rmi.SampleRemoteDiscovery;

public class SampleRemoteDiscoveryImpl implements SampleRemoteDiscovery  {

  @Override
  public SampleRemote lookup(String name) throws Exception {
    return new SampleRemoteImpl();
  }
}
