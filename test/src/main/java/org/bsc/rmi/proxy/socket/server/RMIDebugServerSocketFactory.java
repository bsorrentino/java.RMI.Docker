package org.bsc.rmi.proxy.socket.server;

import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIServerSocketFactory;
import java.util.logging.Level;

import static java.lang.String.format;

@Log
public class RMIDebugServerSocketFactory implements RMIServerSocketFactory {

    static class DebugOutputStream extends FilterOutputStream {
        public DebugOutputStream(OutputStream out) {
            super(out);
        }

//        @Override
//        public void write(int b) throws IOException {
//            if(log.isLoggable(Level.FINE)) {
//                log.fine( format(">\nwrite = '%c' %d\n<", (char)b, b));
//            }
//            super.write(b);
//        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if(log.isLoggable(Level.FINE)) {
                log.fine( format("\n>\nwrite bytes( off:%d, len:%d )\n<", off, len));
            }
            super.write(b, off, len);
        }
    }

    static class DebugInputStream extends FilterInputStream {

        public DebugInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            throw new IllegalAccessError("We don't expect that this method is invoked directly!");
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int result = super.read(b, off, len);

            if(log.isLoggable(Level.FINE)) {
                log.fine( format("\n>\nread bytes( off:%d, len:%d ) = %d\n<", off, len, result));
            }

            return result;
        }
    }

    @EqualsAndHashCode
    static class DebugSocket extends Socket {

        /**
         * Creates an unconnected socket, with the
         * system-default type of SocketImpl.
         *
         * @revised 1.4
         * @since JDK1.1
         */
        public DebugSocket() {
            log.info( format("create rmi server socket ") );
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new DebugInputStream(super.getInputStream());
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return new DebugOutputStream(super.getOutputStream());
        }


    }

    @EqualsAndHashCode
    static class DebugServerSocket extends ServerSocket {


        public DebugServerSocket(int port) throws IOException {
            super(port);

            log.info( format("create rmi server socket factory - port:%d", port));
        }

        @Override
        public Socket accept() throws IOException {
            final Socket result = new DebugSocket();
            implAccept(result);
            return result;
        }

    }

    /**
     * Create a server socket on the specified port (port 0 indicates
     * an anonymous port).
     *
     * @param port the port number
     * @return the server socket on the specified port
     * @throws IOException if an I/O error occurs during server socket
     *                     creation
     * @since 1.2
     */
    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return new DebugServerSocket(port);
    }
}
