package rudp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import net.rudp.ReliableSocket;

public class UDPClient {

	private SocketAddress serverAddress;
	
	
	public UDPClient(String host,int port) {
		this.serverAddress = new InetSocketAddress(host, port);
	}
	
	
	public String sendMessage(String data) throws IOException {
		ReliableSocket client = new ReliableSocket();

		client.connect(this.serverAddress,6000);
		

		DataOutputStream stream = new DataOutputStream(client.getOutputStream());
		stream.writeUTF(data);
		stream.close();
		
		DataInputStream responseStream = new DataInputStream(client.getInputStream());
		String responseData = "";
		try {
			responseData = responseStream.readUTF();
		} catch (EOFException e) {}
		responseStream.close();
		client.close();
		return responseData;
	}
}
