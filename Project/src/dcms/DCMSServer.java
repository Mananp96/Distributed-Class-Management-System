/**
 * 
 */
package dcms;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import rudp.UDPClient;

/**
 * @author Mihir
 *
 */
public class DCMSServer {

	private volatile HashMap<String, ArrayList<Record>> recordData;

	private ArrayList<Record> tempRecords;

	private String name;

	private int serverPort;

	private HashMap<String, Integer> nodePorts;
	
	public DCMSServer(String name,HashMap<String, Integer> nodePorts) {
		this.recordData = new HashMap<String, ArrayList<Record>>();
		this.name = name;
		this.nodePorts = nodePorts;
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
			return String.format("Teacher Record Successfully created by Manager:%s", (managerId));
			
		} else {
			LoggerFactory.Log(this.name,
					String.format("Something went wrong when creating teacher record :%s \n by Manager: %s",
							record.toString(), (managerId)));
			return String.format("Something went wrong when creating teacher record :%s \n by Manager: %s",record.toString(), (managerId));
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
			return String.format("Student Record Successfully created by Manager:%s", (managerId));
		} else {
			LoggerFactory.Log(this.name, String.format("Something went wrong when creating student record :%s \n by Manager: %s",record.toString(), (managerId)));
			return String.format("Something went wrong when creating student record :%s \n by Manager: %s",record.toString(), (managerId));
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

			final int[] nodeports = new int[2];
			int i = 0;
			for (Entry<String, Integer> node : this.nodePorts.entrySet())
				nodeports[i++] = node.getValue();

			final HashMap<Integer, String> result = new HashMap<Integer, String>() {
				{
					put(nodeports[0], "");
					put(nodeports[1], "");
				}
			};

			final CountDownLatch latch = new CountDownLatch(2);
			for (final int port : nodeports) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						
						UDPClient client = new UDPClient("127.0.0.1", port);
						LoggerFactory.Log(name,
								"Request sent to get record data from 127.0.0.1:" + port);
						String reply = client.sendMessage("GET_RECORD_COUNT");
						LoggerFactory.Log(name,
								"Received this response " + reply + " from 127.0.0.1 :" + port);
						if (!reply.equals("INVALID_REQUEST")) {
							result.put(port, reply);
						}
						latch.countDown();
					}
				}).start();

			}
			try {
				latch.await();
				recordCountData += " " + result.get(nodeports[0]) + " " + result.get(nodeports[1]);
			} catch (InterruptedException e) {

			}

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

	public boolean editRecords(String recordId, String fieldName, String newValue, String managerId) {

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

		Record record = findRecord(recordId);

		if (record == null) {
			LoggerFactory.Log(this.name, String.format("Record not found, %s", recordId));
			return false;
		}
		// Set lock on the record, in case if it is editing or transfering by another
		// thread
		synchronized (record.getLock()) {

			// we have to find the record again to make sure the recod has not been
			// transfered and has not been removed from the hashmap by another thread
			record = findRecord(recordId);
			if (record == null) {
				LoggerFactory.Log(this.name, String.format("Record not found, %s", recordId));
				return false;
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
						// throw new RequiredValueException("Status is invalid");
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
						// throw new RequiredValueException("Date is invalid");
					}
					break;
				case "coursesregistered":
					student.setCoursesRegistered(newValue.split(","));
					break;
				default:
					LoggerFactory.Log(this.name, "FieldName is invalid");
					// throw new RequiredValueException("FieldName is invalid");
				}

				LoggerFactory.Log(this.name, String.format("Student record edited, %s", student));

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
						// throw new RequiredValueException("location is invalid");
					}
					assert teacher != null;
					teacher.setLocation(newValue.toLowerCase());
					break;
				default:
					LoggerFactory.Log(this.name, "FieldName is invalid");
					// throw new RequiredValueException("FieldName is invalid");
				}

				LoggerFactory.Log(this.name, String.format("Teacher record edited, %s", teacher));
			}
		}

		return true;
	}

	public boolean transferRecord(String managerID, String recordID, String remoteCenterServerName) {

		LoggerFactory.Log(this.name, "Manager :" + managerID + " requested to transfer a record.");
		LoggerFactory.Log(this.name, String.format("Transering record, RecordID:%s", recordID));

		// find the record
		Record record = findRecord(recordID);

		if (record == null) {
			LoggerFactory.Log(this.name, String.format("Record not found, %s", recordID));
			return false;
		}
		// Set lock on the record, in case if it is editing or transferring by another
		// thread
		synchronized (record.getLock()) {

			// we have to find the record again to make sure the record has not been
			// transfered and has not been removed from the hashmap by another thread
			record = findRecord(recordID);
			if (record == null) {
				LoggerFactory.Log(this.name, String.format("Record not found, %s", recordID));
				return false;
			}

			LoggerFactory.Log(this.name, String.format("Record found, %s", record.toString()));

			// find the remote server
			int port = this.nodePorts.get(remoteCenterServerName);

			if (record.getClass() == StudentRecord.class) {
				StudentRecord student = (StudentRecord) record;
				transferRecord(student, "Student", managerID, port, remoteCenterServerName);

			} else {
				TeacherRecord teacher = (TeacherRecord) record;
				transferRecord(teacher, "Teacher", managerID, port, remoteCenterServerName);

			}
		}

		return true;
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

	private String processRecordTransferRequest(String requestData, String type) {
		requestData = requestData.replaceAll("TRANSFER_" + type.toUpperCase() + ";", "");
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

	private String processAddTransferRequest(String requestData, String type) {
		LoggerFactory.Log(name, "Processing ADD_" + type.toUpperCase() + " request");

		requestData = requestData.replaceAll("ADD_" + type.toUpperCase() + ";", "");
		String[] str = requestData.split(";");
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
				createTRecord(teacher.getFirstName(), teacher.getLastName(), teacher.getAddress(), teacher.getPhone(),
						teacher.getSpecialization(), teacher.getLocation(), managerID);
			} else {
				StudentRecord student = (StudentRecord) record;
				this.createSRecord(student.getFirstName(), student.getLastName(), student.getCoursesRegistered(),
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

	private String sendSocketRequest(DatagramSocket socket, String requestString, InetAddress host, int port,
			String remoteServer) throws IOException {
		LoggerFactory.Log(name, "Sending request to center " + remoteServer + " request details:" + requestString);
		byte[] requestData = requestString.getBytes();
		DatagramPacket request = new DatagramPacket(requestData, requestData.length, host, port);
		socket.send(request);
		LoggerFactory.Log(name, "Request sent to transfer teacher record data to " + host.getHostName() + ":" + port);
		byte[] buffer = new byte[1000];
		DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
		socket.receive(reply);
		String replyData = new String(buffer).replaceAll("\u0000.*", "");
		LoggerFactory.Log(name, "Received this response " + replyData + " from " + host.getHostName() + ":" + port);

		return replyData;
	}

	private boolean transferRecord(final Record record, final String type, final String managerID, final int port,
			final String remoteServer) {

		final CountDownLatch latch = new CountDownLatch(1);
		// Create a new thread to transfer the record
		new Thread(new Runnable() {

			@Override
			public void run() {
				DatagramSocket socket = null;
				try {
					socket = new DatagramSocket();
					InetAddress host = InetAddress.getLocalHost();

					// add the record to temp list to provide transactional operation, we keep a
					// copy of the record in the temp list
					tempRecords.add(record);
					LoggerFactory.Log(name, Integer.toString(port));

					// create a request to transfer the record
					String replyData = sendSocketRequest(socket,
							"TRANSFER_" + type.toUpperCase() + ";" + record.toSplited(), host, port, remoteServer);

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
						replyData = sendSocketRequest(socket,
								"ADD_" + type.toUpperCase() + ";" + managerID + ";" + record.getRecordId() + ";", host,
								port, remoteServer);

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

				} catch (SocketException e) {
					System.out.println(e);
					LoggerFactory.Log(name, "Error occur to connect another region server");

				} catch (UnknownHostException e) {
					System.out.println(e);
					LoggerFactory.Log(name, "Invalid host");
				} catch (IOException e) {
					System.out.println(e);
					LoggerFactory.Log(name, "Invalid data");
				} finally {
					if (socket != null) {
						socket.close();
					}

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
