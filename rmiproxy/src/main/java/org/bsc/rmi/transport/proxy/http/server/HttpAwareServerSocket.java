package org.bsc.rmi.transport.proxy.http.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

import lombok.extern.java.Log;
import org.bsc.rmi.transport.proxy.http.HttpReceiveSocket;
import org.bsc.rmi.transport.proxy.socket.RMIMasterSocketFactory;
import org.bsc.rmi.transport.proxy.http.WrappedSocket;

import static java.lang.String.format;

/**
 * The HttpAwareServerSocket class extends the java.net.ServerSocket
 * class.  It behaves like a ServerSocket, except that if
 * the first four bytes of an accepted socket are the letters "POST",
 * then it returns an HttpReceiveSocket instead of a java.net.Socket.
 * This means that the accept method blocks until four bytes have been
 * read from the new socket's input stream.
 */
@Log
class HttpAwareServerSocket extends ServerSocket {

    /**
     * Create a server socket on a specified port.
     * @param port the port
     * @exception IOException IO error when opening the socket.
     */
    public HttpAwareServerSocket(int port) throws IOException
    {
        super(port);
    }

    /**
     * Create a server socket, bind it to the specified local port
     * and listen to it.  You can connect to an annonymous port by
     * specifying the port number to be 0.  <i>backlog</i> specifies
     * how many connection requests the system will queue up while waiting
     * for the ServerSocket to execute accept().
     * @param port the specified port
     * @param backlog the number of queued connect requests pending accept
     */
    public HttpAwareServerSocket(int port, int backlog) throws IOException
    {
        super(port, backlog);
    }

    /**
     * Accept a connection. This method will block until the connection
     * is made and four bytes can be read from the input stream.
     * If the first four bytes are "POST", then an HttpReceiveSocket is
     * returned, which will handle the HTTP protocol wrapping.
     * Otherwise, a WrappedSocket is returned.  The input stream will be
     * reset to the beginning of the transmission.
     * In either case, a BufferedInputStream will already be on top of
     * the underlying socket's input stream.
     * @exception IOException IO error when waiting for the connection.
     */
    public Socket accept() throws IOException
    {
        Socket socket = super.accept();
        BufferedInputStream in =
            new BufferedInputStream(socket.getInputStream());

        log.info("socket accepted (checking for POST)");

        in.mark(4);
        boolean isHttp = (in.read() == 'P') &&
                         (in.read() == 'O') &&
                         (in.read() == 'S') &&
                         (in.read() == 'T');
        in.reset();

        if( log.isLoggable(Level.INFO) ) {
                log.info(isHttp ? "POST found, HTTP socket returned" :
                          "POST not found, direct socket returned");
        }

        if (isHttp)
            return new HttpReceiveSocket(socket, in, null);
        else
            return new WrappedSocket(socket, in, null);
    }

    /**
     * Return the implementation address and implementation port of
     * the HttpAwareServerSocket as a String.
     */
    public String toString()
    {
        return format( "HttpAware %s", super.toString()) ;
    }
}
