import java.io.IOException;
import java.net.InetAddress;

import rudp.UDPServer;
import rudp.UDPServerListener;

public class ServerUDPTest {

	public static void main(String[] args) throws IOException {
		
		UDPServer server = new UDPServer(9191, new UDPServerListener() {
			
			@Override
			public String respondRequest(String requestData, InetAddress clientHost) {
				System.out.println(requestData);
				return "Request: "+clientHost.getHostAddress()+" Response from server: "+requestData;
			}
		});
		
		server.start();
		System.out.println("Server started");
		
		
	}

}
