package corba.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import util.FEMessage;
import util.UdpPort;

/**
 * defining methods to add and remove requests from queue, send request to Leader Server 
 * through UDP.
 * @author Manan Prajapati
 */
public class FEUdpServer{
	
	private LinkedList<FEMessage> queueA = new LinkedList<FEMessage>();
	
	//Add request message to queue
	public boolean addQueue(FEMessage femessage) {
		boolean status = true;
		try {
		queueA.add(femessage);
		}catch (Exception e) {
			status = false;
			System.out.println("Exception occurred:" +e.getMessage());
			}
		return status;
	}
	
	//Remove first element of queue
	public void removeQueue() {
		queueA.removeFirst();
		
	}
	
	public String sendFirstMessage(int leaderServerPort) {
		
		ArrayList<String> processstatus = new ArrayList<>();
	
		if(!queueA.isEmpty()) 
		{	
			FEMessage femessage = queueA.getFirst();
			
			try {
				final DatagramSocket frontEndSocket = new DatagramSocket(UdpPort.FE_PORT);
				
				InetAddress IPAddress = InetAddress.getByName("localhost");
				
				new Thread(() -> {
					
					final String messageStatus = sendMsgToServer(femessage,frontEndSocket,IPAddress,leaderServerPort);
					
					if(messageStatus.equalsIgnoreCase("leader_crashed")) {
						processstatus.add("Lead Server Crashed");
					}
					else
					{
						String response = null;
						//getting response from server
						response = receiveData(frontEndSocket);
						if(response == null) {
							processstatus.add("Failed to get data from server");
						}
						else {
							processstatus.add(response);
							
						}
				
					}
					}).start();
					frontEndSocket.close();	
				}catch (Exception e) {
					e.printStackTrace();
				} 
		
		}else {
			processstatus.add("No Request in Queue");
		}
		
		
		
		return processstatus.get(0);
	}
	
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
