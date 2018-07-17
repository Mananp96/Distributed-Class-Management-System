package rudp;

import java.net.Socket;

public class UDPRequest {
	
	private Socket socket;
	
	private String data;

	public UDPRequest(Socket socket, String data) {
		super();
		this.socket = socket;
		this.data = data;
	}

	public Socket getSocket() {
		return socket;
	}

	public String getData() {
		return data;
	}
	
}
