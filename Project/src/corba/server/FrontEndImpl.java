package corba.server;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import FrontEndApp.FrontEndPOA;
import dcms.LoggerFactory;
import rudp.UDPClient;

/**
 * Front End remote interface implementation
 * 
 * @author Manan Prajapati
 */

public class FrontEndImpl extends FrontEndPOA {

	private Region MTLLeader;
	private Region LVLLeader;
	private Region DDOLeader;

	public FrontEndImpl() throws FileNotFoundException, IOException, ParseException {
		initialLeaders();
		BullyAlgorithm ba = new BullyAlgorithm(this.MTLLeader, this.LVLLeader, this.DDOLeader);
	}

	private void initialLeaders() throws FileNotFoundException, IOException, ParseException {
		JSONParser parser = new JSONParser();
		JSONObject config = (JSONObject) parser.parse(new FileReader("resources/config.json"));
		JSONObject leader = (JSONObject) config.get("leader");

		this.MTLLeader = initialRegion((JSONObject) leader.get("MTL"));
		this.LVLLeader = initialRegion((JSONObject) leader.get("LVL"));
		this.DDOLeader = initialRegion((JSONObject) leader.get("DDO"));

	}

	private Region initialRegion(JSONObject region) {
		Region s = new Region();
		s.Region = (String) region.get("region");
		s.Host = (String) region.get("host");
		s.Port = (int) region.get("port");
		s.ID = (int) region.get("id");

		return s;
	}
	
	private String sendMessage(String managerID, String msg)  {
		Region r = null;
		String regionStr = managerID.substring(0, 3);
		if (regionStr.equalsIgnoreCase("MTL"))
			r = this.MTLLeader;
		else if (regionStr.equalsIgnoreCase("LVL"))
			r = this.LVLLeader;
		else
			r = this.DDOLeader;

		UDPClient client = new UDPClient(r.Host, r.Port);
		int i = 0;
		while (i++ < 100) {

			try {
				return client.sendMessage(msg);
				

			} catch (IOException e) {

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					
					e1.printStackTrace();
				}

				e.printStackTrace();
			}
		}
		
		return "Error";
	}

	@Override
	public String createTRecord(String firstName, String lastName, String address, String phone, String specialization,
			String location, String managerId) {

		LoggerFactory.LogFrontEnd("Validating fields...");
		if (firstName == null || firstName.isEmpty()) {
			LoggerFactory.LogFrontEnd("First name required");
			// throw new RequiredValueException("First name required");
		}

		if (lastName == null || lastName.isEmpty()) {
			LoggerFactory.LogFrontEnd("Last name required");
			// throw new RequiredValueException("Last name required");
		}

		if (address == null || address.isEmpty()) {
			LoggerFactory.LogFrontEnd("Address required");
			// throw new RequiredValueException("Address required");
		}

		if (phone == null || phone.isEmpty()) {
			LoggerFactory.LogFrontEnd("Phone required");
			// throw new RequiredValueException("Phone required");
		}

		if (specialization == null || specialization.isEmpty()) {
			LoggerFactory.LogFrontEnd("Specialization required");
			// throw new RequiredValueException("Specialization required");
		}

		if (location == null) {
			LoggerFactory.LogFrontEnd("Status required");
			// throw new RequiredValueException("Status required");
		}
		LoggerFactory.LogFrontEnd("Validating fields complete...");

		String msg = "CREATETR" + "|" + "TR" + generateNumber() + "|" + managerId + "|" + firstName + "|" + lastName
				+ "|" + address + "|" + phone + "|" + specialization + "|" + location;
		String ack = sendMessage(managerId, msg);
		return ack;
	}

	@Override
	public String createSRecord(String firstName, String lastName, String[] courseRegistered, String status,
			String statusDate, String managerId) {

		

		LoggerFactory.LogFrontEnd("Validating fields...");
		if (firstName == null || firstName.isEmpty()) {
			LoggerFactory.LogFrontEnd("First name required");
			// throw new RequiredValueException("First name required");
		}

		if (lastName == null || lastName.isEmpty()) {
			LoggerFactory.LogFrontEnd("Last name required");
			// throw new RequiredValueException("Last name required");
		}

		if (statusDate == null || statusDate.isEmpty()) {
			LoggerFactory.LogFrontEnd("Status Date required");
			// throw new RequiredValueException("Status Date required");
		}

		if (courseRegistered == null || courseRegistered.length < 1) {
			LoggerFactory.LogFrontEnd("Registed Course required");
			// throw new RequiredValueException("Registered Course required");
		}

		if (status == null) {
			LoggerFactory.LogFrontEnd("Status required");
			// throw new RequiredValueException("Status required");
		}
		LoggerFactory.LogFrontEnd("Validating fields complete...");

		String msg = "CREATESR" + "|" + "SR" + generateNumber() + "|" + managerId + "|" + firstName + "|" + lastName
				+ "|" + courseRegistered + "|" + status + "|" + statusDate;
		String ack = sendMessage(managerId, msg);
		return ack;
	}

	@Override
	public String getRecordCount(String managerId) {

	
		String msg = "RECORDCOUNT" + "|" + managerId;
		String ack = sendMessage(managerId, msg);
		return ack;
	}

	@Override
	public String editRecords(String recordId, String fieldName, String newValue, String managerId) {

		

		LoggerFactory.LogFrontEnd("Manager :" + managerId + " requested to edit a record.");
		LoggerFactory.LogFrontEnd(String.format("Editing record, RecordID:%s", recordId));

		if (recordId == null || recordId.isEmpty()) {
			LoggerFactory.LogFrontEnd("Record ID required");
			// throw new RequiredValueException("Record ID required");
		}

		if (fieldName == null || fieldName.isEmpty()) {
			LoggerFactory.LogFrontEnd("FieldName required");
			// throw new RequiredValueException("FieldName required");
		}

		if (newValue == null || newValue.isEmpty()) {
			LoggerFactory.LogFrontEnd("FieldValue required");
			// throw new RequiredValueException("FieldValue required");
		}

		String msg = "EDITRECORD" + "|" + managerId + "|" + recordId + "|" + fieldName + "|" + newValue;
		String ack = sendMessage(managerId, msg);
		return ack;
	}

	@Override
	public String transferRecord(String managerId, String recordId, String remoteCenterServerName) {

		

		LoggerFactory.LogFrontEnd("Manager :" + managerId + " requested to transfer a record.");
		LoggerFactory.LogFrontEnd(String.format("Transering record, RecordID:%s", recordId));

		String msg = "TRANSFERRECORD" + "|" + managerId + "|" + recordId + "|" + remoteCenterServerName;
		String ack = sendMessage(managerId, msg);
		return ack;
	}

	private synchronized int generateNumber() {

		Random random = new Random(System.nanoTime());

		return 10000 + random.nextInt(89999);
	}
}

	
