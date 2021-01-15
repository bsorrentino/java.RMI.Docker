package org.bsc.rmi.jetty_websocket;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class BlockingByteArrayInputStream extends java.io.InputStream {

    final BlockingQueue<ByteBuffer> writeQueue = new LinkedBlockingQueue<>(10);
    final AtomicInteger bytesAvailable = new AtomicInteger(0);

    public BlockingByteArrayInputStream() {
    }

    @Override
    public int read() {
        throw new UnsupportedOperationException("read() is not supported!");
    }

    @Override
    public long skip(long n) {
        throw new UnsupportedOperationException("skip( long ) is not supported!");
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("reset() is not supported!");
    }

    protected void write(@NonNull ByteBuffer bb) {
        bytesAvailable.set(bb.remaining());
        boolean result = writeQueue.offer(bb);
        log.debug( "offer bytes:{} result: {}", bb.remaining(), result);
    }

    @Override
    public int available() {
        return bytesAvailable.get();
        //throw new UnsupportedOperationException("available() is not supported!");
    }

    @Override
    public int read(byte b[], int off, int len) {

        try {
            final ByteBuffer bb = writeQueue.take();
            bytesAvailable.set(0);

            int avail = bb.remaining();
            log.debug( "taken bytes:{} off:{} request {}", avail , off, b.length);
            if( len < avail ) {
                log.warn( "requested is less then available");
            }
            bb.get(b, off, avail);
            return avail;

        } catch (InterruptedException e) {
            log.warn("read has been interrupted");
            return -1;
        }

    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void close() throws IOException {
        log.debug("close()");
    }
}
