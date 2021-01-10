package org.bsc.rmi.proxy.socket.debug;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.*;
import java.rmi.server.RMIServerSocketFactory;

@Slf4j
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
            log.debug( "\n>\nwrite bytes( off:{}, len:{} )\n<", off, len);
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

            log.debug( "\n>\nread bytes( off:{}, len:{} ) = {}\n<", off, len, result);

            return result;
        }
    }

    @EqualsAndHashCode
    static class DebugSocket extends Socket {

        protected DebugSocket(SocketImpl impl) throws SocketException {
            super(impl);
        }

        public DebugSocket() {}

        @Override
        public void connect(SocketAddress endpoint) throws IOException {
            log.debug( "connect( endpoint:{} )", endpoint);
            super.connect(endpoint);
        }

        @Override
        public void connect(SocketAddress endpoint, int timeout) throws IOException {
            log.debug( "connect( endpoint:{}, timeout:{} )", endpoint, timeout);
            super.connect(endpoint, timeout);
        }

        @Override
        public void bind(SocketAddress bindpoint) throws IOException {
            log.debug( "bind( bindpoint:{} )", bindpoint);
            super.bind(bindpoint);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new DebugInputStream(super.getInputStream());
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return new DebugOutputStream(super.getOutputStream());
        }

        @Override
        public synchronized void close() throws IOException {
            log.debug( "close()");
            super.close();
        }

        @Override
        public void shutdownInput() throws IOException {
            log.debug( "shutdownInput()");
            super.shutdownInput();
        }

        @Override
        public void shutdownOutput() throws IOException {
            log.debug( "shutdownOutput()");
            super.shutdownOutput();
        }

        @Override
        public boolean isConnected() {
            boolean v = super.isConnected();
            log.debug( "isConnected()={}", v);
            return v;
        }

        @Override
        public boolean isBound() {
            boolean v = super.isBound();
            log.debug( "isBound()={}", v);
            return v;
        }

        @Override
        public boolean isClosed() {
            boolean v = super.isClosed();
            log.debug( "isClosed()={}", v);
            return v;
        }
    }

    @EqualsAndHashCode
    static class DebugServerSocket extends ServerSocket {


        public DebugServerSocket(int port) throws IOException {
            super(port);

        }

        @Override
        public Socket accept() throws IOException {
            log.debug( "create rmi server socket factory - port: {}", super.getLocalPort());
            final Socket result = new DebugSocket();
            implAccept(result);
            return result;
        }

    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return new DebugServerSocket(port);
    }
}
