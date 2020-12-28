package org.bsc.rmi.proxy.socket.client;

import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;

import java.io.*;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.util.logging.Level;

import static java.lang.String.format;

@Log
public class RMIDebugClientSocketFactory implements RMIClientSocketFactory {

    String formatPrintableBuffer( byte[] b, int off, int len ) {
        final StringBuilder sb = new StringBuilder();
        for( int i=off ; i < len ; ++i ) {
            if( Character.isAlphabetic((char)b[i]) ) {
                sb.append( '\"').append( (char)b[i] ).append('\"');
            }
            else {
                sb.append( b[i] );
            }
            sb.append(' ');
        }
        return sb.toString();
    }

    class DebugOutputStream extends FilterOutputStream {
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
                log.fine( format("\n>\nwrite bytes( off:%d, len:%d, b.length:%d )\n%s<\n",
                            off, len, b.length, formatPrintableBuffer(b,off,len)));
            }
            super.write(b, off, len);
        }
    }

    class DebugInputStream extends FilterInputStream {

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
                log.fine( format("\n>\nread bytes( off:%d, len:%d ) = %d\n%s\n<",
                        off, len, result, formatPrintableBuffer(b,off,result)));
            }

            return result;
        }
    }

    @EqualsAndHashCode
    class DebugSocket extends Socket {


        public DebugSocket(String host, int port) throws IOException {
            super(host, port);

            log.info( format("create rmi client socket - host:%s port:%d", host, port));
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

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return new DebugSocket( host, port );
    }
}
