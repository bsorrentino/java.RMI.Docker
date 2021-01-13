package org.bsc.rmi.jetty_websocket;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class BlockingByteArrayInputStream extends java.io.InputStream {

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
