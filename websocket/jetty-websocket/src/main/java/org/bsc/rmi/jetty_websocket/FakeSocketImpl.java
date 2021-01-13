package org.bsc.rmi.jetty_websocket;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;

@Slf4j
public class FakeSocketImpl extends SocketImpl {
  @Override
  protected void create(boolean stream) throws IOException {
    log.debug( "create stream:{}", stream );
  }

  @Override
  protected void connect(String host, int port) throws IOException {
    log.debug( "connect host:{}, port:{}", host, port );
  }

  @Override
  protected void connect(InetAddress address, int port) throws IOException {
    log.debug( "connect address:{}, port:{}", address, port );
  }

  @Override
  protected void connect(SocketAddress address, int timeout) throws IOException {
    log.debug( "connect address:{}, timeout:{}", address, timeout );
  }

  @Override
  protected void bind(InetAddress host, int port) throws IOException {
    log.debug( "bind host:{}, timeout:{}", host, port );
  }

  @Override
  protected void listen(int backlog) throws IOException {
    log.debug( "listen backlog:{}", backlog );
  }

  /**
   * Accepts a connection.
   *
   * @param s the accepted connection.
   * @throws IOException if an I/O error occurs when accepting the
   *                     connection.
   */
  @Override
  protected void accept(SocketImpl s) throws IOException {
    log.debug( "accept s:{}", s );
  }

  @Override
  protected InputStream getInputStream() throws IOException {
    log.debug( "getInputStream()" );
    return null;
  }

  @Override
  protected OutputStream getOutputStream() throws IOException {
    log.debug( "getOutputStream()" );
    return null;
  }

  @Override
  protected int available() throws IOException {
    log.debug( "available()" );
    return 0;
  }

  @Override
  protected void close() throws IOException {
    log.debug( "close()" );
  }

  @Override
  protected void sendUrgentData(int data) throws IOException {
    log.debug( "sendUrgentData( data:{} )", data );
  }

  @Override
  public void setOption(int optID, Object value) throws SocketException {
    log.debug( "setOption( optID:{}, value:{} )", optID, value );

  }

  @Override
  public Object getOption(int optID) throws SocketException {
    log.debug( "getOption( optID:{} )", optID );
    return null;
  }
}
