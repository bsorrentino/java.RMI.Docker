package test.other;

import java.util.ArrayList;
import java.util.List;

import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.net.Client;
import net.sf.lipermi.net.Server;

public class LipeRMIBug2 {

    public interface Calc {
        List<Integer> add(int i);
    }



    private static void startServer() throws Exception {
        final CallHandler handler = new CallHandler();
        handler.registerGlobal(Calc.class, new Calc() {
            private int n = 0;
            private List<Integer> same = new ArrayList<Integer>();
            public List<Integer> add(int i) {
                //final List<Integer> notSame = new ArrayList<Integer>();
                //notSame.add(n++);
                //return notSame;
                same.clear();
                same.add(n++);
                return same;
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

        final Client client = Client.of("localhost", 36666);
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
                    for(int i = 0; i < 10; i++) {
                        System.out.println(remote.add(6));
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t1.start();
    }
}