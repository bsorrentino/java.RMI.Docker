package org.bsc.rmi.proxy.socket.client;

import lombok.EqualsAndHashCode;

import java.io.*;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

public class RMIDebugClientSocket implements RMIClientSocketFactory {

    static class DebugOutputStream extends FilterOutputStream {
        public DebugOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(int b) throws IOException {
            super.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            super.write(b, off, len);
        }
    }

    static class DebugInputString extends FilterInputStream {

        public DebugInputString(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            return super.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return super.read(b, off, len);
        }
    }

    @EqualsAndHashCode
    static class DebugSocket extends Socket {


        public DebugSocket(String host, int port) throws IOException {
            super(host, port);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new DebugInputString(super.getInputStream());
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return new DebugOutputStream(super.getOutputStream());
        }


    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return null;
    }
}
