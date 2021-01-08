package org.bsc.rmi.java_websocket;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.rmi.server.RMIClientSocketFactory;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.String.format;

@Slf4j
public class RMIClientWebsocketFactory implements RMIClientSocketFactory {

    static class BlockingByteArrayInputStream extends InputStream {

        private byte buf[];
        private int pos;
        private int count;

        final ReentrantLock lock = new ReentrantLock();
        final Condition notEmpty = lock.newCondition();

        public BlockingByteArrayInputStream() {
            this.buf = null;
            this.pos = 0;
            this.count = 0;
        }

//        public BlockedByteArrayInputStream(byte buf[], int offset, int length) {
//            this.buf = buf;
//            this.pos = offset;
//            this.count = Math.min(offset + length, buf.length);
//            this.mark = offset;
//        }

        protected void setMessage(@NonNull ByteBuffer bb) {
            lock.lock();
            try {
                buf = new byte[bb.remaining()];
                bb.get(buf);
                count = buf.length;
                pos = 0;
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public int read() {
            throw new UnsupportedOperationException("read() is not supported!");

//            lock.lock();
//            try {
//                if (available() == 0) notEmpty.await();
//                return (pos < count) ? (buf[pos++] & 0xff) : -1;
//            } catch (InterruptedException e) {
//                log.warn("read has been interrupted");
//                return -1;
//            } finally {
//                lock.unlock();
//            }
        }

        @Override
        public int read(byte b[], int off, int len) {

            lock.lock();
            try {
                if (available()==0) notEmpty.await();

                if (pos >= count) {
                    return -1;
                }

                int avail = count - pos;
                if (len > avail) {
                    len = avail;
                }
                if (len <= 0) {
                    return 0;
                }
                System.arraycopy(buf, pos, b, off, len);
                pos += len;
                return len;

            } catch (InterruptedException e) {
                log.warn("read has been interrupted");
                return -1;
            } finally {
                lock.unlock();
            }

        }

        @Override
        public long skip(long n) {
            synchronized (this) {
                long k = count - pos;
                if (n < k) {
                    k = n < 0 ? 0 : n;
                }
                pos += k;
                return k;
            }
        }

        @Override
        public int available() {
            synchronized (this) {
                return count - pos;
            }
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public void reset() {
            synchronized (this) {
                pos = 0;
            }
        }

    }

    static class DelegateByteArrayOutputStream extends java.io.OutputStream {
        final RMIWebSocketClient delegate;

        DelegateByteArrayOutputStream(RMIWebSocketClient delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(int b) throws IOException {
            throw new UnsupportedOperationException("write(int) is not supported!");
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            log.info( "write bytes( off:{}, len:{}, b.length:{} )", off, len, b.length);

            delegate.send( ByteBuffer.wrap(b, off, len) );
        }
    }

    static class RMIWebSocketClient extends WebSocketClient {

        final BlockingByteArrayInputStream  istream = new BlockingByteArrayInputStream();
        final java.io.OutputStream          ostream = new DelegateByteArrayOutputStream(this);

        public RMIWebSocketClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {
            log.info("onOpen {}", serverHandshake.getHttpStatusMessage());
        }

        @Override
        public void onMessage(String s) {
            log.info("onMessage string {}", s);
            istream.setMessage( ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)) );
        }

        @Override
        public void onMessage(ByteBuffer bb) {
            log.info("onMessage bytes {}", bb.remaining());
            istream.setMessage( bb );
        }

        @Override
        public void onClose(int i, String s, boolean b) {
            log.info("onClose( {}, {}, {} )", i, s, b);
            try {
                istream.close();
            } catch (IOException e) {
                log.error( "error closing the input stream", e);
            }
            try {
                ostream.close();
            } catch (IOException e) {
                log.error( "error closing the output stream", e);
            }
        }

        @Override
        public void onError(Exception e) {
            log.error("web socket error", e);
        }
    }

    @EqualsAndHashCode
    static class WebSocketClientProxy extends Socket {
        final RMIWebSocketClient client;

        public WebSocketClientProxy(String host, int port) throws IOException {
            super(host, port);

            log.info("create rmi client socket - host:{} port:{}", host, port);

            client = new RMIWebSocketClient(java.net.URI.create(format("ws://%s:%d", host, port)));
            try {
                client.connectBlocking();
            } catch (InterruptedException e) {
                throw new IOException("connection interrupted!");
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return client.istream;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return client.ostream;
        }


    }

    /**
     * Create a client socket connected to the specified host and port.
     *
     * @param host the host name
     * @param port the port number
     * @return a socket connected to the specified host and port.
     * @throws IOException if an I/O error occurs during socket creation
     * @since 1.2
     */
    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return new WebSocketClientProxy(host, port);
    }
}
