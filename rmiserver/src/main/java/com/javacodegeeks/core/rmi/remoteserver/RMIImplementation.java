package com.javacodegeeks.core.rmi.remoteserver;

import java.rmi.RemoteException;
import java.rmi.UnexpectedException;
import java.rmi.server.UnicastRemoteObject;

import com.javacodegeeks.core.rmi.rminterface.RemoteInterface;

public class RMIImplementation extends UnicastRemoteObject implements RemoteInterface{

	
	protected RMIImplementation() throws RemoteException {
		super();
	}

	private static final long serialVersionUID = 1L;

	@Override
	public String capitalize(String str) throws RemoteException {
		return str.toUpperCase();
	}

}
