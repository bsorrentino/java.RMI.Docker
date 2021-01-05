package org.bsc.rmi.proxy.http;

import lombok.extern.java.Log;
import org.bsc.rmi.servlet.RMIServletHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

@Log
public class RMIHttpProxy {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "WARN");

        int maxThreads = 10;
        int minThreads = 1;
        int idleTimeout = 120;

        final QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);

        final Server server = new Server(threadPool);
        final ServerConnector connector = new ServerConnector(server);
        connector.setPort(80);
        server.setConnectors(new Connector[]{connector});

        final ServletHandler servletHandler = new ServletHandler();

        server.setHandler(servletHandler);
        final ServletHolder service = servletHandler.addServletWithMapping(RMIServletHandler.class, "/*");

//        service.setInitParameter("rmiservlethandler.initialServerCodebase", "");
//        service.setInitParameter("rmiservlethandler.initialServerClass", "");
//        service.setInitParameter("rmiservlethandler.initialServerBindName", "");
//        service.setInitParameter("rmiservlethandler.remoteHost", "");

        server.start();

        log.info("jetty started!");

        server.join();

    }

}
