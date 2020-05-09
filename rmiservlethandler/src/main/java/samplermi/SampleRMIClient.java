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

import java.rmi.*;
import java.rmi.server.*;

/**
 * RMI client to invoke calls through the ServletHandler
 */
public class SampleRMIClient { 
    public static void main(String args[]) {
	try {
	    if (args.length != 1 ) {
		System.out.println("Usage: <hostname>");
		System.exit(1);
	    }
	    

	    /*
	     * NOTICE: To make this example easier to set-up and run,
	     * the following call causes RMI to use a socket factory
	     * that is only capable of invoking remote methods over
	     * HTTP to a CGI script (or servlet).  This client
	     * simulates the behavior that an RMI client would have if
	     * it were forced to invoke remote calls on a server that
	     * resided outside a local firewall.
	     *
	     * It is not recommended that you make use of this sun
	     * implementation class (or any sun.* class) in general-
	     * purpose applications for the following reasons:
	     *
	     *   - Sun Microsystem's does not support the use of 
             *     sun.* classes.
	     *   - All sun.* classes are specific to Sun Microsystem's
	     *     implementation of the Java Development Kit.
	     *       
	     * To fully test the example, you will need to comment out
	     * the following line of code, ensure that the client and
	     * server are on opposite sides of a firewall and set the
	     * client VM's proxy host properties as follows:
	     *
	     *   java -Dhttp.proxyHost=<proxyHost> -Dhttp.proxyPort=<proxyPort> 
	     *       samplermi.SampleRMIClient <servletHostname> 
	     */
	    RMISocketFactory.setSocketFactory( new sun.rmi.transport.proxy.RMIHttpToCGISocketFactory() );
	    

	    System.setSecurityManager(new RMISecurityManager());
	    SampleRMI sampleRMI = (SampleRMI) Naming.
		lookup("rmi://" + args[0] + "/SampleRMI");

	    // Invoke a single remote call to test the servlet.
	    System.out.println(sampleRMI.justPass("This is a test of the RMI " + 
						  "servlet handler"));
	    System.out.println("Servlet installed correctly.");
	    System.exit(0);
	    
	} catch(Exception e) {
	    System.err.println("Error: "+e.getMessage());
	    e.printStackTrace();
	}
    }
}
