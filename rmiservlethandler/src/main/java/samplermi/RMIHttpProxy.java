package samplermi;

import org.bsc.rmi.servlet.RMIServletHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class RMIHttpProxy {
    private static Server server;

    public static void main(String args[]) throws Exception {

            int maxThreads = 10;
            int minThreads = 1;
            int idleTimeout = 120;

            final QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);

            server = new Server(threadPool);
            final ServerConnector connector = new ServerConnector(server);
            connector.setPort(80);
            server.setConnectors(new Connector[] { connector });

            final ServletHandler servletHandler = new ServletHandler();
            server.setHandler(servletHandler);

            servletHandler.addServletWithMapping(RMIServletHandler.class, "/*");

            server.start();

            System.out.println( "jetty started!");

    }

}
