package org.bsc.rmi.jetty_websocket;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.rmi.server.RMIClientSocketFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
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
        final RMIWebSocketClientListener delegate;

        DelegateByteArrayOutputStream(RMIWebSocketClientListener delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(int b) throws IOException {
            throw new UnsupportedOperationException("write(int) is not supported!");
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            log.info( "write bytes( off:{}, len:{}, b.length:{} )", off, len, b.length);

            delegate.getSession().getRemote().sendBytes( ByteBuffer.wrap(b, off, len) );
        }
    }

    static class RMIWebSocketClientListener extends WebSocketAdapter {
        final private CountDownLatch closureLatch = new CountDownLatch(1);

        final BlockingByteArrayInputStream  istream = new BlockingByteArrayInputStream();
        final java.io.OutputStream          ostream = new DelegateByteArrayOutputStream(this);

        public RMIWebSocketClientListener() {
        }

        @Override
        public void onWebSocketConnect(Session sess)
        {
            super.onWebSocketConnect(sess);
            log.debug("onWebSocketConnect( {} )", sess);
        }


        @Override
        public void onWebSocketText(String message)
        {
            super.onWebSocketText(message);
            log.debug("onMessage string {}", message);
            istream.setMessage( ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)) );

        }

        @Override
        public void onWebSocketBinary(byte[] payload, int offset, int len) {
            super.onWebSocketBinary(payload, offset, len);

            log.debug("onMessage bytes {}", len);
            istream.setMessage( ByteBuffer.wrap(payload,offset,len) );
        }

        @Override
        public void onWebSocketClose(int statusCode, String reason)
        {
            super.onWebSocketClose(statusCode, reason);
            log.debug("onWebSocketClose( {}, {} )", statusCode, reason);
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
            closureLatch.countDown();
        }

        @Override
        public void onWebSocketError(Throwable cause)
        {
            super.onWebSocketError(cause);
            log.error("web socket error", cause);

        }
    }

    @EqualsAndHashCode
    class WebSocketClientProxy extends Socket {
        final RMIWebSocketClientListener listener;
        final WebSocketClient client = new WebSocketClient();

        public WebSocketClientProxy(String host, int rmi_port) throws Exception {
            super(host, websocket_port);

            log.debug("create rmi client socket - host:{} rmi port:{} websocket port:{}",
                    host,
                    rmi_port,
                    websocket_port);

            listener = new RMIWebSocketClientListener();

            client.start();

            final java.net.URI uri = java.net.URI.create(format("ws://%s:%d/rmi", host, websocket_port));

            final ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader("rmi_port", String.valueOf(rmi_port));

            Future<Session> sessionFuture = client.connect(listener,uri, request);

            sessionFuture.get();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return listener.istream;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return listener.ostream;
        }


    }

    final int websocket_port;

    public RMIClientWebsocketFactory(int websocket_port) {
        this.websocket_port = websocket_port;
    }

    public int getWebsocketPort() {
        return websocket_port;
    }

    @Override
    public Socket createSocket(String host, int rmi_port) throws IOException {
        try {
            return new WebSocketClientProxy(host, rmi_port);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }
}
