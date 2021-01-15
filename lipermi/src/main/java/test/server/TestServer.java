package test.server;

import lombok.extern.slf4j.Slf4j;
import net.sf.lipermi.exception.LipeRMIException;
import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.handler.CallLookup;
import net.sf.lipermi.handler.filter.GZipFilter;
import net.sf.lipermi.net.IServerListener;
import net.sf.lipermi.net.Server;
import test.common.AnotherObject;
import test.common.ListenerTest;
import test.common.TestService;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class TestServer {
	static class AnotherObjectImpl implements AnotherObject {

		private static final long serialVersionUID = -1881637933978100698L;

		int myNumber;

		AnotherObjectImpl(int myNumber) {
			this.myNumber = myNumber;
		}

		public void test() {
			log.debug("AnotherObjectImpl::test() .. myNumber=" + myNumber);
		}

		public int getNumber() {
			return myNumber;
		}

		public void gimmeYourListener(ListenerTest listener) {
			listener.makingSomeCallback("the listener works, dude.");
		}

	}

	static class TestServiceImpl implements TestService {
		final CallHandler callHandler;
		int anotherNumber = 0;

		public TestServiceImpl(CallHandler callHandler) {
			this.callHandler = callHandler;
		}

		@Override
		public String letsDoIt() {
			log.info("letsDoIt() done.");
			Socket clientSocket = CallLookup.getCurrentSocket();
			log.info("My client: " + clientSocket.getRemoteSocketAddress());
			return "server saying hi";
		}

		@Override
		public AnotherObject getAnotherObject() {
			log.info("building AnotherObject with anotherNumber=" + anotherNumber);
			AnotherObject ao = new AnotherObjectImpl(anotherNumber++);
			try {
				callHandler.exportObject(AnotherObject.class, ao);
			} catch (LipeRMIException e) {
				e.printStackTrace();
			}
			return ao;
		}

		@Override
		public void throwAExceptionPlease() {
			throw new AssertionError("ok, ok");
		}
	}
	public TestServer() {

		log.info("Creating Server");
		Server server = new Server();
		server.addServerListener(new IServerListener() {
			public void clientConnected(Socket socket) {
				log.info("Client connected: " + socket.getInetAddress());
			}

			public void clientDisconnected(Socket socket) {
				log.info("Client disconnected: " + socket.getInetAddress());
			}
		});

		log.info("Creating CallHandler");

		final CallHandler callHandler = new CallHandler();
		final TestService service = new TestServiceImpl(callHandler);

		try {

			log.info("Registrating implementation");
			callHandler.registerGlobal(TestService.class, service);
			log.info("Binding");

			server.bind(1234, callHandler, new GZipFilter());

			log.info("Server listening");

		} catch (LipeRMIException e) {
			log.error( "Lipe RMI error", e);
		} catch (IOException e) {
			log.error( "error", e);
		}
		
	}

	public static void main(String[] args) {
		new TestServer();
	}


}
