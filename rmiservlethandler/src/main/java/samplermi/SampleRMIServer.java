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

package samplermi;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import sun.rmi.transport.proxy.RMIHttpToCGISocketFactory;
import sun.rmi.transport.proxy.RMIHttpToPortSocketFactory;

import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.server.*;

import static java.lang.String.format;

/**
 * Remote object to receive calls forwarded from the ServletHandler.
 */
public class SampleRMIServer extends java.rmi.server.UnicastRemoteObject implements SampleRMI {

    public SampleRMIServer() throws RemoteException {
        super();
    }

    public String justPass(String passed) throws RemoteException {
        System.out.printf("justPass( '%s' )\n", passed );
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

    static Registry reg ;

    public static void main(String args[]) {

        final RMIServerSocketFactory serverSocketFactory = new RMIHttpToCGISocketFactory();
        final RMIClientSocketFactory clientSocketFactory = null;

        System.out.printf( format( "java.security.policy=[%s]\n", System.getProperty("java.security.policy")));
        try {
            System.setSecurityManager(new SecurityManager());

            // create a registry if one is not running already.
            try {
                reg = java.rmi.registry.LocateRegistry.createRegistry(1099, clientSocketFactory, serverSocketFactory);

                reg.bind( "SampleRMI", new SampleRMIServer() );

            } catch (java.rmi.server.ExportException ee) {
                // registry already exists, we'll just use it.
            } catch (RemoteException re) {
                System.err.println(re.getMessage());
                re.printStackTrace();
            }

            //Naming.rebind("/SampleRMI", new SampleRMIServer());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
