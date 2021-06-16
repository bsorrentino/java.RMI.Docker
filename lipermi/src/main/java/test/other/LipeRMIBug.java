package test.other;

import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.handler.filter.DefaultFilter;
import net.sf.lipermi.SocketClient;
import net.sf.lipermi.SocketServer;

import java.io.IOException;

public class LipeRMIBug {

    @FunctionalInterface
    public interface Calc {
        double sqrt(double d);
    }



    private static void startServer() throws Exception {
        final CallHandler handler = new CallHandler();
        handler.registerGlobal(Calc.class, new Calc() {
            public double sqrt(double d) {
                return Math.sqrt(d);
            }
        });
        final SocketServer server = new SocketServer();
        server.bind(36666, handler, null);

        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }}));
    }

    public static void main(String[] args) throws Exception {
        startServer();

        final SocketClient client = new SocketClient("localhost", 36666, new CallHandler(), new DefaultFilter());
        final Calc remote = client.getGlobal(Calc.class);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    client.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });

        final Thread t1 = new Thread(new Runnable() {
            public void run() {
                try {
                    while(true) {
                        //synchronized(pm) {
                            final double result = remote.sqrt(64);
                            if(result != 8) {
                                throw new RuntimeException(Thread.currentThread() + "| result was " + result + " instead of 8");
                            }
                        //}
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }, "sqrting 64");
        final Thread t2 = new Thread(new Runnable() {
            public void run() {
                try {
                    while(true) {
                        //synchronized(pm) {
                            final double result = remote.sqrt(100);
                            if(result != 10) {
                                throw new RuntimeException(Thread.currentThread() + "| result was " + result + " instead of 10");
                            }
                        //}
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }, "sqrting 100");

        t1.start();
        t2.start();
    }
}