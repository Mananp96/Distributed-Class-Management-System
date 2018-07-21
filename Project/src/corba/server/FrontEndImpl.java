package corba.server;

import java.util.Random;

import org.omg.CORBA.ORB;

import logerfactory.*;
import FrontEndApp.*;
import util.FEMessage;
import util.UdpPort;

/**
 * Front End remote interface implementation
 * @author Manan Prajapati
 */

public class FrontEndImpl extends FrontEndPOA {
	
	private int leaderServerPort = UdpPort.Server1_ACTION_PORT;
	FEUdpServer feudp;
	private ORB orb;
	private String name;
	
	void setORB(ORB orb_val) 
	{
		orb = orb_val;
	}

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
		
		String msg = "1" + "," + "TR" + generateNumber() + "," + managerId + "," + firstName + "," + lastName + "," + address + "," + phone + "," + specialization + "," +
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
		
		String msg = "2" + "," + "SR" + generateNumber() + "," + managerId + "," + firstName + "," + lastName + "," + courseRegistered + "," + status + "," + statusDate;
		String ack = sendMessage(msg);
		return ack;
	}

	@Override
	public String getRecordCount(String managerId) {
		String msg = "3" + "," + managerId;
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

		String msg = "4" + "," + managerId + "," + recordId + "," + fieldName + "," + newValue;
		String ack = sendMessage(msg);
		return ack;
	}

	@Override
	public String transferRecord(String managerId, String recordId, String remoteCenterServerName) {
		
		LoggerFactory.Log(this.name, "Manager :" + managerId + " requested to transfer a record.");
		LoggerFactory.Log(this.name, String.format("Transering record, RecordID:%s", recordId));

		String msg = "5" + "," + managerId + "," + recordId + "," + remoteCenterServerName;
		String ack = sendMessage(msg);
		return ack;
	}
	
	private synchronized int generateNumber() {

		Random random = new Random(System.nanoTime());

		return 10000 + random.nextInt(89999);
	}
	
	private String sendMessage(String msg) {
		
		String status = null;
		FEMessage femessage = new  FEMessage(msg);
		boolean queuestatus = feudp.addQueue(femessage);
		if(!queuestatus) {
			System.out.println("Message did not added to queue.");
			status = "Message did not added to queue";
		}
		else {
			//invoking FEUdpServer to send message to Lead Server.
			status = feudp.sendFirstMessage(leaderServerPort);
			
			//invoking FEUdpServer to remove processed message from Queue
			feudp.removeQueue();
		}
		
		return status;
	}
	
	
}
