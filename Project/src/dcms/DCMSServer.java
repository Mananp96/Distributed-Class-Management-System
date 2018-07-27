/**
 * 
 */
package dcms;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.json.simple.JSONObject;

import rudp.UDPClient;

/**
 * @author Mihir
 *
 */
public class DCMSServer {

	private volatile HashMap<String, ArrayList<Record>> recordData;

	private ArrayList<Record> tempRecords;

	private String name;
	
	private JSONObject[] otherRegions;
		
	public DCMSServer(String name,JSONObject[] otherRegions) {
		this.recordData = new HashMap<String, ArrayList<Record>>();
		this.name = name;
		this.otherRegions = otherRegions;		
		this.tempRecords = new ArrayList<Record>();
	}
	
	public void setRegions(JSONObject[] regions) {
		this.otherRegions = regions;
	}
		
	public String createTRecord(String recordId, String firstName, String lastName, String address, String phone, String specialization, String location, String managerId) {

		LoggerFactory.Log(this.name, "Creating Teacher Record.");
		Record record = new TeacherRecord(recordId, firstName, lastName, address, phone, specialization,location);
		
		String firstCharacter = record.getLastName().substring(0, 1).toUpperCase();

		LoggerFactory.Log(this.name, "Adding Record data to List...");
		Boolean result = addToRecordData(firstCharacter, record);

		if (result) {
			LoggerFactory.Log(this.name, String.format("Record added to the list :%s", record.toString()));
			LoggerFactory.Log(this.name,
					String.format("Teacher Record Successfully created by Manager:%s", (managerId)));
			return String.format("SUCCESS: Teacher Record Successfully created by Manager:%s", (managerId));
			
		} else {
			LoggerFactory.Log(this.name,
					String.format("Something went wrong when creating teacher record :%s \n by Manager: %s",
							record.toString(), (managerId)));
			return String.format("ERROR: Something went wrong when creating teacher record :%s \n by Manager: %s",record.toString(), (managerId));
		}
	}

	public String createSRecord(String recordId, String firstName, String lastName, String[] courseRegistered, String status,String statusDate, String managerId) {

		LoggerFactory.Log(this.name, "Creating Student Record...");

		Record record = new StudentRecord(recordId, firstName, lastName, courseRegistered, status, statusDate);

		String firstCharacter = record.getLastName().substring(0, 1).toUpperCase();

		LoggerFactory.Log(this.name, "Adding Record data to List...");
		boolean result = addToRecordData(firstCharacter, record);

		if (result) {
			LoggerFactory.Log(this.name, String.format("Record added to the list :%s", record.toString()));
			LoggerFactory.Log(this.name, String.format("Student Record Successfully created by Manager:%s", (managerId)));
			return String.format("SUCCESS: Student Record Successfully created by Manager:%s", (managerId));
		} else {
			LoggerFactory.Log(this.name, String.format("Something went wrong when creating student record :%s \n by Manager: %s",record.toString(), (managerId)));
			return String.format("ERROR: Something went wrong when creating student record :%s \n by Manager: %s",record.toString(), (managerId));
		}
	}
	
	private boolean addToRecordData(String firstCharacter, Record record) {
		if (this.recordData.containsKey(firstCharacter)) {
			ArrayList<Record> list = this.recordData.get(firstCharacter);
			if (list != null && list.size() > 0) {
				list.add(record);
				return true;
			} else {
				list.add(record);
				this.recordData.put(firstCharacter, list);
				return true;
			}
		} else {
			ArrayList<Record> list = new ArrayList<Record>();
			list.add(record);
			this.recordData.put(firstCharacter, list);
			return true;
		}
	}

	public String getRecordCount(String managerId) {

		if (!(managerId.equalsIgnoreCase("MTL_SERVER") || managerId.equalsIgnoreCase("LVL_SERVER")
				|| managerId.equalsIgnoreCase("DDO_SERVER"))) {

			LoggerFactory.Log(this.name,
					"Received request for " + this.name + " server from " + managerId + " to get record counts.");

			Set<String> keys = this.recordData.keySet();
			int count = 0;
			for (String key : keys) {
				if (this.recordData.get(key) != null) {
					count += this.recordData.get(key).size();
				}
			}

			String recordCountData = this.name + ": " + count;
			LoggerFactory.Log(this.name, "Total Records in " + this.name + " server are " + recordCountData);

			final CountDownLatch latch = new CountDownLatch(this.otherRegions.length);
			final ArrayList<String> results = new ArrayList<String>(); 
			for(final JSONObject region: this.otherRegions) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						
						UDPClient client = new UDPClient((String) region.get("host"), (int) (long)region.get("port"));
						LoggerFactory.Log(name,
								"Request sent to get record data from "+(String) region.get("host")+":" + (int) (long) region.get("port"));
						String reply = null;
						try {
							reply = client.sendMessage("GET_RECORD_COUNT");
						} catch (IOException e) {
							e.printStackTrace();
						}
						LoggerFactory.Log(name,"Received this response " + reply + " from "+(String) region.get("host")+" :" + (int) (long) region.get("port"));
						if (!reply.equals("INVALID_REQUEST")) {
							results.add((String) region.get("name")+": "+reply);
						}
						latch.countDown();
					}
				}).start();
			}

			try {
				latch.await();
				for(int i=0;i<results.size();i++) {
					recordCountData += results.get(i) + " ";
				}
			} catch (InterruptedException e) {}

			return recordCountData;
		} else {
			Set<String> keys = this.recordData.keySet();
			int count = 0;
			for (String key : keys) {
				if (this.recordData.get(key) != null) {
					count += this.recordData.get(key).size();
				}
			}

			return this.name + ": " + count;
		}
	}
	
	public String editRecords(String recordId, String fieldName, String newValue, String managerId) {

		LoggerFactory.Log(this.name, "Manager :" + managerId + " requested to edit a record.");
		LoggerFactory.Log(this.name, String.format("Editing record, RecordID:%s", recordId));

		if (recordId == null || recordId.isEmpty()) {
			LoggerFactory.Log(this.name, "Record ID required");
			return "ERROR: Record ID required";
			// throw new RequiredValueException("Record ID required");
		}

		if (fieldName == null || fieldName.isEmpty()) {
			LoggerFactory.Log(this.name, "FieldName required");
			return "ERROR: FieldName required";
		}

		if (newValue == null || newValue.isEmpty()) {
			LoggerFactory.Log(this.name, "FieldValue required");
			return "ERROR: FieldValue required";
		}

		Record record = findRecord(recordId);

		if (record == null) {
			LoggerFactory.Log(this.name, String.format("Record not found, %s", recordId));
			return "ERROR: Record not found";
		}
		// Set lock on the record, in case if it is editing or transfering by another
		// thread
		synchronized (record.getLock()) {

			// we have to find the record again to make sure the recod has not been
			// transfered and has not been removed from the hashmap by another thread
			record = findRecord(recordId);
			if (record == null) {
				LoggerFactory.Log(this.name, String.format("Record not found, %s", recordId));
				return "ERROR: Record not found";
			}

			LoggerFactory.Log(this.name, String.format("Record found, %s", record.toString()));

			if (record.getClass() == StudentRecord.class) {
				StudentRecord student = (StudentRecord) record;
				switch (fieldName.toLowerCase()) {
				case "firstname":
					student.setFirstName(newValue);
					break;
				case "lastname":
					student.setLastName(newValue);
					break;
				case "status":

					if (!newValue.toLowerCase().equals("active") && !newValue.toLowerCase().equals("inactive")) {
						LoggerFactory.Log(this.name, "Status is invalid");
						return "ERROR: Status is invalid";
					}
					student.setStatus(newValue.toLowerCase());
					break;
				case "statusdate":
					try {
						DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
						df.parse(newValue);
						student.setStatusDate(newValue);
					} catch (ParseException e) {
						LoggerFactory.Log(this.name, "Date is invalid");
						return "ERROR: Date is invalid";
					}
					break;
				case "coursesregistered":
					student.setCoursesRegistered(newValue.split(","));
					break;
				default:
					LoggerFactory.Log(this.name, "FieldName is invalid");
					return "ERROR: FieldName is invalid";
				}

				LoggerFactory.Log(this.name, String.format("Student record edited, %s", student));
				return "SUCCESS: Student record edited";

			} else {
				TeacherRecord teacher = (TeacherRecord) record;
				switch (fieldName.toLowerCase()) {
				case "firstname":
					assert teacher != null;
					teacher.setFirstName(newValue);
					break;
				case "lastname":
					assert teacher != null;
					teacher.setLastName(newValue);
					break;
				case "address":
					assert teacher != null;
					teacher.setAddress(newValue);
					break;
				case "phone":
					assert teacher != null;
					teacher.setPhone(newValue);
					break;
				case "specialization":
					assert teacher != null;
					teacher.setSpecialization(newValue);
					break;

				case "location":

					if (!newValue.toLowerCase().equals("mtl") && !newValue.toLowerCase().equals("lvl")
							&& !newValue.toLowerCase().equals("ddo")) {
						LoggerFactory.Log(this.name, "location is invalid");
						return "ERROR: location is invalid";
					}
					assert teacher != null;
					teacher.setLocation(newValue.toLowerCase());
					break;
				default:
					LoggerFactory.Log(this.name, "FieldName is invalid");
					return "ERROR: FieldName is invalid";
				}

				LoggerFactory.Log(this.name, String.format("Teacher record edited, %s", teacher));
				return "SUCCESS: Teacher record edited";
			}
			
		}

	}
	
	public String transferRecord(String managerID, String recordID, String remoteCenterServerName) {

		LoggerFactory.Log(this.name, "Manager :" + managerID + " requested to transfer a record.");
		LoggerFactory.Log(this.name, String.format("Transering record, RecordID:%s", recordID));

		// find the record
		Record record = findRecord(recordID);

		if (record == null) {
			LoggerFactory.Log(this.name, String.format("Record not found, %s", recordID));
			return "ERROR: Record not found";
		}
		// Set lock on the record, in case if it is editing or transferring by another
		// thread
		synchronized (record.getLock()) {

			// we have to find the record again to make sure the record has not been
			// transfered and has not been removed from the hashmap by another thread
			record = findRecord(recordID);
			if (record == null) {
				LoggerFactory.Log(this.name, String.format("Record not found, %s", recordID));
				return "ERROR: Record not found";
			}

			LoggerFactory.Log(this.name, String.format("Record found, %s", record.toString()));

			int port = 0;
			String host = null;
			
			for(JSONObject regionServer: this.otherRegions) {
				if (((String)regionServer.get("region")).equals(remoteCenterServerName)) {
					port = (int) ((long) (regionServer.get("port")));
					host = (String)regionServer.get("host");
				}
			}

			if (record.getClass() == StudentRecord.class) {
				StudentRecord student = (StudentRecord) record;
				transferRecord(student, "Student", managerID, port, host, remoteCenterServerName);

			} else {
				TeacherRecord teacher = (TeacherRecord) record;
				transferRecord(teacher, "Teacher", managerID, port, host, remoteCenterServerName);

			}
		}

		return "SUCCESS: Record transfered";
	}
	
	
	public String processRecordTransferRequest(String requestData, String type) {
		requestData = requestData.replaceAll("TRANSFER_" + type.toUpperCase() + "|", "");
		if (type.equalsIgnoreCase("Teacher")) {
			// convert the the string request to teacher record
			TeacherRecord teacher = TeacherRecord.fromString(requestData);
			// add the teacher record to temp table, first step of the transaction
			tempRecords.add(teacher);
		} else {
			// convert the the string request to student record
			StudentRecord student = StudentRecord.fromString(requestData);
			// add the student record to temp table, first step of the transaction
			tempRecords.add(student);
		}
		return "OK";
	}

	public String processAddTransferRequest(String requestData, String type) {
		LoggerFactory.Log(name, "Processing ADD_" + type.toUpperCase() + " request");

		requestData = requestData.replaceAll("ADD_" + type.toUpperCase() + "|", "");
		String[] str = requestData.split("|");
		String managerID = str[0];
		String recordID = str[1];
		LoggerFactory.Log(name,
				"Processing ADD_" + type.toUpperCase() + " request, managerID:" + managerID + " recordID:" + recordID);
		Record record = null;
		// find the record on the temp list, to commit the transfer transaction
		for (Record r : tempRecords) {

			if (r.getRecordId().equalsIgnoreCase(recordID.trim())) {
				record = r;
				break;
			}
		}

		if (record == null) {
			// if record has not been find means something is wrong and we will notify the
			// remote server to rollback
			LoggerFactory.Log(name, "Record not found");
			return "Record_not_found";
		} else {

			LoggerFactory.Log(name, "Record in temp list found, " + record.toString());
			// commit the changes and add the record to the main hashmap
			if (type.equalsIgnoreCase("Teacher")) {
				TeacherRecord teacher = (TeacherRecord) record;
				this.createTRecord(teacher.getRecordId(),teacher.getFirstName(), teacher.getLastName(), teacher.getAddress(), teacher.getPhone(),
						teacher.getSpecialization(), teacher.getLocation(), managerID);
			} else {
				StudentRecord student = (StudentRecord) record;
				this.createSRecord(student.getRecordId(), student.getFirstName(), student.getLastName(), student.getCoursesRegistered(),
						student.getStatus(), student.getStatusDate(), managerID);
			}

			LoggerFactory.Log(name, "Attemping to remove record from temp list");
			// remove the record from the temp list
			tempRecords.remove(record);

			LoggerFactory.Log(name, "Record removed from temp list");
			// notify the remote server to commit the transaction
			return "OK";
		}
	}

	private void removeRecord(Record record) {
		for (ArrayList<Record> records : recordData.values()) {

			for (Record r : records) {

				if (r.getRecordId().equalsIgnoreCase(record.getRecordId())) {

					records.remove(r);
					return;
				}
			}
		}
	}

	private boolean transferRecord(final Record record, final String type, final String managerID, int port, String host,
			final String remoteServer) {

		final CountDownLatch latch = new CountDownLatch(1);
		final UDPClient client = new UDPClient(host,port);
		// Create a new thread to transfer the record
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// add the record to temp list to provide transactional operation, we keep a
					// copy of the record in the temp list
					tempRecords.add(record);
					LoggerFactory.Log(name, Integer.toString(port));

					// create a request to transfer the record
					String replyData = client.sendMessage("TRANSFER_" + type.toUpperCase() + "|" + record.toSplited());
					
					// if the remote server couldn't accept the request we have to remove the record
					// from the temp table, which means rollback
					if (!replyData.equalsIgnoreCase("OK")) {
						LoggerFactory.Log(name, "Error occur to transfer record");
						tempRecords.remove(record);
					} else {
						// remove the record from the main hash map
						removeRecord(record);
						LoggerFactory.Log(name, type + " record removed");

						// send a request to the remote server to add the reocrd to main hash map of the
						// remote server
						replyData = client.sendMessage("ADD_" + type.toUpperCase() + "|" + managerID + "|" + record.getRecordId() + "|");
						

						if (!replyData.equalsIgnoreCase("OK")) {
							LoggerFactory.Log(name, "Error occur to add " + type);
							String firstCharacter = record.getLastName().substring(0, 1).toUpperCase();
							// if the remote server couldn't commit the changes we need to rollback which
							// means we have to add the reocrd to main hash map again
							addToRecordData(firstCharacter, record);
							// remove the record from the temp list
							tempRecords.remove(record);
						} else {
							// everything goes well so we remove the record from the temp list
							tempRecords.remove(record);
						}

					}
				} catch (IOException e) {
					System.out.println(e);
					LoggerFactory.Log(name, "Invalid data");
				} finally {
					latch.countDown();
				}

			}
		}).start();

		try {
			latch.await();
			return true;
		} catch (InterruptedException e) {

			return false;
		}

	}
	
	private Record findRecord(String recordId) {
		LoggerFactory.Log(this.name, "Looking record id");
		Record record = null;
		for (ArrayList<Record> records : this.recordData.values()) {

			for (Record r : records) {

				if (r.getRecordId().equalsIgnoreCase(recordId)) {

					record = r;
					break;
				}
			}

			if (record != null)
				break;
		}

		return record;

	}
	
}
