package corba.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NoRouteToHostException;

import util.FEMessage;

/**
 * defining methods to add and remove requests from queue, send request to Leader Server 
 * through UDP.
 * @author Manan Prajapati
 */
public class FEUdpServer{
	
	
	
	
	public String sendMsgToServer(FEMessage femessage, DatagramSocket frontEndSocket, InetAddress IPAddress, int leaderServerPort) {
		
		String serverstatus = null;
		try {
			// Serialize to a byte array
			ByteArrayOutputStream bStream = new ByteArrayOutputStream();
			ObjectOutput oo = new ObjectOutputStream(bStream); 
			oo.writeObject(femessage);
			byte[] serializedMessage = bStream.toByteArray();
			oo.close();

//			And on the receiving end:
//
//				ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(recBytes));
//				Message messageClass = (Message) iStream.readObject();
//				iStream.close();
//			
			//send message
			
			DatagramPacket sendPacket = new DatagramPacket(serializedMessage,serializedMessage.length,IPAddress,leaderServerPort);
			System.out.println("Front End send request to:"+leaderServerPort);
			frontEndSocket.send(sendPacket);
												
		} catch (NoRouteToHostException e) {
			System.out.println("leader down");
			serverstatus = "leader_crashed";
			
		} catch (IOException e) {
			
			e.printStackTrace();
		} 
		
		return serverstatus;
	}
	
	
	private String receiveData(DatagramSocket frontEndSocket) {
		
		String receivedData = null;
		try {
		
			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
			frontEndSocket.receive(receivePacket);
			receivedData = new String(receivePacket.getData()).trim();
			
			//System.out.println("From Server:"+receivedData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return receivedData;
	}
}
