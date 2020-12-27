/*
 * Copyright 2002 Sun Microsystems, Inc. All  Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * -Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES  SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
 * THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */

package org.bsc.rmi.sample;

import lombok.extern.java.Log;
import org.bsc.rmi.proxy.http.server.RMIHttpServerSocketFactory;

import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;

/**
 * Remote object to receive calls forwarded from the ServletHandler.
 */
@Log
public class SampleRMIServer extends java.rmi.server.UnicastRemoteObject implements SampleRMI {

    public SampleRMIServer() throws RemoteException {
        super();
    }

    public String justPass(String passed) throws RemoteException {
        log.info( format( "justPass( '%s' )", passed ));
        return format( "string passed to remote server is [%s]", passed) ;
    }

    @Override
    public String getInfo() throws RemoteException {
        return "I'm a RMI server";
    }

    /**
     * You should not need to run this server from the command line.
     * The ServletHandler class creates its own instance of the
     * rmiregistry and (optionally) an instance of this class as well.
     * This main method will not be executed from the ServletHandler.
     */


    private static CompletableFuture<Void> bind( Registry reg ) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        try {
            reg.bind( "SampleRMI", new SampleRMIServer() );
            result.complete( null );
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    private static CompletableFuture<Registry> createRMIRegistry( int port ) {
        CompletableFuture<Registry> result = new CompletableFuture<>();

        try {
            final Registry reg = java.rmi.registry.LocateRegistry.createRegistry(port );

            result.complete(reg);

        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    private static CompletableFuture<Registry> createCustomRMIRegistry( int port ) {

        final RMIServerSocketFactory serverSocketFactory = new RMIHttpServerSocketFactory();
        final RMIClientSocketFactory clientSocketFactory = null;

        CompletableFuture<Registry> result = new CompletableFuture<>();

        try {
            final Registry reg = java.rmi.registry.LocateRegistry.createRegistry(port, clientSocketFactory, serverSocketFactory );

            result.complete(reg);

        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }


    /**
     *
     * @param args
     */
    public static void main(String args[]) {

        log.info( format( "java.security.policy=[%s]", System.getProperty("java.security.policy")));

        createCustomRMIRegistry(1099)
        //createRMIRegistry( 1099 )
            .thenCompose( SampleRMIServer::bind )
            .exceptionally( ex -> {
                log.throwing(SampleRMIServer.class.getName(), "main", ex);
                return null;
            })
            .join();
    }
}
