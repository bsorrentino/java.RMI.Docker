package org.bsc.rmi.proxy.socket.debug;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

@Slf4j
public class RMIDebugClientSocketFactory implements RMIClientSocketFactory {

    private String formatPrintableBuffer( byte[] b, int off, int len ) {
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
            if(log.isDebugEnabled()) {
                log.debug("\n>\nwrite to port:{} bytes( off:{}, len:{}, b.length:{} )\n{}\n<",
                        port,
                        off, len, b.length,
                        formatPrintableBuffer(b, off, len));
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

            if(log.isDebugEnabled()) {
                log.debug("\n>\nread from port:{} bytes( off:{}, len:{} ) = {}\n{}\n<",
                        port,
                        off, len, result,
                        formatPrintableBuffer(b, off, result));
            }

            return result;
        }
    }

    @EqualsAndHashCode
    class DebugSocket extends Socket {

        public DebugSocket(String host, int port) throws IOException {
            super(host, port);
            log.debug( "create rmi client socket - host:{} port:{}", host, port);
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

    int port;

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        this.port = port;
        return new DebugSocket( host, port );
    }
}
