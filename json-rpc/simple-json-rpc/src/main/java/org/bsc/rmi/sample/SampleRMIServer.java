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

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcParam;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcService;
import com.github.arteam.simplejsonrpc.server.JsonRpcServer;
import com.google.common.io.CharStreams;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.io.IOException;

import static java.lang.String.format;

/**
 * Remote object to receive calls forwarded from the ServletHandler.
 */
@Log
public class SampleRMIServer implements SampleRMI {

    public SampleRMIServer() {
    }

    @Override
    public String justPass( String passed) {
        log.info( format( "justPass( '%s' )", passed ));
        return format( "string passed to remote server is [%s]", passed) ;
    }

    @Override
    public String getInfo() {
        return "I'm a RMI server";
    }


    /**
     *
     * @param args
     */
    public static void main(String args[]) throws Exception {

        int maxThreads = 10;
        int minThreads = 1;
        int idleTimeout = 120;

        final QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);

        final Server httpServer = new Server(threadPool);
        final ServerConnector connector = new ServerConnector(httpServer);

        connector.setPort(80);

        httpServer.setConnectors(new Connector[]{connector});

        final SampleRMI service = new SampleRMIServer();

        final JsonRpcServer rpcServer = new JsonRpcServer();

        httpServer.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

                log.info( format("\n>\ntarget:%s\nrequestURI:%s\n>",
                        target,
                        baseRequest.getRequestURI())
                );

                final String body = CharStreams.toString(baseRequest.getReader());

                log.info( format(">\n%s\n<", body ));

                final String result = rpcServer.handle( body, service);

                res.getWriter().print(result);
                res.getWriter().flush();
                baseRequest.setHandled(true);
            }
        });

//        final ServletHandler servletHandler = new ServletHandler();
//
//        server.setHandler(servletHandler);
//        final ServletHolder service = servletHandler.addServletWithMapping(RMIServletHandler.class, "/*");
//
//        service.setInitParameter("rmiservlethandler.initialServerCodebase", "");
//        service.setInitParameter("rmiservlethandler.initialServerClass", "");
//        service.setInitParameter("rmiservlethandler.initialServerBindName", "");
//        service.setInitParameter("rmiservlethandler.remoteHost", "");

        httpServer.start();

        log.info("jetty started!");

        httpServer.join();

    }
}
