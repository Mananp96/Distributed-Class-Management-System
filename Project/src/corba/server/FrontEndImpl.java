package corba.server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import logerfactory.*;
import rudp.UDPClient;
import FrontEndApp.*;

import org.omg.CORBA.ORB;

import FrontEndApp.FrontEndPOA;
import dcms.LoggerFactory;
import util.FEMessage;
import util.UdpPort;

/**
 * Front End remote interface implementation
 * @author Manan Prajapati
 */

public class FrontEndImpl extends FrontEndPOA {
	
	private LinkedList<String> queueA = new LinkedList<String>();
	private int leaderServerPort = UdpPort.Server1_ACTION_PORT;
	FEUdpServer feudp;
	private String name;
	
	@Override
	public String createTRecord(String firstName, String lastName, String address, String phone, String specialization,
			String location, String managerId) {
		
		LoggerFactory.Log(this.name, "Validating fields...");
		if (firstName == null || firstName.isEmpty()) {
			LoggerFactory.Log(this.name, "First name required");
			// throw new RequiredValueException("First name required");
		}

		if (lastName == null || lastName.isEmpty()) {
			LoggerFactory.Log(this.name, "Last name required");
			// throw new RequiredValueException("Last name required");
		}

		if (address == null || address.isEmpty()) {
			LoggerFactory.Log(this.name, "Address required");
			// throw new RequiredValueException("Address required");
		}

		if (phone == null || phone.isEmpty()) {
			LoggerFactory.Log(this.name, "Phone required");
			// throw new RequiredValueException("Phone required");
		}

		if (specialization == null || specialization.isEmpty()) {
			LoggerFactory.Log(this.name, "Specialization required");
			// throw new RequiredValueException("Specialization required");
		}

		if (location == null) {
			LoggerFactory.Log(this.name, "Status required");
			// throw new RequiredValueException("Status required");
		}
		LoggerFactory.Log(this.name, "Validating fields complete...");
		
		String msg = "CREATETR" + "|" + "TR" + generateNumber() + "|" + managerId + "|" + firstName + "|" + lastName + "|" + address + "|" + phone + "|" + specialization + "|" +
		location;
		String ack = sendMessage(msg);
		return ack;
	}

	@Override
	public String createSRecord(String firstName, String lastName, String[] courseRegistered, String status,
			String statusDate, String managerId) {
		
		LoggerFactory.Log(this.name, "Validating fields...");
		if (firstName == null || firstName.isEmpty()) {
			LoggerFactory.Log(this.name, "First name required");
			// throw new RequiredValueException("First name required");
		}

		if (lastName == null || lastName.isEmpty()) {
			LoggerFactory.Log(this.name, "Last name required");
			// throw new RequiredValueException("Last name required");
		}

		if (statusDate == null || statusDate.isEmpty()) {
			LoggerFactory.Log(this.name, "Status Date required");
			// throw new RequiredValueException("Status Date required");
		}

		if (courseRegistered == null || courseRegistered.length < 1) {
			LoggerFactory.Log(this.name, "Registed Course required");
			// throw new RequiredValueException("Registered Course required");
		}

		if (status == null) {
			LoggerFactory.Log(this.name, "Status required");
			// throw new RequiredValueException("Status required");
		}
		LoggerFactory.Log(this.name, "Validating fields complete...");
		
		String msg = "CREATESR" + "|" + "SR" + generateNumber() + "|" + managerId + "|" + firstName + "|" + lastName + "|" + courseRegistered + "|" + status + "|" + statusDate;
		String ack = sendMessage(msg);
		return ack;
	}

	@Override
	public String getRecordCount(String managerId) {
		String msg = "RECORDCOUNT" + "," + managerId;
		String ack = sendMessage(msg);
		return ack;
	}

	@Override
	public String editRecords(String recordId, String fieldName, String newValue, String managerId) {
		
		LoggerFactory.Log(this.name, "Manager :" + managerId + " requested to edit a record.");
		LoggerFactory.Log(this.name, String.format("Editing record, RecordID:%s", recordId));

		if (recordId == null || recordId.isEmpty()) {
			LoggerFactory.Log(this.name, "Record ID required");
			// throw new RequiredValueException("Record ID required");
		}

		if (fieldName == null || fieldName.isEmpty()) {
			LoggerFactory.Log(this.name, "FieldName required");
			// throw new RequiredValueException("FieldName required");
		}

		if (newValue == null || newValue.isEmpty()) {
			LoggerFactory.Log(this.name, "FieldValue required");
			// throw new RequiredValueException("FieldValue required");
		}

		String msg = "EDITRECORD" + "," + managerId + "," + recordId + "," + fieldName + "," + newValue;
		String ack = sendMessage(msg);
		return ack;
	}

	@Override
	public String transferRecord(String managerId, String recordId, String remoteCenterServerName) {
		
		LoggerFactory.Log(this.name, "Manager :" + managerId + " requested to transfer a record.");
		LoggerFactory.Log(this.name, String.format("Transering record, RecordID:%s", recordId));

		String msg = "TRANSFERRECORD" + "," + managerId + "," + recordId + "," + remoteCenterServerName;
		String ack = sendMessage(msg);
		return ack;
	}
	
	private synchronized int generateNumber() {

		Random random = new Random(System.nanoTime());

		return 10000 + random.nextInt(89999);
	}
	
	private String sendMessage(String msg) {
		
		String status = null;
		boolean queuestatus = this.addQueue(msg);
		
		if(!queuestatus)
		{
			System.out.println("Message did not added to queue.");
			status = "Message did not added to queue";
		}
		else
		{
			//invoking FEUdpServer to send message to Lead Server.
			status = this.sendFirstMessage(leaderServerPort);
			
			//invoking FEUdpServer to remove processed message from Queue
			this.removeQueue();
		}
		
		return status;
	}
	
	
	//Add request message to queue
	public boolean addQueue(String femessage) {
		
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
	
	//Sends first message in queue
	public String sendFirstMessage(int leaderServerPort) {
		
		ArrayList<String> processstatus = new ArrayList<>();
	
		if(!queueA.isEmpty()) 
		{	
			String femessage = queueA.getFirst();
			
			try {
				final DatagramSocket frontEndSocket = new DatagramSocket(UdpPort.FE_PORT);
				
				InetAddress IPAddress = InetAddress.getByName("localhost");
				
				new Thread(() -> {
					UDPClient client = new UDPClient("127.0.0.1", 9191);
					
					String messageStatus;
					try {
						messageStatus = client.sendMessage(femessage);
						processstatus.add(messageStatus);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
//					if(messageStatus.equalsIgnoreCase("leader_crashed")) {
//						processstatus.add("Lead Server Crashed");
//					}
//					else
//					{
//						String response = null;
//						//getting response from server
//						response = receiveData(frontEndSocket);
//						if(response == null) {
//							processstatus.add("Failed to get data from server");
//						}
//						else {
//							processstatus.add(response);
//							
//						}
//				
//					}
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

	
	
	
}
