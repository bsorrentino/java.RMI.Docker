package test.other;

import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.net.Client;
import net.sf.lipermi.net.Server;

public class LipeRMIBug {

    public interface Calc {
        public double sqrt(double d);
    }



    private static void startServer() throws Exception {
        final CallHandler handler = new CallHandler();
        handler.registerGlobal(Calc.class, new Calc() {
            public double sqrt(double d) {
                return Math.sqrt(d);
            }
        });
        final Server server = new Server();
        server.bind(36666, handler);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.close();
            }
        });
    }

    public static void main(String[] args) throws Exception {
        startServer();

        final CallHandler handler = new CallHandler();
        final Client client = new Client("localhost", 36666, handler);
        final Calc remote = (Calc) client.getGlobal(Calc.class);

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