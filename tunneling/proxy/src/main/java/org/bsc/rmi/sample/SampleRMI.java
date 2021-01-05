package org.bsc.rmi.sample;

/**
 * Remote interface that will help test the ServletHandler servlet.
 * This interface will be implemented by the SampleRMIServer.  
 */
public interface SampleRMI extends java.rmi.Remote {
    /**
     * Test remote method.
     */
    String justPass(String toPass) throws java.rmi.RemoteException;

    String getInfo() throws java.rmi.RemoteException;
}

